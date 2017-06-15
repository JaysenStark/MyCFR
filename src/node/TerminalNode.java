package node;

import acpc.Hand;

public class TerminalNode extends BettingNode {
	
	public TerminalNode(boolean showdown, int [] foldValue, int money) {
		this.showdown = showdown;
		this.foldValue = foldValue;
		this.money = money;
	}
	
	@Override
	public int evaluate(Hand hand, int position) {
		return (showdown ? hand.showdownValue[position] : foldValue[position]) * money;
	}
}
