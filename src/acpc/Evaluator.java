package acpc;

public class Evaluator {

	private static CardSet cardSet = new CardSet();
	
	public static int evaluate(Game game) {
		// TODO
		return 0;
	}
	
	public static void evaluate(Hand hand, int [] ranks) {
		int numPlayers = hand.holeCards.length;
		
		for ( int p = 0; p < numPlayers; ++p ) {
			cardSet.emptyCardSet();
			for (int i = 0; i < hand.holeCards[p].length; ++i ) {
				int card = hand.holeCards[p][i];
				cardSet.addCard(Card.suitOfCard(card), Card.rankOfCard(card));
			}
			for (int i = 0; i < hand.boardCards.length; ++i ) {
				int card = hand.boardCards[i];
				cardSet.addCard(Card.suitOfCard(card), Card.rankOfCard(card));
			}
			ranks[p] = cardSet.rankCardSet();
		}
	}
	
	
	public static void main(String[] args) {
		
	}

}
