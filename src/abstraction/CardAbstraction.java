package abstraction;

import node.BettingNode;
import acpc.Action;
import acpc.Game;
import acpc.Hand;

public abstract class CardAbstraction {
	public abstract int numBuckets(Game game, BettingNode node);
	public abstract int numBuckets(Game game);
	public abstract int getBucket(Game game, BettingNode node);
	public abstract void precomputeBuckets(Game game, Hand hand);
	public abstract boolean canPrecomputeBuckets();
}
