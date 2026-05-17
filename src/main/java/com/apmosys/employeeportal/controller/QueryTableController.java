package com.apmosys.employeeportal.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.apmosys.employeeportal.dto.QueryTableDTO;
import com.apmosys.employeeportal.model.CustomQuery;
import com.apmosys.employeeportal.service.QueryTableService;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@PostMapping("/create")
	public ResponseEntity<?> create(@RequestBody Map<String, Object> req) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			CustomQuery query = mapper.convertValue(req.get("query"), CustomQuery.class);
			List<?> roleIdsRaw = (List<?>) req.get("roleIds");
			List<Long> roleIds = roleIdsRaw.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
			CustomQuery saved = queryTableService.createCustomQuery(query, roleIds);
			return ResponseEntity.ok(Map.of("status", 200, "message", "Query created successfully", "data", saved));

		} catch (Exception ex) {
			return ResponseEntity.internalServerError().body(Map.of("status", 500, "message", ex.getMessage()));
		}
	}

	@GetMapping("/getByEmpID/{empId}")
	public ResponseEntity<?> getByEmp(@PathVariable Long empId) {
		try {
			return ResponseEntity.ok(Map.of("status", 200, "message", "Queries fetched successfully", "data", queryTableService.getQueriesByEmpId(empId)));
		} catch (Exception ex) {
			return ResponseEntity.internalServerError().body(Map.of("status", 500, "message", ex.getMessage()));
		}
	}

	@PutMapping("/update/{queryId}")
	public ResponseEntity<?> update(@PathVariable Long queryId, @RequestBody Map<String, Object> req) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			CustomQuery query = mapper.convertValue(req.get("query"), CustomQuery.class);
			List<?> roleIdsRaw = (List<?>) req.get("roleIds");
			List<Long> roleIds = roleIdsRaw.stream().map(id -> Long.valueOf(id.toString())).collect(Collectors.toList());
			CustomQuery updated = queryTableService.updateCustomQuery(queryId, query, roleIds);
			return ResponseEntity.ok(Map.of("status", 200,"message", "Query updated successfully","data", updated));
		} catch (Exception ex) {
			return ResponseEntity.internalServerError().body(Map.of("status", 500,"message", ex.getMessage()));
		}
	}
	
}
