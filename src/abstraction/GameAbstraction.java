package abstraction;

import exception.NotSupportParameterException;
import acpc.Game;
import acpc.KuhnGame;
import parameter.AbsParameter;

public class GameAbstraction {
	
	public Game game;
	public ActionAbstraction actionAbs;
	public long [] numEntriesPerBucket;
	
	public GameAbstraction(AbsParameter param) throws Exception {
		
		/* choose game type */
		switch (param.gameType) {
		case "Kuhn":
			game = new KuhnGame();
			break;
		case "limit Texas":
			//TODO
			break;
		case "no-limit Texas":
			//TODO
			break;
		default:
			throw new NotSupportParameterException("Game Type Not Supported!");
		}
		
		/* choose action abstraction type */
		switch (param.actionAbsType) {
		case "NullActionAbstraction":
			//actionAbs = ;TODO
			break;
		case "FcpaActionAbstraction":
			//TODO
			break;
		default:
			throw new NotSupportParameterException("Action Abstraction Parameter Not Supported!"); 
		}
		
		/* init num_entries_per_bucket to zero */
		numEntriesPerBucket = new long[game.numRounds];
		
		/* build betting tree */
		//root = TODO 
		
	}
	
}
