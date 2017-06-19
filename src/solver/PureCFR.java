package solver;

import java.io.File;
import java.util.Random;

import parameter.AbsParameter;

public class PureCFR {
	
	public static final int BLOCK_SIZE = 10000;
	public static final int NUMBER = 4;
	
	public static void main(String[] args) {
		Random random = new Random();
		random.setSeed(0);
		AbsParameter params = new AbsParameter();
		PureCFRMachine pcm = new PureCFRMachine(params);
		runPureCFR(pcm, random);
		System.out.println("end");
	}
	
	public static void runPureCFR(PureCFRMachine pcm, Random random) {
		int count = 0;
		int offset = 0;
		
		for ( int i = 0; i < NUMBER * BLOCK_SIZE; ++i ) {
			pcm.doIteration(random);
			count++;
			if ( count == BLOCK_SIZE) {
				count = 0;
				offset += BLOCK_SIZE;
				String suffix = ".regret";
				String prefix = "iter-";
				String filename = prefix + (offset + count) + suffix;
				File file = new File(filename);
				pcm.dumpRegret(file);
			}
		}
//		pcm.loadRegret("iter-10000.regret");
		
	}
	

}
