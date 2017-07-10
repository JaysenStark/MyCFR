package abstraction;

import acpc.Action;
import acpc.Game;
import acpc.State;

public abstract class ActionAbstraction implements AbstractionConstants {
	public abstract int getActions(Game game, State state, Action [] actions);

	public Action [] getActions(Game game, State oldState) {
		return null;
	}
}
