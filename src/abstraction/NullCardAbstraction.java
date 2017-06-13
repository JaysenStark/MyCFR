package abstraction;

import node.BettingNode;
import acpc.Game;

public class NullCardAbstraction extends CardAbstraction {

	public int mNumBuckets[];
	
	@Override
	public int numBuckets(Game game, BettingNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int numBuckets(Game game) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getBucket(Game game, BettingNode node) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void precomputeBuckets(Game game) {
		// TODO Auto-generated method stub

	}

}
