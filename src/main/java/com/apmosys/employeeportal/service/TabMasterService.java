package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.JobRoleDTO;
import com.apmosys.employeeportal.dto.RoleFeatureMapDTO;
import com.apmosys.employeeportal.dto.TabMasterDTO;
import com.apmosys.employeeportal.repository.EmployeeAccessOverrideRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.TabMasterRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class TabMasterService {
	
	@Autowired
	RoleFeatureMapRepository roleFeatureMapRepository;

	@Autowired
	EmployeeAccessOverrideRepository employeeAccessOverrideRepository;

	public ServiceResponse getTabsByRoleId(Long jobRoleId, Long empId) {
		ServiceResponse response = new ServiceResponse();
		try {
			List<Object[]> tabArrayList = roleFeatureMapRepository.getTabsByRoleId(jobRoleId);
			List<Object[]> overrideTabArrayList  = employeeAccessOverrideRepository.findActiveTabRowsByEmpId(empId);
			if (overrideTabArrayList != null && !overrideTabArrayList.isEmpty()) {
				if(tabArrayList == null){
					tabArrayList = new ArrayList<>();
				}
				tabArrayList.addAll(overrideTabArrayList);
			}

			if(tabArrayList != null  && !tabArrayList.isEmpty()){
				List<RoleFeatureMapDTO> dtoList = new ArrayList<RoleFeatureMapDTO>();
				for(Object[] tab :tabArrayList)
				{
					RoleFeatureMapDTO dto = new RoleFeatureMapDTO();
					dto.setSubFeatureMasterId(tab[0] != null ? Long.parseLong(tab[0].toString()) : null);
					dto.setSubFeatureName(tab[1] != null ? tab[1].toString() : null);
					dto.setFeatureId(tab[2] != null ? Long.parseLong(tab[2].toString()) : null);
					dto.setFeatureName(tab[3] != null ? tab[3].toString() : null);
					dto.setTabName(tab[4] != null ? tab[4].toString() : null);
					dto.setTabIcon(tab[5] != null ? tab[5].toString() : null);
					dto.setTabRouteName(tab[6] != null ? tab[6].toString() : null);		
					dto.setTabSequence(tab[7] != null ? Integer.parseInt(tab[7].toString()) : null);					
					dtoList.add(dto);
					
				}

				List<RoleFeatureMapDTO> sortedDtoList = dtoList.stream().sorted(Comparator.comparing(RoleFeatureMapDTO::getTabSequence)).collect(Collectors.toList());
				response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
				response.setServiceResponse(sortedDtoList);
			}
			else
			{
				response.setServiceStatus(ServiceResponse.STATUS_FAIL);
				response.setServiceResponse("Tab List is null.");
			}			

		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceResponse("Something Went Wrong.");
			response.setServiceError(e.getMessage());

		}
		return response;
	}

}
