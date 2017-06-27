package abstraction;

import node.BettingNode;
import acpc.Card;
import acpc.Game;
import acpc.Hand;
import acpc.State;

public class NullCardAbstraction extends CardAbstraction {

	public int mNumBuckets[];
	public Game game;
	
	public NullCardAbstraction(Game game) {
		this.game = game;
		mNumBuckets = new int[game.numRounds];
		
		/* Precompute number of buckets per round */
		int deckSize = game.numRanks * game.numSuits;
		mNumBuckets[0] = 1;
		for ( int i = 0; i < game.numHoleCards; ++i ) {
			mNumBuckets[0] *= deckSize;
		}
		for ( int r = 0; r < game.numRounds; ++r ) {
			if ( r > 0 ) {
				mNumBuckets[r] = mNumBuckets[r - 1];
				for ( int i = 0; i < game.numBoardCards[r]; ++i ) {
					mNumBuckets[r] *= deckSize;
				}
			}
		}
	}
	
	@Override
	public int numBuckets(Game game, BettingNode node) {
		return mNumBuckets[node.getRound()];
	}

	@Override
	public int numBuckets(State state) {
		return mNumBuckets[state.round];
	}

	@Override
	public int getBucket(Game game, BettingNode node, Hand hand) {
		int [] boardCards = hand.boardCards;
		int [] currentPlayerHoleCards = hand.holeCards[node.getPlayer()];
		int round = node.getRound();
		return getBucketInternal(game, boardCards, currentPlayerHoleCards, round);
	}

	private int getBucketInternal(Game game, int[] boardCards, int[] currentPlayerHoleCards, int round) {
		int bucket = 0;
		int deckSize = game.numRanks * game.numSuits;
		for ( int i = 0; i < game.numHoleCards; ++i ) {
			if ( i > 0 ) {
				bucket *= deckSize;
			}
			int card = currentPlayerHoleCards[i];
			bucket += Card.rankOfCard(card) * game.numSuits + Card.suitOfCard(card);
		}
		for ( int r = 0; r <= round; ++r ) {
			for ( int i = game.bcStart(r); i < game.sumBoardCards(r); ++i ) {
				bucket *= deckSize;
				int card = boardCards[i];
				bucket += Card.rankOfCard(card) * game.numSuits + Card.suitOfCard(card);
			}
		}
		return bucket;
	}

	@Override
	public void precomputeBuckets(Game game, Hand hand) {
		for ( int p = 0; p < game.numPlayers; ++p ) {
			for (int r = 0; r < game.numRounds; ++r ) {
				hand.precomputedBuckets[p][r] = getBucketInternal(game, hand.boardCards, hand.holeCards[p], r);
			}
		}
	}

	@Override
	public boolean canPrecomputeBuckets() {
		return true;
	}

	@Override
	public int getBucket(Game game, BettingNode node, int[] boardCards, int[][] holeCards) {
		int [] currentPlayerHoleCards = holeCards[node.getPlayer()];
		int round = node.getRound();
		return getBucketInternal(game, boardCards, currentPlayerHoleCards, round);
	}

}
