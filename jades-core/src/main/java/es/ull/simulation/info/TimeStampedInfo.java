package es.ull.simulation.info;

/**
 * A piece of information that has a timestamp associated to it
 */
public abstract class TimeStampedInfo implements IPieceOfInformation {
	/** The timestamp of the information */
	final protected long ts;
	
	/**
	 * Creates a new piece of information with a timestamp
	 * @param ts Timestamp
	 */
	public TimeStampedInfo(final long ts) {
		this.ts = ts;
	}

	/**
	 * Returns the timestamp of the information
	 * @return Timestamp
	 */
	public long getTs() {
		return ts;
	}

}
