package node;
import exception.NotImplementedException;


public abstract class BettingNode {
	protected int showdown; /* 1 for true */
	protected int[] fold_value = new int[2]; /* 1 for win, -1 for lose, 0 for tie */
	protected int money; 
	protected BettingNode sibling = null;
	
	public BettingNode() {
		sibling = null;
	}
	
	public long getSolnIdx(){ return 0l; };
	
	public int getNumChoices(){ return 0; };

	public BettingNode getChild(){ return null; };

	public int getRound() {	return 0; };
	
	public int getPlayer() { return 0; };
	
	public boolean didPlayerFold(int position) { return false; }
	
	public void setSibling(BettingNode sibing) {
		this.sibling = sibing;
	}
	
	public BettingNode getSibling() { return sibling; }
	
	

	/**
	 * 计算utility
	 * @param hand
	 * @param position
	 * @return
	 */
//	public int evaluate(Hand hand,int position) {
//		return 0;
//	}
		
	
}

