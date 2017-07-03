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


public class PlayerModule{
    protected GameAbstraction gameAbs;
    protected boolean verbose;
    public EntriesLoader [] entries;
    private final Random random = new Random();
    
    public PlayerModule(File file) {
    	random.setSeed(0);
        this.verbose = true;
        
        // choose abstraction parameter
        // currently use default kuhn game, null abstraction
        
        AbsParameter params = new AbsParameter();
        
        /* Initialize abstract game*/
        this.gameAbs = new GameAbstraction(params);
        
        /* Next, count the number of entries required per round to store the entries */
        int MAX_ROUNDS = gameAbs.game.numRounds;
        int[] numEntriesPerBucket = new int[MAX_ROUNDS];
        int[] totalNumEntries = new int[MAX_ROUNDS];
        entries = new EntriesLoader[MAX_ROUNDS];
        gameAbs.countEntries(numEntriesPerBucket, totalNumEntries);
        
        
        for ( int i = 0; i < MAX_ROUNDS; ++i ) {
        	entries[i] = new EntriesLoader(numEntriesPerBucket[i], totalNumEntries[i]);
        }
        
        assert (entries.length == 1);
        
        FileInputStream is = null;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
        DataInputStream dis = new DataInputStream(is);
        /* Finally, build the entries from the dump */
        for (int r = 0; r < MAX_ROUNDS; ++r) {
            assert (r < gameAbs.game.numRounds);
            /* Establish entries for this round and move dump pointer to next set of
             * entries.
             */
            EntriesLoader el = new EntriesLoader(numEntriesPerBucket[r], totalNumEntries[r]);
            el.load(dis);
            entries[r] = el;

            if (entries[r] == null) {
            	System.out.println( "Could not load entries for round " + r);
            	System.exit(1);
            }
        }
    
    }
    
    protected void getDefaultActionProbs(State state, double[] actionProbs) {
    	final int MAX_ABSTRACT_ACTIONS = AbstractionConstants.MAX_ABSTRACT_ACTIONS;
    	
        /* Default will be always call */
        Arrays.fill(actionProbs, 0.0);
        
        /* Get the abstract actions */

        Action [] actions = new Action[MAX_ABSTRACT_ACTIONS];
        int numChoices = gameAbs.actionAbs.getActions(gameAbs.game, state, actions);
        
        /* Find the call action */
        for( int a = 0; a < numChoices; ++a ) {
            if( actions[a].type == ActionType.a_call ) {
              actionProbs[ a ] = 1.0;
              return;
            }
        }

        /* Still haven't returned?  This means we couldn't find a call action,
           * so we must be dealing with a very weird action abstraction.
           * Let's just always play the first action then by default.
           */
        actionProbs[0] = 1.0;
    }
    
    public void getActionProbs(State state, double[] actionProbs, int bucket) {
    	final int MAX_ABSTRACT_ACTIONS = AbstractionConstants.MAX_ABSTRACT_ACTIONS;
    	
        /* Initialize action probs to the default in case we must abort early
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
        
        for ( int r = 0; r <= state.round ; ++r ) {
            for ( int a = 0; a < state.numActions[r]; ++a ) {
                Action realAction = state.action[r][a];
                Action [] abstractAction = new Action[MAX_ABSTRACT_ACTIONS];
                int numAction = gameAbs.actionAbs.getActions(gameAbs.game, oldState, abstractAction);
                
                if (numAction != node.getNumChoices()) {
                    if (verbose) {
                        System.out.println("Number of actions does not match number of choices");
                    }
                    return;
                }
                int choice;
                if ( ( gameAbs.game.bettingType == "no-limit" ) && ( realAction.type == ActionType.a_raise ) ) {
                    // noLimit
                    /* First, find the smallest abstract raise greater than or equal to the
                     * real raise size (upper), and the largest abstract raise less than or
                     * equal to the real raise size (lower).
                     */
                    
                }//nolimit endif
                else {
                    /* Limit game or non-raise action. Just match the real action. */
                    for (choice = 0; choice < numAction; ++ choice) {
                        if (abstractAction[choice].type == realAction.type) {
                            break;
                        }
                    }
                    
                    if (choice >= numAction) {
                        if (verbose) {
                            System.out.println("Unable to translate action at round " + state.round + 
                                        	   " turn " + state.actingPlayer[r][a] );
                        }
                        return;
                    }
                    
                    /* Move the current node and old_state along */
                    node = node.getChild();
                    for (int i = 0; i < choice; ++ i) {
                        node = node.getSibling();
                        if (node == null) {
                            if (verbose) {
                                System.out.println("Ran out of sibings for choice: " + choice);
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
                    
                    
                }//limit elseend
            }
        }
        
        /* Bucket the cards */
          if (bucket == -1) {
            bucket = gameAbs.cardAbs.getBucket(gameAbs.game, node, state.boardCards, state.holeCards);
          }
          if (verbose)  {
              System.out.println(" Bucket=" + bucket);
          }

          /* Check for problems */
//        if (state.getActingPlayer(round, action) != node->get_player()) {
//          if (verbose)
//          {
//            fprintf(stderr, "Abstract player does not match current player\n");
//          }
//          return;
//        }
          if (state.round != node.getRound()) {
            if (verbose) {
              System.out.println("Abstract round does not match current round");
            }
            return;
          }
          
          /* Get the positive entries at this information set */
          int numChoices = node.getNumChoices();
          int solnIdx = node.getSolnIdx();
          int round = node.getRound();
          int [] posEntries = new int[numChoices];
          double sumPosEntries = this.entries[round].getPositiveValues(bucket, solnIdx, numChoices, posEntries);
          
          /* Get the abstract game action probabilities */
          if (sumPosEntries == 0.0) {
              if (verbose) {
                  System.out.println("All positive entries are zero!");
              }
              return;
          }
          for (int c = 0; c < numChoices; ++ c) {
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
        int numChoices = gameAbs.actionAbs.getActions(gameAbs.game, state, actions);
        if (verbose) {
            System.out.println("probs:todo");
            for (int a = 0; a < numChoices; ++a) {
                if ((numChoices < 5) || (actionProbs[a] > 0.001)) {
                    //TODO printAction
                }
            }
        }
        
        /* Choose an action */
        double dart = random.nextDouble();
        int a;
        for (a = 0; a < numChoices - 1; ++ a) {
            if (dart < actionProbs[a]) {
                break;
            }
            dart -= actionProbs[a];
        }
        if (verbose) {
            //TODO print action
        }
        
        /* Make sure action is legal */
        if (!gameAbs.game.isValidAction(state, actions[a], false)) {
            if (verbose) {
                System.out.println("Action chosen is not legal");
            }
        }
        //
        if( actions[a].type == ActionType.a_fold ) {
            System.out.println("f");
        }
        //
        return actions[a];
    }
    
}