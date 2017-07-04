package acpc;

public class Card {
	
	public static final String rankChars = "23456789TJQKA";
	public static final String suitChars = "cdhs";
	
	public static final int MAX_SUITS = 4;
	public static final int MAX_RANKS = 13;
	
	public static int rankOfCard(int card) {
		return card % MAX_SUITS;
	}
	
	public static int suitOfCard(int card) {
		return card / MAX_SUITS;
	}
	
	public static int makeCard(int rank, int suit) {
		return rank * MAX_SUITS + suit;
	}

	public static int makeCard(char rankChar, char suitChar) {
		int rank = -1, suit = -1;
		switch (rankChar) {
			case 'T' :
				rank = 8;
				break;
			case 'J' :
				rank = 9;
				break;
			case 'Q' :
				rank = 10;
				break;
			case 'K' :
				rank = 11;
				break;
			case 'A' :
				rank = 12;
				break;
			default:
				rank = Character.getNumericValue(rankChar) - 2;
		}

		switch (suitChar) {
			case 'c' :
				suit = 0;
				break;
			case 'd' :
				suit = 1;
				break;
			case 'h' :
				suit = 2;
				break;
			case 's' :
				suit = 3;
				break;
			default:
				System.out.println("ERROR: suit must be in cdhs!");
				System.exit(-1);
		}
		return rank * MAX_SUITS + suit;
	}

	public static String cardToString(int card) {
		int rankOfCard = rankOfCard(card);
		int suitOfCard = suitOfCard(card);
		return rankChars.substring(rankOfCard, rankOfCard);
	}
}
