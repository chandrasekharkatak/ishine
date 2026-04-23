package com.apmosys.employeeportal.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderDashboardRequest;
import com.apmosys.employeeportal.dto.repeatedoffender.RepeatedOffenderSummaryPayload;
import com.apmosys.employeeportal.repository.RepeatedOffenderRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Application service for Repeated Offender dashboard — dedicated APIs (no reuse of legacy dashboard endpoints).
 */
@Slf4j
@Service
public class RepeatedOffenderService {

	@Autowired
	private RepeatedOffenderRepository repeatedOffenderRepository;

	@Autowired
	private LogService logService;

	@Autowired
	private HttpServletRequest httpRequest;

	public ServiceResponse getSummary(RepeatedOffenderDashboardRequest request) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("repeatedOffenderSummary");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder("repeatedOffender/summary");

		try {
			if (request == null || request.getViewerEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("viewerEmpId is required");
				return response;
			}
			normalizePagingIgnoredForSummary(request);

			RepeatedOffenderSummaryPayload payload = repeatedOffenderRepository.fetchSummary(request);
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(payload);
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Repeated offender summary (snapshot KPIs)");
		} catch (Exception e) {
			log.error("repeatedOffender/summary failed", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		} finally {
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
		return response;
	}

	public ServiceResponse getEmployeeGrid(RepeatedOffenderDashboardRequest request) {
		ServiceResponse response = new ServiceResponse();
		LogDTO apiLogInfo = new LogDTO();
		apiLogInfo.setSubFeatureName("repeatedOffenderEmployees");
		apiLogInfo.setLogLevel("INFO");
		StringBuilder logBuilder = new StringBuilder("repeatedOffender/employees");

		try {
			if (request == null || request.getViewerEmpId() == null) {
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("viewerEmpId is required");
				return response;
			}
			normalizeGridRequest(request);

			long total = repeatedOffenderRepository.countEmployeeRows(request);
			response.setTotalElements((int) Math.min(total, Integer.MAX_VALUE));

			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			response.setServiceResponse(repeatedOffenderRepository.fetchEmployeeRows(request));
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
			apiLogInfo.setApiResponse("Repeated offender employees (snapshot list by metric segment)");
		} catch (Exception e) {
			log.error("repeatedOffender/employees failed", e);
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something went wrong.");
			apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
		} finally {
			apiLogInfo.setApiRequest(logBuilder.toString());
			logService.logMyInfo(httpRequest, apiLogInfo);
		}
		return response;
	}

	private static void normalizePagingIgnoredForSummary(RepeatedOffenderDashboardRequest request) {
		if (request.getDefaultedThreshold() == null || request.getDefaultedThreshold() < 1) {
			request.setDefaultedThreshold(1);
		}
	}

	private static void normalizeGridRequest(RepeatedOffenderDashboardRequest request) {
		normalizePagingIgnoredForSummary(request);
		if (request.getPage() == null || request.getPage() < 1) {
			request.setPage(1);
		}
		if (request.getSize() == null || request.getSize() < 1) {
			request.setSize(20);
		}
		if (request.getSortBy() == null || request.getSortBy().isBlank()) {
			request.setSortBy("employeeName");
		}
		if (request.getSortDirection() == null || request.getSortDirection().isBlank()) {
			request.setSortDirection("asc");
		}
		if (request.getTableSegment() == null || request.getTableSegment().isBlank()) {
			request.setTableSegment("TOTAL_APPLICABLE");
		}
	}
}
