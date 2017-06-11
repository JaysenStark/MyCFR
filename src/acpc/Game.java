package acpc;

import java.util.Random;

import exception.TwoManyActionsException;

/**
 * @author jaysen
 *
 */
public class Game implements Cloneable {
	
	public static final int MAX_NUM_ACTIONS = 64;
	public static final int NUM_ACTION_TYPES = 3;
	public int MAX_NUM_BOARDCARDS;
	public int MAX_NUM_HOLECARDS;
	
	private Random rand = new Random();

	/* stack sizes for each player */
	public int stack[];

	/* entry fee for game, per player */
	public int blind[];

	/* size of fixed raises for limitBetting games */
	public int raiseSize[];

	/* general class of game */
	public String bettingType;

	/* number of players in the game */
	public int numPlayers;

	/* number of betting rounds */
	public int numRounds;

	/* first player to act in a round */
	public int firstPlayer[];

	/* number of bets/raises that may be made in each round */
	public int maxRaises[];

	/* number of suits and ranks in the deck of cards */
	public int numSuits;
	public int numRanks;

	/* number of private player cards */
	public int numHoleCards;

	/* number of shared public cards each round */
	public int numBoardCards [];
	
	public State state;
	
	
	
	public Game() {
		state = new State();
//		rand.setSeed(System.currentTimeMillis());
		rand.setSeed(0);
	}
	
	/* sizes[0] minRaiseSize, size[1] maxRaiseSize */
	public boolean raiseIsValid(int [] sizes) throws TwoManyActionsException {
		int p;
		if ( numRaises() >= maxRaises[state.round] ) {
			return false;
		}
		if ( state.numActions[state.round] + numPlayers > MAX_NUM_ACTIONS ) {
			System.out.println("WARINING: Two Many Actions, Forcing Call/Fold!");
		}
		if ( numActingPlayers() <= 1 ) {
			return false;
		}
		if ( bettingType != "no-limit" ) {
			/* limit betting, no need to 
			 * worry about sizes */
			sizes[0] = 0;
			sizes[1] = 0;
			return true;
		}
		//TODO
		return false;
	}
	
	public int currentPlayer() {
		if ( state.numActions[state.round] != 0 ) {
			return nextPlayer(state.actingPlayer[state.round][state.numActions[state.round] - 1 ]);
		}
		return nextPlayer( firstPlayer[state.round] + numPlayers - 1 );
	}
	
	public int nextPlayer(int curPlayer) {
		int n = curPlayer;
		do {
			n = ( n + 1 ) % numPlayers;
		} while ( state.playerFolded[n] || state.spent[n] >= stack[n] );
		return n;
	}
	
	//ADVANCE
	public int numRaises() {
		int ret = 0;
		for ( int i : state.numActions ) {
			if ( state.action[state.round][i].type == ActionType.a_raise ) {
				++ret;
			}
		}
		return ret;
	}
	
	public int numFolded() {
		int ret = 0;
		for ( int p = 0; p < numPlayers; ++p ) {
			if ( state.playerFolded[p] ) {
				++ret;
			}
		}
		return ret;
	}
	
	public int numCalled() {
		int ret = 0;
		int p;
		for (int i = state.numActions[state.round]; i > 0; --i ) {
			p = state.actingPlayer[state.round][i-1];
			if ( state.action[state.round][i-1].type == ActionType.a_raise ) {
				if ( state.spent[p] < stack[p] ) {
					++ret;
				}
				return ret;
			} else if ( state.action[state.round][i-1].type == ActionType.a_call ){
				if ( state.spent[p] < stack[p] ) {
					/* player is not all-in, so they are still acting */
					++ret;
				}
			}
		}
		return ret;
	}
	
	public int numAllIn() {
		int ret = 0;
		for ( int p = 0; p < numPlayers; ++p ) {
			if ( state.spent[p] >= stack[p] ) {
				++ret;
			}
		}
		return ret;
	}
	
	public int numActingPlayers() {
		int ret = 0;
		for ( int p = 0; p < numPlayers; ++p ) {
			if (!state.playerFolded[p]  && state.spent[p] < stack[p] ) {
				++ret;
			}
		}
		return ret;
	}
	
	public void dealCards() {
		//TODO
	}

	public boolean isValidAction(Action action, boolean tryFixing) throws Exception {
		int [] sizes = new int[2];
		if ( stateFinished() || action.type == ActionType.a_invalid ) {
			return false;
		}
		int p = currentPlayer();
		if ( action.type == ActionType.a_raise ) {
			if ( !raiseIsValid(sizes)) {
				return false;
			}
			if ( bettingType == "no-limit" ) {
				if ( action.size < sizes[0] ) {
					if ( !tryFixing ) {
						return false;
					}
					System.out.printf("WARNING: raise of %d increased to %d\n", action.size, sizes[0]);
				} else {
					;
				}
			} else if ( action.type == ActionType.a_fold ) {
				if ( state.spent[p] == state.maxSpent || state.spent[p] == stack[p] ) {
					return false;
				}
				if ( action.size != 0 ) {
					System.out.println("WARNING: size given for fold" );
					action.size = 0;
				}
			} else {
				if ( action.size != 0 ) {
					System.out.println("WARNING: size given for something other than a no-limit raise");
					action.size = 0;
				}
			}
		}
		return true;
	}

	public boolean stateFinished() {
		return state.finished;
	}
	
	public void doAction(Action action) {
		int p = currentPlayer();
		assert ( state.numActions[state.round] < MAX_NUM_ACTIONS );
		state.action[state.round][state.numActions[state.round]] = action;
		state.actingPlayer[state.round][state.numActions[state.round]] = p;
		++state.numActions[state.round];
		switch ( action.type ) {
		case a_fold :
			state.playerFolded[p] = true;
			break;
		case a_call :
			if ( state.maxSpent > stack[p] ) {
				/* calling puts player all-in */
				state.spent[p] = stack[p];
			} else {
				/* player matches the bet by spending same amount of money */
				state.spent[p] = state.maxSpent;
			}
			break;
		case a_raise:
			if ( bettingType == "no-limit" ) {
				assert( action.size > state.maxSpent );
				assert( action.size <= stack[p] );
				if ( action.size * 2 - state.maxSpent > state.minNoLimitRaiseTo ) {
					state.minNoLimitRaiseTo = action.size * 2 - state.maxSpent;
				} 
				state.maxSpent = action.size;
			} else {
				if ( state.maxSpent + raiseSize[state.round] > stack[p] ) {
					/* raise puts player all-in */
					state.maxSpent = stack[p];
				} else {
					/* player raises by the normal limit size */
					state.maxSpent += raiseSize[state.round];
				}
			}
			state.spent[p] = state.maxSpent;
			break;
		default:
			System.out.println("ERROR: trying to do invalid action!");
			assert (false);	
		}// end switch
		
		/* see if the round or game has ended */
		if ( numFolded() + 1 >= numPlayers ) {
			state.finished = true;
		} else if ( numCalled() >= numActingPlayers() ) {
			/* >= 2 non-folded players, all acting players have called */
			if ( numActingPlayers() > 1 ) {
				/* there are at least 2 acting players */
				if ( state.round + 1 < numRounds ) {
					++state.round;
					state.minNoLimitRaiseTo = 1;
					for ( p = 0; p < numPlayers; ++p ) {
						if ( blind[p] > state.minNoLimitRaiseTo ) {
							state.minNoLimitRaiseTo = blind[p];
						}
					}
					/* we finished at least one round, so raise-to = raise-by + maxSpent */
					state.minNoLimitRaiseTo += state.maxSpent;
				} else {
					/* no more betting rounds, so we're totally finished */
					state.finished = true;
				}
			} else {
				/* not enough players for more betting, but still need a showdown */
				state.finished = true;
				state.round = numRounds - 1;
			}
		}
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Game newGame = (Game) super.clone();
		return newGame;
	}
	
	public static void main(String[] args) {
		Game game = new Game();
		game.state.initState(game, 0);
		Game newGame = null;
		try {
			newGame = (Game) game.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		
		assert (newGame.state != game.state);
		System.out.println("end");
	}
	
}
