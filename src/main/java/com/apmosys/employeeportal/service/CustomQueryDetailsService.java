package com.apmosys.employeeportal.service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.dto.CustomQueryDetailsDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CustomQueryDetails;
import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.CustomQueryDetailsRepository;
import com.apmosys.employeeportal.repository.DesignationDepartmentMapRepository;
import com.apmosys.employeeportal.repository.DesignationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;


@Service
public class CustomQueryDetailsService {
	
	@Autowired
	CustomQueryDetailsRepository customQueryDetailsRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	JobRoleRepository jobRoleRepository;
	
	@Autowired
	DesignationDepartmentMapRepository designationDepartmentMapRepository;
	
	@Autowired
	DesignationRepository designationRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;

//	 @Transactional
//	    public ServiceResponse saveCustomQueryDetails(CustomQueryDetailsDTO customQueryDTO) {
//	        ServiceResponse response = new ServiceResponse();
//	        LogDTO apiLogInfo = new LogDTO();
//	        apiLogInfo.setApiUrl("/api/saveCustomQueryDetails");
//	        apiLogInfo.setLogLevel("INFO");
//	        
//	        try {
//	            CustomQueryDetails customQueryDetails = new CustomQueryDetails();
//	            customQueryDetails.setCreatedBy(customQueryDTO.getCreatedBy());
//	            customQueryDetails.setQueryName(customQueryDTO.getQueryName());
//	            customQueryDetails.setAvailableColumns(String.join(",", customQueryDTO.getAvailableColumns()));
//	            customQueryDetails.setSelectedColumns(String.join(",", customQueryDTO.getSelectedColumns()));
//
//	            CustomQueryDetails savedQuery = customQueryDetailsRepository.save(customQueryDetails);
//	            
//	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//	            response.setServiceResponse("Custom query saved successfully.");
//	            
//	            apiLogInfo.setApiResponse("Custom query saved successfully.");
//	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
//	            
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//	            response.setServiceResponse("Something Went Wrong.");
//	            response.setServiceError(e.getMessage());
//	            
//	            apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
//	            apiLogInfo.setApiResponse("ERROR");
//	        }
//	        
//	        apiLogInfo.setApiRequest(customQueryDTO.toString());
//	        logService.logMyInfo(httpRequest, apiLogInfo);
//	        
//	        return response;
//	    }
	 
	@Transactional
	public ServiceResponse saveCustomQueryDetails(CustomQueryDetailsDTO customQueryDTO) {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/saveCustomQueryDetails");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	    	
	    	
	    	
	    	  Optional<CustomQueryDetails> existingQueryByName = customQueryDetailsRepository.findByQueryName(customQueryDTO.getQueryName());
	          
	          if (existingQueryByName.isPresent()) {
	              response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	              response.setServiceResponse("A query with the name '" + customQueryDTO.getQueryName() + "' already exists. Duplicate query names are not allowed.");
	              return response;
	          }
	        // Check for duplicate available and selected columns
	        String availableColumns = String.join(",", customQueryDTO.getAvailableColumns());
	        String selectedColumns = String.join(",", customQueryDTO.getSelectedColumns());
	        
	        Optional<CustomQueryDetails> existingQuery = customQueryDetailsRepository.findByAvailableAndSelectedColumns(availableColumns, selectedColumns);
	        
	        if (existingQuery.isPresent()) {
	            // If found, check if the query name is the same or different
	            if (existingQuery.get().getQueryName().equalsIgnoreCase(customQueryDTO.getQueryName())) {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("A query with this name and columns already exists.");
	            } else {
	                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	                response.setServiceResponse("This combination of available and selected columns already exists under the name: " + existingQuery.get().getQueryName());
	            }
	            return response;
	        }
	        
	        // Save new query if no duplicates found
	        CustomQueryDetails customQueryDetails = new CustomQueryDetails();
	        customQueryDetails.setCreatedBy(customQueryDTO.getCreatedBy());
	        customQueryDetails.setQueryName(customQueryDTO.getQueryName());
	        customQueryDetails.setAvailableColumns(availableColumns);
	        customQueryDetails.setSelectedColumns(selectedColumns);

	        customQueryDetailsRepository.save(customQueryDetails);
	        
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("Custom query saved successfully.");
	        apiLogInfo.setApiResponse("Custom query saved successfully.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something Went Wrong.");
	        response.setServiceError(e.getMessage());
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("ERROR");
	    }
	    
	    apiLogInfo.setApiRequest(customQueryDTO.toString());
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    
	    return response;
	}
	
	@Transactional
	public ServiceResponse getCustomQueries() {
	    ServiceResponse response = new ServiceResponse();
	    LogDTO apiLogInfo = new LogDTO();
	    apiLogInfo.setApiUrl("/api/getCustomQueries");
	    apiLogInfo.setLogLevel("INFO");
	    
	    try {
	        List<Object[]> queries = customQueryDetailsRepository.findAllCustomQueries();
	        
	        if (queries.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("No custom queries found.");
	            return response;
	        }

	        List<CustomQueryDetailsDTO> queryDTOs = new ArrayList<>();
	        for (Object[] query : queries) {
	            CustomQueryDetailsDTO dto = new CustomQueryDetailsDTO();
	            dto.setCustomQueryId(((Number) query[0]).longValue());
	            dto.setQueryName((String) query[1]);
	            dto.setAvailableColumns(Arrays.asList(((String) query[2]).split(",")));
	            dto.setSelectedColumns(Arrays.asList(((String) query[3]).split(",")));
	            queryDTOs.add(dto);
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse(queryDTOs);
	        apiLogInfo.setApiResponse("Custom queries fetched successfully.");
	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_SUCCESS);
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	        response.setServiceError(e.getMessage());

	        apiLogInfo.setApiStatus(ServiceResponse.STATUS_FAIL);
	        apiLogInfo.setApiResponse("ERROR");
	    }

	    apiLogInfo.setApiRequest("Fetching custom queries");
	    logService.logMyInfo(httpRequest, apiLogInfo);
	    
	    return response;
	}
	
	public ServiceResponse bulkUpload(MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
	    ServiceResponse response = new ServiceResponse();
	    List<Long> inactiveEmployees = new ArrayList<>();
	    List<String> errorMessages = new ArrayList<>();

	    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rows = sheet.iterator();

	        Row headerRow = rows.next();
	        Set<String> availableColumns = new HashSet<>(Arrays.asList("Employee Id", "Billable", "Billable Type", "Gender", "Manager Name", "Designation Name"));

	        Map<String, Integer> columnIndexMap = new HashMap<>();
	        for (Cell cell : headerRow) {
	            String headerName = cell.getStringCellValue().trim();
	            if (availableColumns.contains(headerName)) {
	                columnIndexMap.put(headerName, cell.getColumnIndex());
	            }
	        }
	        
	        if (!rows.hasNext()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("The uploaded file is empty.");
	            return response;
	        }

	        int rowNum = 1;
	        while (rows.hasNext()) {
	            Row currentRow = rows.next();
	            rowNum++;

	            Long employeeId = null;
	            try {
	                employeeId = (long) currentRow.getCell(columnIndexMap.get("Employee Id")).getNumericCellValue();
	            } catch (Exception e) {
	                errorMessages.add("Row " + rowNum + ": Invalid EmployeeId.");
	                continue;
	            }

	            Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employeeId));
	            if (!optionalEmployee.isPresent()) {
	                errorMessages.add("Row " + rowNum + ": Employee with ID '" + employeeId + "' not found.");
	                continue;
	            }

	            Employee employee = optionalEmployee.get();

	            // Check for InActive employees
	            if ("InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
	                inactiveEmployees.add(employeeId);
	                continue;
	            }

	            // Check for null or empty Billable field
	            if (columnIndexMap.containsKey("Billable")) {
	                Cell billableCell = currentRow.getCell(columnIndexMap.get("Billable"));
	                if (billableCell == null || billableCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Billable field is null or empty.");
	                } else {
	                    String billable = billableCell.getStringCellValue().trim().toLowerCase();
	                    if ("yes".equals(billable)) {
	                        employee.setBillable("Yes");
	                    } else if ("no".equals(billable)) {
	                        employee.setBillable("No");
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Invalid value for Billable field. Allowed values are 'Yes' or 'No'.");
	                    }
	                }
	            }

	            // Check for null or empty Billable Type field and normalize
	            if (columnIndexMap.containsKey("Billable Type")) {
	                Cell billableTypeCell = currentRow.getCell(columnIndexMap.get("Billable Type"));
	                if (billableTypeCell == null || billableTypeCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Billable Type field is null or empty.");
	                } else {
	                    String billableType = billableTypeCell.getStringCellValue().trim();
	                    switch (billableType.toLowerCase()) {
	                        case "internalrndproducts":
	                            employee.setBillableType("InternalRNDProducts");
	                            break;
	                        case "fixed cost":
	                            employee.setBillableType("Fixed Cost");
	                            break;
	                        case "tnm":
	                            employee.setBillableType("TNM");
	                            break;
	                        case "shadow":
	                            employee.setBillableType("Shadow");
	                            break;
	                        case "bench":    
	                        	employee.setBillableType("Bench");
	                        	break;
	                        default:
	                            errorMessages.add("Row " + rowNum + ": Invalid Billable Type. Allowed values are 'InternalRNDProducts', 'Fixed Cost', 'TNM', 'Bench', 'Shadow'.");
	                    }
	                }
	            }

	            // Check for null or empty Gender field and normalize
	            if (columnIndexMap.containsKey("Gender")) {
	                Cell genderCell = currentRow.getCell(columnIndexMap.get("Gender"));
	                if (genderCell == null || genderCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Gender field is null or empty.");
	                } else {
	                    String gender = genderCell.getStringCellValue().trim().toLowerCase();
	                    if ("male".equals(gender) || "female".equals(gender)) {
	                        employee.setGender(gender);  // Store as "male" or "female"
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Invalid Gender value. Allowed values are 'male' or 'female'.");
	                    }
	                }
	            }

	            // Check for null or empty Manager Name field
	            if (columnIndexMap.containsKey("Manager Name")) {
	                Cell managerNameCell = currentRow.getCell(columnIndexMap.get("Manager Name"));
	                if (managerNameCell == null || managerNameCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Manager Name field is null or empty.");
	                } else {
	                    String managerName = managerNameCell.getStringCellValue().trim().toLowerCase();
	                    Optional<Employee> findManager = Optional.ofNullable(employeeRepository.findByNameIgnoreCase(managerName));
	                    
	                    if (findManager.isPresent()) {
	                        Employee manager = findManager.get();

	                        // Check if manager is inactive
	                        if ("InActive".equalsIgnoreCase(manager.getEmploymentstatus())) {
	                            errorMessages.add("Row " + rowNum + ": Manager '" + managerName + "' is InActive and cannot be assigned.");
	                            continue;  // Skip to the next row
	                        }

	                        employee.setManagerId(manager.getEmpId());
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Manager '" + managerName + "' not found.");
	                    }
	                }
	            }

	            // Check for null or empty Designation Name field
	            if (columnIndexMap.containsKey("Designation Name")) {
	                Cell designationNameCell = currentRow.getCell(columnIndexMap.get("Designation Name"));
	                if (designationNameCell == null || designationNameCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Designation Name field is null or empty.");
	                } else {
	                    String designationName = designationNameCell.getStringCellValue().trim().toLowerCase();
	                    Optional<Designation> findDesignation = Optional.ofNullable(designationRepository.findByDesignationNameIgnoreCase(designationName));
	                    if (!findDesignation.isPresent()) {
	                        errorMessages.add("Row " + rowNum + ": Designation '" + designationName + "' not found.");
	                        continue;
	                    }

	                    Designation designation = findDesignation.get();
	                    Long jobRoleId = employee.getJobRoleId(); 
	                    Optional<JobRole> jobRole = jobRoleRepository.findById(jobRoleId);
	                    if (!jobRole.isPresent()) {
	                        errorMessages.add("Row " + rowNum + ": Job role for employee ID '" + employeeId + "' not found.");
	                        continue;
	                    }

	                    Long employeeDeptId = jobRole.get().getDeptId();
	                    List<Long> deptIdListForDesignation = designationDepartmentMapRepository
	                        .findDeptIdsByDesignationId(designation.getDesignationId());

	                    if (!deptIdListForDesignation.contains(employeeDeptId)) {
	                        errorMessages.add("Row " + rowNum + ": Employee ID '" + employeeId + "' with department ID '" + employeeDeptId + 
	                                          "' does not belong to the valid departments mapped to designation '" + designationName + "'.");
	                        continue;
	                    }

	                    employee.setDesignationId(designation.getDesignationId());
	                }
	            }

	            employeeRepository.save(employee);
	        }

	        if (!inactiveEmployees.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Inactive employees found: " + inactiveEmployees.toString());
	            return response;
	        }

	        if (!errorMessages.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(String.join(", ", errorMessages));
	        } else {
	            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	            response.setServiceResponse("File uploaded and processed successfully.");
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	    }

	    return response;
	}

}
