package acpc;

import java.util.ArrayList;

public class MatchState extends State{
	
	public int viewingPlayer = -1;
	public int handId = -1;
	
	public void readMatchState(Game game, String message) {
		initState(game, 0);
		String [] messages = message.split(":");
		viewingPlayer =  Integer.parseInt(messages[1]);
		handId =  Integer.parseInt(messages[2]);
		
		String cardString = messages[4];
		String bettingString = messages[3];
		
		String holeCards = null, boardCards = null;
		
		int firstSlashIndex = cardString.indexOf("/");
		if ( firstSlashIndex == -1 ) {
			holeCards = cardString.split("\\|")[viewingPlayer];
		} else {
			holeCards = cardString.substring(0, firstSlashIndex).split("\\|")[viewingPlayer];
			boardCards = cardString.substring(firstSlashIndex + 1);
		}
		
		
		readHoleCards(holeCards);
		readBoardCards(boardCards);
		ArrayList<Action> actions =  readBetting(bettingString);
		for ( Action action : actions ) {
			game.doAction(this, action);	
		}
	}
	
	public void readBoardCards(String boardCards) {
		if ( boardCards == null ) {
			return ;
		}
		int len = boardCards.length();
		assert( len % 2 == 0 );
		for ( int i = 0; i < len / 2; i ++ ) {
			char rankChar = boardCards.charAt(2 * i);
			char suitChar = boardCards.charAt(2 * i + 1);
			int card = Card.makeCard(rankChar, suitChar);
			this.boardCards[i] = card;
		}
	}
	
	public void readHoleCards(String holeCards) {
		if ( holeCards == null ) {
			return ;
		}
		int len = holeCards.length();
		assert( len % 2 == 0 );
		for ( int i = 0; i < len / 2; i ++ ) {
			char rankChar = holeCards.charAt(2 * i);
			char suitChar = holeCards.charAt(2 * i + 1);
			//TODO
			suitChar = 'c';
			if ( rankChar == 'Q' ) {
				rankChar = '2';
			} else if ( rankChar == 'K' ) {
				rankChar = '3';
			} else if ( rankChar == 'A' ) {
				rankChar = '4';
			}
			int card = Card.makeCard(rankChar, suitChar);
			this.holeCards[viewingPlayer][i] = card;
		}
	}
	
	public ArrayList<Action> readBetting(String bettingHistory) {
		if ( bettingHistory == null || bettingHistory == "" ) {
			return null;
		}
		ArrayList<Action> actions = new ArrayList<Action>();
		int len = bettingHistory.length();
		boolean raiseFlag = false;
		String temp = "";
		
		for ( int i = 0; i < len; ++i ) {
			char ch = bettingHistory.charAt(i);
			switch ( ch ) {
			case 'c' :
				actions.add(new Action(ActionType.a_call, 0));
				break;
			case 'f' :
				actions.add(new Action(ActionType.a_fold, 0));
				break;
			case 'r':
				if (raiseFlag) {
					actions.add(new Action(ActionType.a_raise, Integer.parseInt(temp)));
					temp = "";
				}
				raiseFlag = !raiseFlag;
				// if there is no more character left, this must be a 0-size raise
				if ( i + 1 == len ) {
					actions.add( new Action(ActionType.a_raise, 0));
				}
				break;
			default :
				temp += ch;
				break;
			}
		}
		return actions;
	}
	
	public static void main(String[] args) {
		
		MatchState m = new MatchState();
		Game game = new KuhnGame();
		m.initState(game, 0);
		m.readMatchState(game, "MATCHSTATE:0:9:c:As|");
	}
	
}
