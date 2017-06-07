package node;
import exception.NotImplementedException;


public abstract class BettingNode {
	protected int showdown; /* 1 for true */
	protected int[] fold_value = new int[2]; /* 1 for win, -1 for lose, 0 for tie */
	protected int money; 
	protected BettingNode sibling = null;
	
	public BettingNode() {}
	
	public int getSolnIdx(){ return 0; };
	
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
		
	public BettingNode initBettingTree(Game game, ActionAbstraction actionAbs, int[] numEntriesPerBucket) {
	    BettingNode	node;
		if(state.isFinished()){
			assert(true);
			// create termial node
			boolean showdown = state.getPlayerFolded(0) || state.getPlayerFolded(1) ? false : true;
			int[] foldValue = new int[2];
			int money = -1;
			for (int p = 0; p < Game.MAX_PLAYERS; ++p) {
				if (state.getPlayerFolded(p)) {
					foldValue[p] = -1;
					money = state.getSpent(p);
				} else if (state.getPlayerFolded(1 - p)) {
					foldValue[p] = 1;
					money = state.getSpent(1 - p);
				} else {
					//no one had fold, so in showdown stage.
					foldValue[p] = 0;
					money = state.getSpent(p);
				}
			}
			node = new TerminalNode(showdown, foldValue, money);
			return node;
		}
		
		 /* Choice node.  First, compute number of different allowable actions */
		ActionType[] actions = new ActionType[MAX_ABSTRACT_ACTIONS];
		int numChoices = actionAbs.getActions(game, state, actions);
        /* Next, grab the index for this node into the regrets and avg_strategy */
        int solnIdx=numEntriesPerBucket[state.getRound()];
        /* Update number of entries */
        numEntriesPerBucket[state.getRound()]+=numChoices;
        /* Recurse to create children */       
		BettingNode first_child = null;
		BettingNode last_child = null;
//		System.out.println(numChoices);
		for (int a = 0; a < numChoices; a++) {
			GameState newState =(GameState)state.clone();
			newState.doAction(game, actions[a]);
			BettingNode child = initBettingTree(newState, game, actionAbs, numEntriesPerBucket);
			if (last_child != null) {
				last_child.setSibling(child);
			} else {
				first_child = child;
			}
			last_child = child;
		}
		assert(last_child != null);
		last_child.setSibling(null);
		node = new InfoSetNode(solnIdx, numChoices, state.currentPlayer(), state.getRound(), first_child);
		return node;
	}
	
}

