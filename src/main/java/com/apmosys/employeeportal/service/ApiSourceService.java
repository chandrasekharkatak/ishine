package com.apmosys.employeeportal.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.ApiSourceDTO;
import com.apmosys.employeeportal.dto.DepartmentDTO;
import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.ProjectDTO;
import com.apmosys.employeeportal.model.ApiSource;
import com.apmosys.employeeportal.repository.ApiSourceRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;

@Service
public class ApiSourceService {
	
	@Autowired
	ApiSourceRepository apiSourceRepository;
	
	@Autowired
	ProjectRepository projectRepository;
	
	@Autowired
	DepartmentRepository departmentRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;

	public List<ApiSourceDTO> getAllApiList() {
	    try {
	        List<ApiSource> sources = apiSourceRepository.findAll();
	        return sources.stream()
	                .map(api -> new ApiSourceDTO(
	                        api.getApiSourceId(),
	                        api.getLabel(),
	                        api.getUrl(),
	                        api.getLabelKey(),
	                        api.getValueKey(),
	                        api.getIsdependent()))
	                .collect(Collectors.toList());

	    } catch (Exception e) {
	        throw new RuntimeException("Unable to fetch API source list at this time. Please try again later.");
	    }
	}

	public List<ProjectDTO> getAllProject() {
	    try {
	        return projectRepository.findAll()
	            .stream()
	            .map(obj -> {
	                ProjectDTO newdto = new ProjectDTO();
	                newdto.setProjectId(obj.getProjectId());
	                newdto.setProjectName(obj.getProjectName());
	                return newdto;
	            })
	            .collect(Collectors.toList());
	    } catch (Exception e) {
	        throw new RuntimeException("Something went wrong.", e);
	    }
	}

	public List<ProjectDTO> getClientByProjectId(String id) {
		try {
			return projectRepository.getClientByProjectId(Integer.parseInt(id));
	    } catch (Exception e) {
	        throw new RuntimeException("Something went wrong.", e);
	    }
	}

	public List<DepartmentDTO> getAllDepartment() {
		try {
			return departmentRepository.findAll().stream()
					.map(obj -> {
						DepartmentDTO newDeptDto = new DepartmentDTO();
						newDeptDto.setDeptId(obj.getDeptId());
						newDeptDto.setDeptName(obj.getName());
						return newDeptDto;
					})
					.collect(Collectors.toList());
	    } catch (Exception e) {
	        throw new RuntimeException("Something went wrong.", e);
	    }
	}

	public List<EmployeeDTO> getAllEmployee() {
		try {
			return employeeRepository.getAllEmployeeAsApiSource();
	    } catch (Exception e) {
	        throw new RuntimeException("Something went wrong.", e);
	    }
	}


}
