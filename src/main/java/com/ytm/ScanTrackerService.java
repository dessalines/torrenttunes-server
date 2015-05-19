package com.ytm;

import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractScheduledService;

public class ScanTrackerService extends AbstractScheduledService {

	@Override
	protected void runOneIteration() throws Exception {
		
		// Query the DB for new 
		System.out.println("u suck");
		
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 5, TimeUnit.SECONDS);
	}

}
