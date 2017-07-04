package agent;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import parameter.AbsParameter;

import node.BettingNode;
import node.EntriesLoader;

import abstraction.AbstractionConstants;
import abstraction.GameAbstraction;
import acpc.Action;
import acpc.ActionType;
import acpc.State;

public class PlayerModule {
	protected GameAbstraction gameAbs;
	protected boolean verbose;
	public EntriesLoader[] entries;
	private final Random random = new Random();

	public PlayerModule(File file) {
		random.setSeed(0);
		verbose = false;

		// choose abstraction parameter
		// currently use default kuhn game, null abstraction
		AbsParameter params = new AbsParameter();

		/* Initialize abstract game */
		gameAbs = new GameAbstraction(params);

		/*
		 * Next, count the number of entries required per round to store the
		 * entries
		 */
		int MAX_ROUNDS = gameAbs.game.numRounds;
		int[] numEntriesPerBucket = new int[MAX_ROUNDS];
		int[] totalNumEntries = new int[MAX_ROUNDS];
		entries = new EntriesLoader[MAX_ROUNDS];
		gameAbs.countEntries(numEntriesPerBucket, totalNumEntries);

		// initialize entries array
		for (int i = 0; i < MAX_ROUNDS; ++i) {
			entries[i] = new EntriesLoader(numEntriesPerBucket[i],
					totalNumEntries[i]);
		}

		// file that data will be loaded from
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		DataInputStream dis = new DataInputStream(is);

		/* Finally, build the entries from the dump */
		for (int r = 0; r < MAX_ROUNDS; ++r) {
			entries[r].load(dis);
		}

		try {
			dis.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * first walk abstract tree according to actions, reach a node which
	 * represents current game state; then get bucket according to node and
	 * state (hole, board); finally get entries according to bucket, then
	 * generate strategy by scaling its sum to 1
	 */
	public double[] getActionProbs(State state, int bucket) {
		BettingNode node = gameAbs.root;
		State oldState = new State();
		oldState.initState(gameAbs.game, 0);

		// walk abstract tree according to actions and its
		// mappings[real->abstract]
		for (int r = 0; r <= state.round; ++r) {
			for (int a = 0; a < state.numActions[r]; ++a) {
				Action realAction = state.action[r][a];
				Action[] absActions = gameAbs.actionAbs.getActions(
						gameAbs.game, oldState);
				int numActions = absActions.length;
				Action realToAbsAction = null;
				// if action number doesn't matches
				if (numActions != node.getNumChoices()) {
					if (verbose) {
						System.out
								.println("Number of actions does not match number of choices");
						System.exit(-1);
					}
				}

				//
				int choice = -1;
				if ((gameAbs.game.bettingType == "no-limit")
						&& (realAction.type == ActionType.a_raise)) {
					System.out.println("no-limit raise not supported yet!");
				} else {
					/*
					 * Limit game or non-raise action. Just match the real
					 * action.
					 */
					for (choice = 0; choice < absActions.length; ++choice) {
						if (absActions[choice].type == realAction.type) {
							realToAbsAction = absActions[choice];
							break;
						}
					}

					if (realToAbsAction == null) {
						System.out
								.println("ERROR: can't translate real action to abstract action!");
						System.exit(-1);
					}

					// move node pointer then doAction on state
					node = node.getChild();
					for (int i = 0; i < choice; ++i) {
						node = node.getSibling();
						if (node == null) {
							System.out
									.println("Error: walk abstract tree error!");
							System.exit(-1);
						}
					}
					gameAbs.game.doAction(oldState, realToAbsAction);
				} // end else
			}
		} // end outer for loop

		/* Bucket the cards */
		if (bucket == -1) {
			// ADV
			bucket = gameAbs.cardAbs.getBucket(gameAbs.game, node,
					state.boardCards, state.holeCards);
		}
		if (state.round != node.getRound()) {
			System.out.println("Abstract round does not match current round");
			System.exit(-1);
		}

		/* Get the positive entries at this information set */
		int numChoices = node.getNumChoices();
		int solnIdx = node.getSolnIdx();
		int round = node.getRound();
		int[] posEntries = new int[numChoices];
		double sumPosEntries = this.entries[round].getPositiveValues(bucket,
				solnIdx, numChoices, posEntries);

		/* Get the abstract game action probabilities */
		if (sumPosEntries == 0.0) {
			if (verbose) {
				System.out.println("All positive entries are zero!");
			}
			System.exit(-1);
		}

		// generate strategy
		double[] actionProbs = new double[numChoices];
		for (int c = 0; c < numChoices; ++c) {
			actionProbs[c] = posEntries[c] / sumPosEntries;
		}
		return actionProbs;
	}

	protected void getDefaultActionProbs(State state, double[] actionProbs) {
		final int MAX_ABSTRACT_ACTIONS = AbstractionConstants.MAX_ABSTRACT_ACTIONS;

		/* Default will be always call */
		Arrays.fill(actionProbs, 0.0);

		/* Get the abstract actions */

		Action[] actions = new Action[MAX_ABSTRACT_ACTIONS];
		int numChoices = gameAbs.actionAbs.getActions(gameAbs.game, state,
				actions);

		/* Find the call action */
		for (int a = 0; a < numChoices; ++a) {
			if (actions[a].type == ActionType.a_call) {
				actionProbs[a] = 1.0;
				return;
			}
		}

		/*
		 * Still haven't returned? This means we couldn't find a call action, so
		 * we must be dealing with a very weird action abstraction. Let's just
		 * always play the first action then by default.
		 */
		actionProbs[0] = 1.0;
	}

	public void getActionProbs(State state, double[] actionProbs, int bucket) {
		final int MAX_ABSTRACT_ACTIONS = AbstractionConstants.MAX_ABSTRACT_ACTIONS;

		/*
		 * Initialize action probs to the default in case we must abort early
		 * for one of several reasons
		 */
		getDefaultActionProbs(state, actionProbs);

		/* Find the current node from the sequence of actions in state */
		BettingNode node = gameAbs.root;
		State oldState = new State();
		oldState.initState(gameAbs.game, 0);

		if (verbose) {
			System.out.println("Tranlated abstract state:");

		}

		for (int r = 0; r <= state.round; ++r) {
			for (int a = 0; a < state.numActions[r]; ++a) {
				Action realAction = state.action[r][a];
				Action[] abstractAction = new Action[MAX_ABSTRACT_ACTIONS];
				int numAction = gameAbs.actionAbs.getActions(gameAbs.game,
						oldState, abstractAction);

				if (numAction != node.getNumChoices()) {
					if (verbose) {
						System.out
								.println("Number of actions does not match number of choices");
					}
					return;
				}
				int choice;
				if ((gameAbs.game.bettingType == "no-limit")
						&& (realAction.type == ActionType.a_raise)) {
					// noLimit
					/*
					 * First, find the smallest abstract raise greater than or
					 * equal to the real raise size (upper), and the largest
					 * abstract raise less than or equal to the real raise size
					 * (lower).
					 */

				}// nolimit endif
				else {
					/*
					 * Limit game or non-raise action. Just match the real
					 * action.
					 */
					for (choice = 0; choice < numAction; ++choice) {
						if (abstractAction[choice].type == realAction.type) {
							break;
						}
					}

					if (choice >= numAction) {
						if (verbose) {
							System.out
									.println("Unable to translate action at round "
											+ state.round
											+ " turn "
											+ state.actingPlayer[r][a]);
						}
						return;
					}

					/* Move the current node and old_state along */
					node = node.getChild();
					for (int i = 0; i < choice; ++i) {
						node = node.getSibling();
						if (node == null) {
							if (verbose) {
								System.out
										.println("Ran out of sibings for choice: "
												+ choice);
							}
							return;
						}
					}

					if (node.getChild() == null) {
						if (verbose) {
							System.out.println("Abstract game over");
						}
						return;
					}
					gameAbs.game.doAction(oldState, abstractAction[choice]);

				}// limit elseend
			}
		}

		/* Bucket the cards */
		if (bucket == -1) {
			bucket = gameAbs.cardAbs.getBucket(gameAbs.game, node,
					state.boardCards, state.holeCards);
		}
		if (verbose) {
			System.out.println(" Bucket=" + bucket);
		}

		/* Check for problems */
		// if (state.getActingPlayer(round, action) != node->get_player()) {
		// if (verbose)
		// {
		// fprintf(stderr, "Abstract player does not match current player\n");
		// }
		// return;
		// }
		if (state.round != node.getRound()) {
			if (verbose) {
				System.out
						.println("Abstract round does not match current round");
			}
			return;
		}

		/* Get the positive entries at this information set */
		int numChoices = node.getNumChoices();
		int solnIdx = node.getSolnIdx();
		int round = node.getRound();
		int[] posEntries = new int[numChoices];
		double sumPosEntries = this.entries[round].getPositiveValues(bucket,
				solnIdx, numChoices, posEntries);

		/* Get the abstract game action probabilities */
		if (sumPosEntries == 0.0) {
			if (verbose) {
				System.out.println("All positive entries are zero!");
			}
			return;
		}
		for (int c = 0; c < numChoices; ++c) {
			actionProbs[c] = posEntries[c] / sumPosEntries;
		}
	}

	public Action getAction(State state) {
		final int MAX_ABSTRACT_ACTIONS = AbstractionConstants.MAX_ABSTRACT_ACTIONS;

		/* Get the abstract game action probabilities */
		double[] actionProbs = new double[MAX_ABSTRACT_ACTIONS];
		getActionProbs(state, actionProbs, -1);

		/* Get the corresponding actions */
		Action[] actions = new Action[MAX_ABSTRACT_ACTIONS];
		int numChoices = gameAbs.actionAbs.getActions(gameAbs.game, state,
				actions);

		/* Choose an action */
		double dart = random.nextDouble();
		int a;
		for (a = 0; a < numChoices - 1; ++a) {
			if (dart < actionProbs[a]) {
				break;
			}
			dart -= actionProbs[a];
		}
		if (verbose) {
			// TODO print action
		}

		/* Make sure action is legal */
		if (!gameAbs.game.isValidAction(state, actions[a], false)) {
			if (verbose) {
				System.out.println("Action chosen is not legal");
			}
		}
		//
		if (actions[a].type == ActionType.a_fold) {
			System.out.println("f");
		}
		//
		return actions[a];
	}

}