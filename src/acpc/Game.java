package acpc;

import java.util.Random;

public class Game {
	
	public static final int MAX_NUM_ACTIONS = 64;
	public int MAX_NUM_BOARDCARDS;
	public int MAX_NUM_HOLECARDS;
	
	private Random rand = new Random();

	/* stack sizes for each player */
	public int stack[];

	/* entry fee for game, per player */
	public int blind[];

	/* size of fixed raises for limitBetting games */
	public int raiseSize[];

	/* general class of game */
	public String bettingType;

	/* number of players in the game */
	public int numPlayers;

	/* number of betting rounds */
	public int numRounds;

	/* first player to act in a round */
	public int firstPlayer[];

	/* number of bets/raises that may be made in each round */
	public int maxRaises[];

	/* number of suits and ranks in the deck of cards */
	public int numSuits;
	public int numRanks;

	/* number of private player cards */
	public int numHoleCards;

	/* number of shared public cards each round */
	public int numBoardCards [];
	
	public State state;
	
	
	
	public Game() {
		state = new State(this, 0l);
//		rand.setSeed(System.currentTimeMillis());
		rand.setSeed(0);
	}
	
}
