package solver;

import java.io.File;
import java.util.Random;

import parameter.AbsParameter;

public class PureCFR {
	
	public static final int BLOCK_SIZE = 40000000;
	public static final int NUMBER = 1;
	
	public static void main(String[] args) {
		System.out.println("start");
		Random random = new Random();
		random.setSeed(0);
		AbsParameter params = new AbsParameter();
		PureCFRMachine pcm = new PureCFRMachine(params);
		runPureCFR(pcm, random);
		System.out.println("end");
	}
	
	/* 
	 * do NUMBER * BLOCKSIZE iterations, dump regret and strategy for every block size
	 */
	public static void runPureCFR(PureCFRMachine pcm,final Random random) {
		int count = 0;
		int offset = 0;
		
		for ( int i = 0; i < NUMBER * BLOCK_SIZE; ++i ) {
			pcm.doIteration(random);
			count++;
			if ( count == BLOCK_SIZE) {
				count = 0;
				offset += BLOCK_SIZE;
				
				pcm.dumpRegret(new File("iter-" + (offset + count) + ".regret"));
				pcm.dumpStrategy(new File("iter-" + (offset + count) + ".avgStrategy"));
			}
		}
		
		
	}
	

}
