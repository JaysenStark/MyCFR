package display;

import java.io.File;

import parameter.AbsParameter;

import abstraction.GameAbstraction;
import abstraction.NullCardAbstraction;
import acpc.Action;
import acpc.ActionType;
import acpc.Card;
import acpc.Game;
import acpc.KuhnGame;
import acpc.State;
import agent.PlayerModule;
import node.BettingNode;

public class Display{
	
	 public static void displayStrategy(PlayerModule playerModule, State state, GameAbstraction gameAbs,
			 final int p, final int MAX_ROUND) {
		 if ( state.finished || state.round >= MAX_ROUND ) {
			 /* End of game or we've gone past the rounds we care to print */
			 return ;
		 }
		 Game game = gameAbs.game;
		 
		 Action [] actions = gameAbs.actionAbs.getActions(game, state);
		 
		 String stateString = null;
		 if ( p == game.currentPlayer(state) ) {
			 stateString = stateToString(game, state, p);
			 
			 // print player's action probabilities over actions for every possible bucket
			 final int buckets = gameAbs.cardAbs.numBuckets(state);
			 for ( int bucket = 0; bucket < buckets; ++bucket ) {
				 // get action probabilities 
				 double [] actionProbs = playerModule.getActionProbs(state, bucket);
				 // display bucket, state, action probabilities
				 System.out.print("Bucket " + bucket + " " + stateString +  ": ");
				 for ( int a = 0; a < actions.length; ++a ) {
					 System.out.print(actions[a].toString() + ":" + actionProbs[a] + " ");
				 }
				 System.out.print("\n");
			 }
		 } // endif
		 
		 // recurse
		 for ( int a = 0; a < actions.length; ++a ) {
			 State newState = (State) state.clone();
			 game.doAction(newState, actions[a]);
			 displayStrategy(playerModule, newState, gameAbs, p, MAX_ROUND);
		 }
		 
	 }
	 
	
	 public static String stateToString(Game game, State state, int p) {
//		String playerHands = handToString(game, state, p);
		String bettingHistory = actionToString(game, state);
		return "[" + bettingHistory + "]";
	 }
	 
	 /*
	  * playerHands contains player's own cards and board cards
	  */
	 public static String handToString(Game game, State state, int p) {
		 StringBuilder sb = new StringBuilder();
		 for ( int i = 0; i < game.MAX_NUM_HOLECARDS; ++i ) {
			 int card = state.holeCards[p][i];
			 sb.append(Card.cardToString(card));
		 }
		 sb.append(":");
		 for ( int i = 0; i < state.game.MAX_NUM_BOARDCARDS; ++i ) {
			 int card = state.boardCards[i];
			 sb.append(Card.cardToString(card));
		 }
		 return sb.toString();
	 }

	 public static String actionToString(Game game, State state) {
		 StringBuilder sb = new StringBuilder();
		 for ( int r = 0; r < state.action.length; ++r ) {
			 for (int n = 0; n < state.action[r].length; ++n ) {
				 if ( state.action[r][n] == null || state.action[r][n].type == ActionType.a_invalid ){
					 break;
				 }
				 sb.append(state.action[r][n].toString());
			 }
			 if ( r < state.round ) {
				 sb.append("/");
			 }
		 }
		 return sb.toString();
	 }
	 
	 public static void main(String[] args) {
		int player = 0;
		int MAX_ROUND = 1;
		Game game = new KuhnGame();
		State state = new State();
		state.initState(game, 0);
		File file = new File("iter-40000000.avgStrategy");
		PlayerModule playerModule = new PlayerModule(file);
		GameAbstraction gameAbs = new GameAbstraction(new AbsParameter());
		System.out.println("---position 0---");
		Display.displayStrategy(playerModule, state, gameAbs, 0, MAX_ROUND);
		System.out.println("---position 1---");
		Display.displayStrategy(playerModule, state, gameAbs, 1, MAX_ROUND);
	}
}
