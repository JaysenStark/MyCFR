package abstraction;

import acpc.Action;
import acpc.Game;

public abstract class ActionAbstraction implements AbstractionConstants {
	public abstract int getActions(Game game, Action [] actions) throws Exception;
}
