package acpc;

import java.util.Random;

import exception.TooManyActionsException;

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
	
//	public State state;
	
	
	
	public Game() {
//		state = new State();
//		rand.setSeed(System.currentTimeMillis());
		rand.setSeed(0);
	}
	
	/* sizes[0] minRaiseSize, size[1] maxRaiseSize */
	public boolean raiseIsValid(State state, int [] sizes) {
		
		if ( numRaises(state) >= maxRaises[state.round] ) {
			return false;
		}
		if ( state.numActions[state.round] + numPlayers > MAX_NUM_ACTIONS ) {
			System.out.println("WARINING: Two Many Actions, Forcing Call/Fold!");
		}
		if ( numActingPlayers(state) <= 1 ) {
			return false;
		}
		if ( bettingType != "no-limit" ) {
			/* limit betting, no need to 
			 * worry about sizes */
			sizes[0] = 0;
			sizes[1] = 0;
			return true;
		}
		
		int p = currentPlayer(state);
		sizes[0] = state.minNoLimitRaiseTo;
		sizes[1] = stack[p];
		
		/* handle case where remaining player stack is too small */
		if ( sizes[0] > stack[p] ) {
			/* can't handle the minimum bet size - can we bet at all? */
			if ( state.maxSpent >= stack[p] ) {
				/* not enough money to increase current bet */
				return false;
			} else {
				/* can raise by going all-in */
				sizes[0] = sizes[1];
				return true;
			}
		}
		
		return true;
	}
	
	public int currentPlayer(State state) {
		if ( state.numActions[state.round] != 0 ) {
			return nextPlayer(state, state.actingPlayer[state.round][state.numActions[state.round] - 1 ]);
		}
		return nextPlayer(state, firstPlayer[state.round] + numPlayers - 1 );
	}
	
	public int nextPlayer(State state, int curPlayer) {
		int n = curPlayer;
		do {
			n = ( n + 1 ) % numPlayers;
		} while ( state.playerFolded[n] || state.spent[n] >= stack[n] );
		return n;
	}
	
	//ADVANCE
	public int numRaises(State state) {
		int ret = 0;
		for ( int i : state.numActions ) {
			if (state.action[state.round][i] != null ) {
				if ( state.action[state.round][i].type == ActionType.a_raise ) {
					++ret;
				}
			}
		}
		return ret;
	}
	
	public int numFolded(State state) {
		int ret = 0;
		for ( int p = 0; p < numPlayers; ++p ) {
			if ( state.playerFolded[p] ) {
				++ret;
			}
		}
		return ret;
	}
	
	public int numCalled(State state) {
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
	
	public int numAllIn(State state) {
		int ret = 0;
		for ( int p = 0; p < numPlayers; ++p ) {
			if ( state.spent[p] >= stack[p] ) {
				++ret;
			}
		}
		return ret;
	}
	
	public int numActingPlayers(State state) {
		int ret = 0;
		for ( int p = 0; p < numPlayers; ++p ) {
			if (!state.playerFolded[p]  && state.spent[p] < stack[p] ) {
				++ret;
			}
		}
		return ret;
	}
	
	public int dealCard(final Random random, int [] deck, int numCards) {
		int ret = -1;
		int i = random.nextInt(numCards) % numCards;
		ret = deck[i];
		deck[i] = deck[numCards - 1];
		return ret;
	}
	
	public void dealCards(final Random random, State state) {
		int numCards = 0;
		int [] deck = new int[numRanks * numSuits];
		/* create deck first */
		for ( int s = 0; s < numSuits; ++s ) {
			for ( int r = 0; r < numRanks; ++r ) {
				deck[numCards] = Card.makeCard(r, s);
				++numCards;
			}
		}
		/* deal hole cards for each player */
		for ( int p = 0; p < numPlayers; ++p ) {
			for ( int i = 0; i < numHoleCards; ++i ) {
				state.holeCards[p][i] = dealCard(random, deck, numCards);
				--numCards;
			}
		}
		/* deal public cards */
		int s = 0;
		for ( int r = 0; r < numRounds; ++r ) {
			for ( int i = 0; i < numBoardCards[r]; ++i ) {
				state.boardCards[s] = dealCard(random, deck, numCards);
				--numCards;
				++s;
			}
		}
	}

	public boolean isValidAction(State state, Action action, boolean tryFixing) {
		int [] sizes = new int[2];
		if ( stateFinished(state) || action.type == ActionType.a_invalid ) {
			return false;
		}
		int p = currentPlayer(state);
		if ( action.type == ActionType.a_raise ) {
			if ( !raiseIsValid(state, sizes)) {
				/* there are no valid raise sizes */
				return false;
			}
			
			if ( bettingType == "no-limit" ) {
				/* no limit games have a size */
				if ( action.size < sizes[0] ) {
					if ( !tryFixing ) {
						return false;
					}
					System.out.printf("WARNING: raise of %d increased to %d\n", action.size, sizes[0]);
					action.size = sizes[0];
				} else if ( action.size > sizes[1] ) {
					/* bet size is too big */
					if ( !tryFixing ) {
						return false;
					}
					System.out.printf("WARNING: raise of %d decreased to %d\n", action.size, sizes[1]);
					action.size = sizes[1];
				}
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
		return true;
	}

	public boolean stateFinished(State state) {
		return state.finished;
	}
	
	public void doAction(State state, Action action) {
		int p = currentPlayer(state);
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
				/* limit */
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
		if ( numFolded(state) + 1 >= numPlayers ) {
			state.finished = true;
		} else if ( numCalled(state) >= numActingPlayers(state) ) {
			/* >= 2 non-folded players, all acting players have called */
			if ( numActingPlayers(state) > 1 ) {
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
	public Object clone() {
		Game newGame = null;
		try {
			newGame = (Game) super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("ERROR: game object clone fails!");
			e.printStackTrace();
		}
//		newGame.state = (State) state.clone();
		return newGame;
	}
	
	public static void main(String[] args) {
//		Game game = new Game();
//		game.state.initState(game, 0);
//		Game newGame = null;
//		
//		newGame = (Game) game.clone();
//		
//		assert (newGame.state != game.state);
//		System.out.println("end");
	}

	/* board card start index */
	public int bcStart(int round) {
		int start = 0;
		for ( int r = 0; r < round; ++r ) {
			start += numBoardCards[r];
		}
		return start;
	}

	public int sumBoardCards(int round) {
		int total = 0;
		for ( int r = 0; r <= round; ++r ) {
			total += numBoardCards[r];
		}
		return total;
	}
	
}
