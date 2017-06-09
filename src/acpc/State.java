package acpc;

public class State {
	
	public final long handId;
	public int maxSpent;
	public int [] spent;
	public int minNoLimitRaiseTo;
	public boolean [] playerFolded;
	public int [] numActions;
	public int round;
	public boolean finished;
	public Action [][] action;
	public int [][] actingPlayer;
	public int [] boardCards;
	public int [][] holeCards;

	public State(Game game, long handId){
		this.handId = handId;
		maxSpent = 0;
		
		spent = new int[game.numPlayers];
		/* playerFolded all 0 */
		playerFolded = new boolean[game.numPlayers];
		
		for ( int p = 0; p < game.numPlayers; ++p ) {
			spent[p] = game.blind[p];
			if ( game.blind[p] > spent[p] ) {
				maxSpent = game.blind[p];
			}
		}
		
		if ( game.bettingType == "nolimit" ) {
			/* no-limit games need to keep track of the minimum bet */
			if ( maxSpent > 0 ) {
				 /* we'll have to call the big blind and then raise by that
				 amount, so the minimum raise-to is 2*maximum blinds */
				minNoLimitRaiseTo = maxSpent * 2;
			} else {
				/* need to bet at least one chip, and there are no blinds/ante */
				minNoLimitRaiseTo = 1;
			}
		} else {
			/* limit games don't need to worry about minimum raises */
			minNoLimitRaiseTo = 0;
		}
		
		for ( int p = 0; p < game.numPlayers; ++p ) {
			spent[p] = game.blind[p];
			if( game.blind[p] > maxSpent ) {
				maxSpent = game.blind[p];
			}
		}
		/* numActions all 0 */
		numActions = new int[game.numRounds];
		
		round = 0;
		finished = false;
		
		action = new Action[game.numRounds][game.MAX_NUM_ACTIONS];
		actingPlayer = new int[game.numRounds][game.MAX_NUM_ACTIONS];
		boardCards = new int[game.MAX_NUM_ACTIONS];
		holeCards = new int [game.numPlayers][game.MAX_NUM_HOLECARDS];	
	}
	
	

}
