package abstraction;

import node.BettingNode;
import acpc.Game;
import acpc.Hand;
import acpc.State;

public abstract class CardAbstraction {
	public abstract int numBuckets(Game game, BettingNode node);
	public abstract int getBucket(Game game, BettingNode node, Hand hand);
	public abstract void precomputeBuckets(Game game, Hand hand);
	public abstract boolean canPrecomputeBuckets();
	public abstract int numBuckets(State state);
	public abstract int getBucket(Game game, BettingNode node,int [] boardCards, int [][] holeCards);
	
}
