package node;

public class InfoSet extends BettingNode{
	
	public int solnIdx;
	public int num_choices;
	public int player;
	public int round;
	public BettingNode child;

	public InfoSet(int solnIdx, int num_choices, int player, int round, BettingNode child) {
		this.solnIdx = solnIdx;
		this.num_choices = num_choices;
		this.player = player;
		this.round = round;
		this.child = child;
	}
	
	@Override
	public int getSolnIdx() {
		return solnIdx;
	}

	@Override
	public int getNumChoices() {
		return num_choices;
	}

	@Override
	public BettingNode getChild() {
		return child;
	}

	@Override
	public int getPlayer() {
		return player;
	}

	@Override
	public int getRound() {
		return round;
	}

}
