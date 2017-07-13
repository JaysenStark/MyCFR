package solver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import node.BettingNode;
import node.Entries;
import node.EntriesLoader;
import parameter.AbsParameter;
import parameter.SolverParameter;
import abstraction.GameAbstraction;
import acpc.Evaluator;
import acpc.Hand;
import acpc.State;

public class PureCFRMachine {
	
	public GameAbstraction gameAbs;
	public boolean doAverage;
	public Entries [] regrets;
	public Entries [] avgStrategy;
	
	private Random random;
	private final int blockSize;
	private int counter;
	private int threadNumber;
	private CountDownLatch cdLatch;
	
	public PureCFRMachine(final AbsParameter params, final SolverParameter solverParams) {
		this.doAverage = params.doAverage;
		gameAbs = new GameAbstraction(params);
		regrets = new Entries[gameAbs.game.numRounds];
		avgStrategy = new Entries[gameAbs.game.numRounds];
		int[] numEntriesPerBucket = new int[gameAbs.game.numRounds];
		int[] totalNumEntries = new int[gameAbs.game.numRounds];
		
		gameAbs.countEntries(numEntriesPerBucket, totalNumEntries);
		
		// initialize regret and average strategy
		for (int r = 0; r < gameAbs.game.numRounds; ++r) {
				// regret
				regrets[r] = new EntriesLoader(numEntriesPerBucket[r], totalNumEntries[r]);
				// average strategy
				avgStrategy[r] = new EntriesLoader(numEntriesPerBucket[r], totalNumEntries[r]);
		}
		random = ThreadLocalRandom.current();
		blockSize = solverParams.getBlockSize();
		threadNumber = solverParams.getThreadNumber();
		counter = 0;
	}
	
	public void setCdLatch(CountDownLatch cdLatch) {
		this.cdLatch = cdLatch;
	}


	public void doIteration() {
		Hand hand = new Hand(gameAbs.game);
		generateHand(hand, random);
		for ( int p = 0; p < gameAbs.game.numPlayers; ++p ) {
			walkPureCFR(p, gameAbs.root, hand, random);
		}
	}
	
	// do iteration with block size number
	public void doIterations() {
		for (int i = 0; i < blockSize; ++i) {
			doIteration();
		}
	}
	
	//ADV deal cards directly into hand
	public void generateHand(Hand hand, final Random random) {
		State state = new State();
		state.initState(gameAbs.game, 0);
		gameAbs.game.dealCards(random, state);
		/* copy board cards, hole cards (both player) dealt by game into hand */
		for ( int i = 0; i < state.game.MAX_NUM_BOARDCARDS; ++i ) {
			hand.boardCards[i] = state.boardCards[i];
		}
		for ( int p = 0; p < state.game.numPlayers; ++p ) {
			for ( int i = 0; i < state.game.MAX_NUM_HOLECARDS; ++i ) {
				hand.holeCards[p][i] = state.holeCards[p][i];
			}
		}
		/* bucket each hands for each player, round if possible */
		if ( gameAbs.cardAbs.canPrecomputeBuckets() ) {
			gameAbs.cardAbs.precomputeBuckets(gameAbs.game, hand);
		}
		
		/* rank the hands */
		assert( state.round == gameAbs.game.numRounds - 1 );
		int [] ranks = new int[gameAbs.game.numPlayers];
		Evaluator.evaluate(hand, ranks);
		
		/* set evaluation values */
		switch( gameAbs.game.numPlayers ) {
		case 2 :
			if ( ranks[0] > ranks[1] ) {
				hand.showdownValue[0] = 1;
				hand.showdownValue[1] = -1;
			} else if ( ranks[0] < ranks[1] ) {
				hand.showdownValue[0] = -1;
				hand.showdownValue[1] = 1;
			} else {
				hand.showdownValue[0] = 0;
				hand.showdownValue[1] = 0;
			}
			break;
		case 3 :
			System.out.println("ERROR: not implemented for 3 player evaluation yet!");
			System.exit(-1);
		default:
			System.out.println("ERROR: player number must be wrong!");
			System.exit(-1);
		}
	}
	
	public int walkPureCFR(final int position, final BettingNode curNode, final Hand hand, final Random random) {
		int retVal = 0;
		if ( curNode.getChild() == null || curNode.didPlayerFold(position) ) {
			retVal = curNode.evaluate(hand, position);
			return retVal;
		}
		
		/* Grab some values that will be used often */
		int numChoices = curNode.getNumChoices();
		int player = curNode.getPlayer();
		int round = curNode.getRound();
		int solnIdx = curNode.getSolnIdx();
		
		int bucket = -1;
		if ( gameAbs.cardAbs.canPrecomputeBuckets() ) {
			bucket = hand.precomputedBuckets[player][round];
		} else {
			bucket = gameAbs.cardAbs.getBucket(gameAbs.game, curNode, hand);
		}
		
		/* Get the positive regrets at this information set */
		int [] positiveRegrets = new int[numChoices];
		int sumPositiveRegrets = regrets[round].getPositiveValues(bucket, solnIdx, numChoices, positiveRegrets);
		
		if ( sumPositiveRegrets == 0 ) {
			sumPositiveRegrets = numChoices;
			/* No positive regret, so assume a default uniform random current strategy */
			for ( int c = 0; c < numChoices; ++c ) {
				positiveRegrets[c] = 1;
			}
		}
		
		/* Purify the current strategy so that we always take choice */
		double dart = random.nextDouble() * sumPositiveRegrets;
		int choice;
		for ( choice = 0 ; choice < numChoices; ++ choice) {
			if ( dart < positiveRegrets[choice] ) {
				break;
			}
			dart -= positiveRegrets[choice];
		}
		assert( choice < numChoices );
		assert( positiveRegrets[choice] > 0 );
		
		BettingNode child = curNode.getChild();
		
		if ( player != position ) {
			/* Opponent's node. Recurse down the single choice. */
			for ( int c = 0; c < choice; ++c ) {
				child = child.getSibling();
			}
			retVal = walkPureCFR(position, child, hand, random);
			
			/* Update the average strategy if we are keeping track of one */
			if ( doAverage ) {
				if ( avgStrategy[round].incrementEntry(bucket, solnIdx, choice) ) {
					;
				} else {
					// overflow
					System.out.print("The average strategy has overflown : " +
							"To fix this, you must set a bigger AVG_STRATEGY_TYPE " +
							"in constants.cpp and start again from scratch.\n");
					System.exit(-1);
				}
			}
		} else {
			/* Current player's node. Recurse down all choices to get the value of each */
			int [] values = new int[numChoices];
			
			for ( int c = 0; c < numChoices; ++c ) {
				values[c] = walkPureCFR(position, child, hand, random);
				child = child.getSibling();
			}
			
			 /* We return the value that the sampled pure strategy attains */
			retVal = values[choice];
			
			 /* Update the regrets at the current node */
			regrets[round].updateRegret(bucket, solnIdx, numChoices, values, retVal);
		}
		
		return retVal;
	} //  end walkPureCFR
	
	public void dumpStrategy(File file) {
		FileOutputStream os = null;
		DataOutputStream dos = null;
		try {
			os = new FileOutputStream(file);
			dos = new DataOutputStream(os);
			for ( int r = 0; r < avgStrategy.length; ++r ) {
				avgStrategy[r].dump(dos);
			}
			dos.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void dumpRegret(File file) {
		FileOutputStream os = null;
		DataOutputStream dos = null;
		try {
			os = new FileOutputStream(file);
			dos = new DataOutputStream(os);
			for ( int r = 0; r < regrets.length; ++r ) {
				regrets[r].dump(dos);
			}
			dos.close();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadStrategy(File file) {
		FileInputStream is = null;
		DataInputStream dis = null;
		try {
			is = new FileInputStream(file);
			dis = new DataInputStream(is);
			for ( int r = 0; r < avgStrategy.length; ++ r ) {
				avgStrategy[r].load(dis);
			}
			dis.close();
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void loadRegret(File file) {
		FileInputStream is = null;
		DataInputStream dis = null;
		try {
			is = new FileInputStream(file);
			dis = new DataInputStream(is);
			for ( int r = 0; r < regrets.length; ++ r ) {
				regrets[r].load(dis);
			}
			dis.close();
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
