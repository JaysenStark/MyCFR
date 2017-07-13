package solver;

import java.io.File;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import parameter.AbsParameter;
import parameter.SolverParameter;

public class PureCFR {
	
	public static final int BLOCK_SIZE = 400000;
	public static final int NUMBER = 10;
	
	public static void main(String[] args) {
		System.out.println("start");
		
		AbsParameter absParams = new AbsParameter();
		SolverParameter solverParams = new SolverParameter(BLOCK_SIZE, 4, NUMBER);
		PureCFRMachine pcm = new PureCFRMachine(absParams, solverParams);
		runPureCFR(pcm);
		System.out.println("---------------");
		multiRunPureCFR(pcm, solverParams);
		
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
	
	public static void multiRunPureCFR(PureCFRMachine pcm, SolverParameter solverParams) {
		int parallelNumber = solverParams.getParallelNumber();
		int threadNumber = solverParams.getThreadNumber();
		int blockSize = solverParams.getBlockSize();
		
		CountDownLatch cdLatch = new CountDownLatch(threadNumber);
		pcm.setCdLatch(cdLatch);
		ExecutorService fixedThreadPool = Executors.newFixedThreadPool(parallelNumber);
		
		long startTime = System.currentTimeMillis();
		for (int th = 0; th < threadNumber; ++th) {
			fixedThreadPool.execute(pcm);
		}
		
		try {
			cdLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		fixedThreadPool.shutdown();
		long endTime = System.currentTimeMillis();
		
		System.out.println(threadNumber * blockSize + " iterations used " + (endTime - startTime) / 1000.0 + " seconds");
	}
	

}
