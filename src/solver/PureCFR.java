package solver;

import java.util.Random;

import parameter.AbsParameter;

public class PureCFR {
	
	public static void main(String[] args) {
		Random random = new Random();
		random.setSeed(0);
		AbsParameter params = new AbsParameter();
		PureCFRMachine pcm = new PureCFRMachine(params);
		for ( int i = 0; i < 999999; ++i ) {
			pcm.doIteration(random);
		}
		System.out.println("end");
	}

}
