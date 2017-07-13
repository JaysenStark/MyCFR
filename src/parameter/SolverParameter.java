package parameter;

public class SolverParameter {
	// a block size for a thread
	private final int blockSize;
	private final int parallelNumber;
	private final int threadNumber;

	// default constructor
	public SolverParameter() {
		this.blockSize = 400000;
		this.parallelNumber = Runtime.getRuntime().availableProcessors();
		this.threadNumber = 10;
	}

	public SolverParameter(int blockSize, int parallelNumber, int threadNumber) {
		this.blockSize = blockSize;
		this.parallelNumber = parallelNumber;
		this.threadNumber = threadNumber;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getThreadNumber() {
		return threadNumber;
	}

	public int getParallelNumber() {
		return parallelNumber;
	}

}