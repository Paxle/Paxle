package org.paxle.core.threading;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class PPM {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Semaphore used for the PPM update and calculation
	 */
	protected Semaphore ppmsm = new Semaphore(1, true);	
	
	/**
	 * This list stores the number of elements processed since the last 
	 * @see getCleanPPM()
	 */
	private LinkedList<Long> ppm = new LinkedList<Long>();

	public void trick() throws InterruptedException {
		//add the job to the total job-count and the PPM
		this.ppmsm.acquire();
		this.ppm.addLast(System.currentTimeMillis());
		this.ppmsm.release();
		
		//Nobody should have more than 200 PPS, so we clean the DB if it gets to big after some time to save memory
		if (this.ppm.size() > 12000) this.getCleanPPM();		
	}
	
	/**
	 * Cleans all entries from the PPM-DB that are older than 1 minute.
	 * @return The number of files processed in the last minute
	 */
	private int getCleanPPM() {
		//the timestamp 60 seconds ago
		long maxage = System.currentTimeMillis();
		maxage = maxage - 60000;
		
		try {
			this.ppmsm.acquire();
		} catch (InterruptedException e) {
			//Should only occur on program shutdown
		}
		
		while (this.ppm.size() > 0 && this.ppm.getFirst() < maxage) {
			this.ppm.removeFirst();
		}
		
		//store in special variable so the value is not altered after semaphore release 
		int retval = this.ppm.size();
		this.ppmsm.release();
		return retval;
	}
	
	public int getPPM() {
		return (this.getCleanPPM());
	}
	
	public void reset() {
		try {
			this.ppmsm.acquire();
			this.ppm.clear();
			this.ppmsm.release();			
		} catch (InterruptedException e) {
			//Should only occur on program shutdown
		}
	}
}
