package abstraction;

import acpc.Action;
import acpc.ActionType;
import acpc.Game;

public class NullActionAbstraction extends ActionAbstraction implements AbstractionConstants {

	@Override
	public int getActions(Game game, Action[] actions) throws Exception {
		int numActions = 0;
		boolean error = false;
		ActionType [] types = ActionType.values();
		for ( ActionType type : types ) {
			Action action = new Action(type, 0);
			if ( type == ActionType.a_invalid ) {
				continue;
			}
			if ( type == ActionType.a_raise ) {
				int [] sizes = new int[2];
				if ( game.raiseIsValid(sizes) ) {
					if ( numActions + sizes[1] - sizes[0] + 1 > MAX_ABSTRACT_ACTIONS ) {
						error = true;
						break;
					}
					for ( int s = sizes[0]; s < sizes[1]; ++s ) {
						actions[numActions] = new Action(type, s);
						++numActions;
					}
				}	
			} else if ( game.isValidAction(action, false) ) {
				/* If you hit this assert, there are too many abstract actions allowed.
				  * Either coarsen the betting abstraction or increase MAX_ABSTRACT_ACTIONS
				  * in constants.hpp
				*/
				if ( numActions >= MAX_ABSTRACT_ACTIONS ) {
					error = true;
					break;
				}
				actions[numActions] = action;
				++numActions;
			}
		}// end for
		assert (!error);		
		return numActions;
	}

}
