package com.apmosys.employeeportal.utility;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

public class EmailTrigger {

	public static void sendAfterCommit(Runnable task) {

	    if (TransactionSynchronizationManager.isActualTransactionActive()) {

	        TransactionSynchronizationManager.registerSynchronization(
	            new TransactionSynchronization() {
	                @Override
	                public void afterCommit() {
	                    task.run();
	                }
	            }
	        );

	    } else {
	        task.run(); // fallback (no transaction)
	    }
	}
	
	public static void sendAfterCommit2(Runnable task) {

	    if (TransactionSynchronizationManager.isSynchronizationActive()) {

	        TransactionSynchronizationManager.registerSynchronization(
	            new TransactionSynchronization() {
	                @Override
	                public void afterCommit() {
	                    task.run();
	                }
	            }
	        );

	    } else {
	        task.run();
	    }
	}

}
