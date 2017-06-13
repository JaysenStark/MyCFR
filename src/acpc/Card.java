package acpc;

public class Card {
	
	public static final int MAX_SUITS = 4;
	public static final int MAX_RANKS = 13;
	
	public static int rankOfCard(int card) {
		return card % MAX_SUITS;
	}
	
	public static int suitOfCard(int card) {
		return card / MAX_SUITS;
	}
	
	public static int makeCard(int rank, int suit) {
		return rank * MAX_SUITS + rank;
	}

}
