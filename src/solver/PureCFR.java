package solver;

import java.io.File;
import java.util.Random;

import parameter.AbsParameter;
import parameter.SolverParameter;

public class PureCFR {
	
	public static final int BLOCK_SIZE = 400000;
	public static final int NUMBER = 10;
	
	public static void main(String[] args) {
		System.out.println("start");
		Random random = new Random();
		random.setSeed(0);
		AbsParameter params = new AbsParameter();
		SolverParameter solverParams = new SolverParameter(BLOCK_SIZE, 4, NUMBER);
		PureCFRMachine pcm = new PureCFRMachine(params, solverParams);
		runPureCFR(pcm);
		System.out.println("end");
	}
	
	/* 
	 * do NUMBER * BLOCKSIZE iterations, dump regret and strategy for every block size
	 */
	public static void runPureCFR(PureCFRMachine pcm) {
		int count = 0;
		int offset = 0;
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < NUMBER * BLOCK_SIZE; ++i) {
			pcm.doIteration();
			count++;
			if (count == BLOCK_SIZE) {
				count = 0;
				offset += BLOCK_SIZE;

				pcm.dumpRegret(new File("iter-" + (offset + count) + ".regret"));
				pcm.dumpStrategy(new File("iter-" + (offset + count) + ".avgStrategy"));
			}
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println(NUMBER * BLOCK_SIZE + " iterations used " + (endTime - startTime) / 1000.0 + " seconds");
	}
	

}
