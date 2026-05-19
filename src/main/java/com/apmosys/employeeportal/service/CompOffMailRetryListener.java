package com.apmosys.employeeportal.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * Logs each failed attempt during Spring Retry for comp-off notification mail.
 */
@Component("compOffMailRetryListener")
public class CompOffMailRetryListener implements RetryListener {

	private static final Logger log = LogManager.getLogger(CompOffMailRetryListener.class);

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
		if (throwable != null) {
			log.warn("Comp-off mail attempt failed (retryCount={}): {}", context.getRetryCount(),
					throwable.getMessage());
		}
	}

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		// Must return true so the retry callback runs; false would skip/abandon the operation.
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
			Throwable throwable) {
		// No-op; optional: log final outcome at TRACE if needed.
	}
}
