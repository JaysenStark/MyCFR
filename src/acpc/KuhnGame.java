package acpc;

public class KuhnGame extends Game implements KuhnGameConstants {
	
	public KuhnGame() {
//		System.out.println("Kuhn Game is choosed.");
		numPlayers = NUM_PLAYERS;
		numRounds = NUM_ROUNDS;
		numSuits = NUM_SUITS;
		numRanks = NUM_RANKS;
		numHoleCards = NUM_HOLECARDS;
		numBoardCards = NUM_BOARDCARDS;
		firstPlayer = FIRST_PLAYER;
		stack = new int[numPlayers];
		blind = BLIND;
		bettingType = BETTING_TYPE;
		maxRaises = MAX_RAISES;
		raiseSize = RAISE_SIZE;
		
		int sum = 0;
		for (int n : numBoardCards) {
			sum += n;
		}
		MAX_NUM_BOARDCARDS = sum;
		MAX_NUM_HOLECARDS = numHoleCards;
	}
	
	public void printGameConfig() {
		System.out.println("GAMEDEF");
		System.out.println(bettingType);
		System.out.println("numPlayers = " + numPlayers);
		System.out.println("numRounds = " + numRounds);
		System.out.print("blind =");
		for (int b : blind){
			System.out.print(" " + b);
		}
		System.out.println();
		System.out.print("raiseSize =" );
		for (int b : raiseSize){
			System.out.print(" " + b);
		}
		System.out.println();
		System.out.print("firstPlayer =");
		for (int b : firstPlayer){
			System.out.print(" " + b);
		}
		System.out.println();
		System.out.print("maxRaises =");
		for (int b : maxRaises){
			System.out.print(" " + b);
		}
		System.out.println();
		System.out.println("numSuits = " + numSuits);
		System.out.println("numRanks = " + numRanks);
		System.out.println("numHoleCards = " + numHoleCards);
		System.out.print("numBoardCards =");
		for (int b : numBoardCards){
			System.out.print(" " + b);
		}
		System.out.println();
		System.out.println("END GAMEDEF");
//		System.out.println(this.MAX_NUM_BOARDCARDS);
//		System.out.println(this.MAX_NUM_HOLECARDS);
	}
	
	public static void main(String[] args) {
//		KuhnGame g = new KuhnGame();
//		g.printGameConfig();
	}
	
	@Override
	public KuhnGame clone() throws CloneNotSupportedException {
		return (KuhnGame) super.clone();
	}
	
}
