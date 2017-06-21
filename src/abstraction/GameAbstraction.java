package abstraction;

import node.BettingNode;
import acpc.Game;
import acpc.KuhnGame;
import acpc.State;
import parameter.AbsParameter;
import tree.BettingTree;

public class GameAbstraction {
	
	public Game game;
	public State state;
	public int [] numEntriesPerBucket;
	public BettingNode root;
	public final CardAbstraction cardAbs;
	public final ActionAbstraction actionAbs;
	
	
	public GameAbstraction(AbsParameter params) {
		
		/* choose game type */
		switch (params.gameType) {
		case "Kuhn":
			game = new KuhnGame();
			break;
		case "limit Texas":
			//TODO
			break;
		case "no-limit Texas":
			//TODO
			break;
		default:
			System.out.println("Error: Game Type Not Supported!");
			System.exit(-1);
		}
		/* initialize game state */
		state = new State();
		state.initState(game, 0);
		
		/* choose action abstraction type */
		switch (params.actionAbsType) {
		case "NullActionAbstraction":
			actionAbs = new NullActionAbstraction();
			break;
		case "FcpaActionAbstraction":
			//TODO
			actionAbs = new FcpaActionAbstraction();
			break;
		default:
			actionAbs = null;
			System.out.println("Error: Action Abstraction Parameter Not Supported!"); 
			assert( false );
		}
		
		/* init num_entries_per_bucket to zero */
		numEntriesPerBucket = new int[game.numRounds];
		
		/* build betting tree */
		try {
			root = BettingTree.buildTree(game, state, actionAbs, numEntriesPerBucket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/* Create card abstraction */
		switch (params.cardAbsType) {
		case "NullCardAbstraction" :
			cardAbs = new NullCardAbstraction(game);
			break;
		case "BlindCardAbstraction" :
			//TODO
			cardAbs = null;
			System.out.println("ERROR: BlindCardAbstraction not implemented yet!");
			break;
		default:
			cardAbs = null;
			System.out.println("ERROR: Card Abstraction Parameter Not Supported!");
			assert( false );
		}
		
	}
	
	public static void main(String[] args) {
		Game game = new KuhnGame();
		State state = new State();
		state.initState(game, 0);
		ActionAbstraction actionAbs = new NullActionAbstraction();
		int [] numEntriesPerBucket = new int[game.numRounds];
		BettingNode root = null;
		try {
			root = BettingTree.buildTree(game, state, actionAbs, numEntriesPerBucket);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int sum = visit(root);
		assert( sum == 9 );
		System.out.println("build tree success!");
	}
	
	public static int visit(BettingNode node) {
		int sum = 0;
		if ( node == null ) {
			return sum;
		} else {
			sum += 1;
		}
		BettingNode child = node.getChild();
		if ( child == null ) {
			return sum;
		}
		do {
			sum += visit(child);
			child = child.getSibling();
		} while ( child != null );
		return sum;
	}

	public void countEntries(int [] numEntriesPerBucket, int [] totalNumEntries) {
		countEntries(root, numEntriesPerBucket, totalNumEntries);
	}
	
	public void countEntries(BettingNode node, int [] numEntriesPerBucket, int [] totalNumEntries) {
		BettingNode child = node.getChild();
		if ( child == null ) {
			/* terminal node */
			return ;
		}
		final int round = node.getRound();
		final int numChoices = node.getNumChoices();
		
		/* Update entries counts */
		numEntriesPerBucket[round] += numChoices;
		final int buckets = cardAbs.numBuckets(game, node);
		totalNumEntries[round] += buckets * numChoices;
		
		/* Recurse */
		for ( int c = 0; c < numChoices; ++c ) {
			countEntries(child, numEntriesPerBucket, totalNumEntries);
			child = child.getSibling();
		}
	}
	
}
