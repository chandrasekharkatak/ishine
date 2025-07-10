package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ApiSourceDTO;
import com.apmosys.employeeportal.model.ApiSource;
import com.apmosys.employeeportal.repository.ApiSourceRepository;

@Service
public class ApiSourceService {
	
	@Autowired
	ApiSourceRepository apiSourceRepository;

	public List<ApiSourceDTO> getAllApiList() {
	    try {
	        List<ApiSource> sources = apiSourceRepository.findAll();
	        return sources.stream()
	                .map(api -> new ApiSourceDTO(
	                        api.getApiSourceId(),
	                        api.getLabel(),
	                        api.getUrl(),
	                        api.getLabelKey(),
	                        api.getValueKey()))
	                .collect(Collectors.toList());

	    } catch (Exception e) {
	        throw new RuntimeException("Unable to fetch API source list at this time. Please try again later.");
	    }
	}


}
