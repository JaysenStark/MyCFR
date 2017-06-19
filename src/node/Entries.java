package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;

import abstraction.AbstractionConstants;

public class Entries implements AbstractionConstants{
	
	public int numEntriesPerBucket;
	public int totalNumEntries;
	public int [] entries;
	
	public Entries(int numEntriesPerBucket, int totalNumEntries) {
		this.numEntriesPerBucket = numEntriesPerBucket;
		this.totalNumEntries = totalNumEntries;
		entries = new int[totalNumEntries];
	}
	
	//ADV change public to protected
	public int getEntriesIndex(final int bucket, final int solnIdx) {
		return numEntriesPerBucket * bucket + solnIdx;
	}
	
	public boolean incrementEntry(final int bucket, final int solnIdx, final int choice) {
		System.out.println("ERROR: call a not implemented function incrementEntry() on Entries!");
		assert (false);
		return false;
	}
	
	public boolean dump(DataOutputStream dis) {
		System.out.println("ERROR: call a not implemented function write() on Entries!");
		assert (false);
		return false;
	}
	
	public boolean load(DataInputStream dis) {
		System.out.println("ERROR: call a not implemented function load() on Entries!");
		assert (false);
		return false;
	}
	
	//ADV entries type not implemented yet.
	
	public int getPositiveValues(final int bucket, final int solnIdx, final int choice, int [] positiveValues) {
		System.out.println("ERROR: call a not implemented function getPositiveValue() on Entries!");
		assert (false);
		return 0;
	}
	
	public void updateRegret(final int bucket, final int solnIdx, final int numChoices, int [] values, int retVal) {
		System.out.println();
		assert (false);
	}
	
}
