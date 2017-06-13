package acpc;

import java.util.Random;

public class RandomNumberGenerator {
	
	private Random random;
	
	public RandomNumberGenerator(long seed) {
		random = new Random();
		random.setSeed(seed);
	}
	

}
