package node;

import java.io.File;

public class EntriesLoader extends Entries {
	
	public EntriesLoader(int numEntriesPerBucket, int totalNumEntries) {
		super(numEntriesPerBucket, totalNumEntries);
	}
	
	/* return true for success, false for overflow */
	/* attention, this is in contrary to the c++ implementation */
	@Override
	public boolean incrementEntry(final int bucket, final int solnIdx, final int choice) {
		int baseIndex = getEntriesIndex(bucket, solnIdx);
		entries[baseIndex + choice] += 1;
		if ( entries[baseIndex + choice] <= 0 ) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean write(File file) {
		return true;
	}
	
	@Override
	public boolean load(File file) {
		return true;
	}
	
	/* Returns the sum of all pos_values in the returned pos_values array */
	@Override
	public int getPositiveValues(final int bucket, final int solnIdx, final int numChoices, int [] positiveValues) {
		/* Get the local entries at this index */
		int baseIndex = getEntriesIndex(bucket, solnIdx);
		int [] localEntries = new int[numChoices];
		int sumValues = 0;
		/* Zero out negative values and store in the returned array */
		for ( int c = 0; c < numChoices; ++c ) {
			localEntries[c] = (entries[baseIndex + c] > 0)? entries[baseIndex + c] : 0;
			positiveValues[c] = localEntries[c];
			sumValues += localEntries[c];
		}
		return sumValues;
	}
	
	@Override
	public void updateRegret(final int bucket, final int solnIdx, final int numChoices, int [] values, int retVal) {
		int baseIndex = getEntriesIndex(bucket, solnIdx);
		
		for ( int c = 0; c < numChoices; ++c ) {
			int diff = values[c] - retVal;
			int newRegret = entries[baseIndex + c] + diff;
			/* Only update regret if no overflow occurs */
			if ( ( (diff < 0) && (newRegret < entries[baseIndex + c]) ) || ( (diff > 0) && (newRegret > entries[baseIndex + c]) ) ) {
				entries[baseIndex + c] = newRegret;
			}
		}
	}
	
}
