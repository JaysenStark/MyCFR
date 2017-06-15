package node;
import acpc.Hand;
import exception.NotImplementedException;


public abstract class BettingNode {
	protected boolean showdown; /* 1 for true */
	protected int[] foldValue = new int[2]; /* 1 for win, -1 for lose, 0 for tie */
	protected int money; 
	protected BettingNode sibling = null;
	
	public BettingNode() {
		sibling = null;
	}
	
	public int getSolnIdx(){ return 0; };
	
	public int getNumChoices(){ return 0; };

	public BettingNode getChild(){ return null; };

	public int getRound() {	return 0; };
	
	public int getPlayer() { return 0; };
	
	public boolean didPlayerFold(int position) { return false; }
	
	public void setSibling(BettingNode sibling) { this.sibling = sibling; }
	
	public BettingNode getSibling() { return sibling; }
	
	public int evaluate(Hand hand,int position) { return 0; }
		
	
}

