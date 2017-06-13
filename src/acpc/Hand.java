package acpc;

public class Hand {
	
	public int [] boardCards;
	public int [][] holeCards;
	public int [][] precomputedBuckets;
	
	/* only support 2p */
	int [] showdownValue;
	
	public Hand(Game game) {
		boardCards = new int[game.MAX_NUM_HOLECARDS];
		holeCards = new int[game.numPlayers][game.MAX_NUM_HOLECARDS];
		precomputedBuckets = new int[game.numPlayers][game.numRounds];
		
		showdownValue = new int[game.numPlayers];
		
	}
	
}
