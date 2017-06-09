package tree;

import node.BettingNode;
import node.TerminalNode;
import abstraction.ActionAbstraction;
import acpc.Action;
import acpc.Game;
import acpc.State;


public class BettingTree {
    public BettingNode buildTree(Game game, ActionAbstraction actionAbs) throws Exception {
    	BettingNode node;
    	State state = game.state;
    	
    	if ( state.finished ) {
    		switch ( game.numPlayers ) {
    		case 2 : 
    			int showdown = ( state.playerFolded[0] || state.playerFolded[1] ) ? 0 : 1;
    			int [] foldValue = new int[2];
    			int money = -1;
    			for ( int p = 0; p < 2; ++p ) {
    				if ( state.playerFolded[p] ) {
    					foldValue[p] = -1;
    					money = state.spent[p];
    				} else if ( state.playerFolded[1-p] ) {
    					foldValue[p] = 1;
    					money = state.spent[1-p];
    				} else {
    					foldValue[p] = 0;
    					money = state.spent[p];
    				}
    			}
    			node = new TerminalNode(showdown, foldValue, money);
    			break;
    		case 3 :
    			//TODO
    			break;
    		default :
    			System.out.println("ERROR: numPlayers not support!");
    			assert (false);
    			break;
    		}
    	}
    	
    	/* Choice node.  First, compute number of different allowable actions */
    	Action [] actions = new Action[actionAbs.MAX_ABSTRACT_ACTIONS];
    	/* Next, grab the index for this node into the regrets and avg_strategy */
    	int numChoices = actionAbs.getActions(game, actions);
    	// TODO
    	long solnIdx = 0;
    	
    	BettingNode firstChild = null, lastChild = null;
    	for ( int a = 0; a < numChoices; ++a ) {
    		// create a new copy of game
    		// TODO
    		subGame.doAction(actions[a]);
    		BettingNode child = buildTree(subGame, actionAbs);
    		if ( lastChild != null ) {
    			lastChild.setSibling(child);
    		} else {
    			firstChild = child;
    		}
    		lastChild = child;
    	}
    	
    	assert ( firstChild != null );
    	assert ( lastChild != null );
    	/* Siblings are represented by a linked list,
    	 * so the last child should have no sibling
    	 */
    	lastChild.setSibling(null);
    	
    	/* Create the InfoSetNode */
    	switch ( game.numPlayers ) {
    	case 2 :
    		node =  new InfoSet(solnIdx, numChoices, game.currentPlayer(), game.state.round, firstChild);
    		break;
    	case 3 :
    		break;
    	default:
    		System.out.println("ERROR: numPlayers not support!");
			assert (false);
			break;
    	}
        return node;
    }
}