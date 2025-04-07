package com.apmosys.employeeportal.controller;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.CustomFilterDTO;
import com.apmosys.employeeportal.dto.QueryTableDTO;
import com.apmosys.employeeportal.service.QueryTableService;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping("/api/query")
@CrossOrigin("*")
public class QueryTableController {

	@Autowired
	private QueryTableService queryTableService;

	@RequestMapping(value="/createQuery" , method = RequestMethod.POST)
	public ServiceResponse createQuery(@RequestBody QueryTableDTO queryTableDto) {
		ServiceResponse response = new ServiceResponse();

		response = queryTableService.createQuery(queryTableDto);
		return response;
	}

	@RequestMapping(value="/getNonPublishedQuery" , method = RequestMethod.GET)
	public ServiceResponse getNonPublishedQuery() {
		ServiceResponse response = new ServiceResponse();

		response = queryTableService.getNonPublishedQuery();
		return response;
	}
	
	@RequestMapping(value="/getPublishedQuery" , method = RequestMethod.GET)
	public ServiceResponse getPublishedQuery() {
		ServiceResponse response = new ServiceResponse();

		response = queryTableService.getPublishedQuery();
		return response;
	}
	
	@RequestMapping(value = "/getQueryDataForPreview", method = RequestMethod.POST)
	public ServiceResponse getQueryDataForPreview(@RequestBody QueryTableDTO queryTableDto) throws SQLException {
		ServiceResponse response = queryTableService.getQueryDataForPreview(queryTableDto);
		return response;
	}
	
	@RequestMapping(value = "/getQueryDetailsByQueryId", method = RequestMethod.POST)
	public ServiceResponse getQueryDetailsByQueryId(@RequestBody QueryTableDTO queryTableDto) throws SQLException {
		ServiceResponse response = queryTableService.getQueryDetailsByQueryId(queryTableDto);
		return response;
	}
	
	@RequestMapping(value = "/updateQuery", method = RequestMethod.POST)
	public ServiceResponse updateQuery(@RequestBody QueryTableDTO queryTableDto) {
		ServiceResponse response = queryTableService.updateQuery(queryTableDto);
		return response;
	}
	
	@RequestMapping(value = "/deleteQuery", method = RequestMethod.POST)
	public ServiceResponse deleteQuery(@RequestBody QueryTableDTO queryTableDto) {
		ServiceResponse response = queryTableService.deleteQuery(queryTableDto);
		return response;
	}
	
	@RequestMapping(value = "/publishQuery", method = RequestMethod.POST)
	public ServiceResponse publishQuery(@RequestBody QueryTableDTO queryTableDto) {
		ServiceResponse response = queryTableService.publishQuery(queryTableDto);
		return response;
	}
	
}
