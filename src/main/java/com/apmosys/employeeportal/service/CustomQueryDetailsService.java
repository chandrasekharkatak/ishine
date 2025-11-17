package com.apmosys.employeeportal.service;
import java.io.IOException;
import java.time.LocalDateTime;
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
import com.apmosys.employeeportal.model.Department;
import com.apmosys.employeeportal.model.Designation;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.FieldAlteration;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.CustomQueryDetailsRepository;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.DesignationDepartmentMapRepository;
import com.apmosys.employeeportal.repository.DesignationRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.FieldAlterationRepository;
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
	DepartmentRepository departmentRepository;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Autowired
	FieldAlterationRepository fieldAlterationRepository;


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
	
	
	
	
	public ServiceResponse bulkUploadifNoerror(MultipartFile file, Long uploadedBy) throws EncryptedDocumentException, InvalidFormatException {
	    ServiceResponse response = new ServiceResponse();
	    List<String> errorMessages = new ArrayList<>();
	    List<Employee> employeesToUpdate = new ArrayList<>(); // Collect employees to update

	    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rows = sheet.iterator();

	        Row headerRow = rows.next();
	        Set<String> availableColumns = new HashSet<>(Arrays.asList(
	            "Employee Id", "Employee Name", "Gender", "Manager Name", "Designation Name" ,"Manager Id"
	        ));

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

	            String empIdentifier = null;
	            Long empId = 0L;

	            if (columnIndexMap.containsKey("Employee Id")) {
	                Cell empIdentifierCell = currentRow.getCell(columnIndexMap.get("Employee Id"));
	                empIdentifier = getCellValueAsString(empIdentifierCell);
	                empId = resolveEmployeeId(empIdentifier);
	                if (empId == null) {
	                    errorMessages.add("Row " + rowNum + ": Employee '" + empIdentifier + "' not found.");
	                    continue;
	                }
	            }

	            String employeeName = null;
	            if (columnIndexMap.containsKey("Employee Name")) {
	                Cell employeeNameCell = currentRow.getCell(columnIndexMap.get("Employee Name"));
	                if (employeeNameCell == null || employeeNameCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Employee Name is missing.");
	                    continue;
	                }
	                employeeName = employeeNameCell.getStringCellValue().trim();
	            }

	            Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmpId(empId));
	            if (!optionalEmployee.isPresent()) {
	                continue;
	            }

	            Employee employee = optionalEmployee.get();

	            if (!normalizeName(employee.getName()).equals(normalizeName(employeeName))) {
	                errorMessages.add("Row " + rowNum + ": Employee Name does not correspond to Employee ID '" + empIdentifier + "'.");
	                continue;
	            }

	            // Gender validation
	            if (columnIndexMap.containsKey("Gender")) {
	                Cell genderCell = currentRow.getCell(columnIndexMap.get("Gender"));
	                if (genderCell == null || genderCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Gender field is null or empty.");
	                } else {
	                    String gender = genderCell.getStringCellValue().trim().toLowerCase();
	                    if ("male".equals(gender) || "female".equals(gender)) {
	                        employee.setGender(gender);
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Invalid Gender value. Allowed values are 'male' or 'female'.");
	                    }
	                }
	            }
	         // Manager validation (based on both Manager Id and Manager Name)
	            if (columnIndexMap.containsKey("Manager Id") || columnIndexMap.containsKey("Manager Name")) {

	                String managerIdentifier = null;
	                Long managerId = null;
	                String managerName = null;

	                if (columnIndexMap.containsKey("Manager Id")) {
	                    Cell managerIdCell = currentRow.getCell(columnIndexMap.get("Manager Id"));
	                    if (managerIdCell != null && !getCellValueAsString(managerIdCell).trim().isEmpty()) {
	                        managerIdentifier = getCellValueAsString(managerIdCell).trim();
	                        managerId = resolveEmployeeId(managerIdentifier);
	                        if (managerId == null) {
	                            errorMessages.add("Row " + rowNum + ": Manager ID '" + managerIdentifier + "' not found.");
	                        }
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Manager ID is missing.");
	                    }
	                }

	                if (columnIndexMap.containsKey("Manager Name")) {
	                    Cell managerNameCell = currentRow.getCell(columnIndexMap.get("Manager Name"));
	                    if (managerNameCell != null && !managerNameCell.getStringCellValue().trim().isEmpty()) {
	                        managerName = managerNameCell.getStringCellValue().trim();
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Manager Name is missing.");
	                    }
	                }

	                if (managerId != null && managerName != null) {
	                    Optional<Employee> optionalManager = Optional.ofNullable(employeeRepository.findByEmpId(managerId));
	                    if (optionalManager.isPresent()) {
	                        Employee manager = optionalManager.get();
	                        if (!normalizeName(manager.getName()).equals(normalizeName(managerName))) {
	                            errorMessages.add("Row " + rowNum + ": Manager Name does not match Manager ID '" + managerIdentifier + "'.");
	                        } else if ("InActive".equalsIgnoreCase(manager.getEmploymentstatus())) {
	                            errorMessages.add("Row " + rowNum + ": Manager '" + managerName + "' is InActive and cannot be assigned.");
	                        } else {
	                            employee.setManagerId(manager.getEmpId());
	                        }
	                    } else {
	                        errorMessages.add("Row " + rowNum + ": Manager ID '" + managerIdentifier + "' not found.");
	                    }
	                }
	            }


	            // Designation validation
	            if (columnIndexMap.containsKey("Designation Name")) {
	                Cell designationNameCell = currentRow.getCell(columnIndexMap.get("Designation Name"));
	                if (designationNameCell == null || designationNameCell.getStringCellValue().trim().isEmpty()) {
	                    errorMessages.add("Row " + rowNum + ": Designation Name field is null or empty.");
	                } else {
	                    String designationName = normalizeName(designationNameCell.getStringCellValue().trim().toLowerCase());
	                    Optional<Designation> findDesignation = Optional.ofNullable(designationRepository.findByDesignationNameIgnoreCase(designationName));
	                    if (!findDesignation.isPresent()) {
	                        errorMessages.add("Row " + rowNum + ": Designation '" + designationName + "' not found.");
	                    } else {
	                        Designation designation = findDesignation.get();
	                        Long jobRoleId = employee.getJobRoleId();
	                        Optional<JobRole> jobRole = jobRoleRepository.findById(jobRoleId);
	                        if (!jobRole.isPresent()) {
	                            errorMessages.add("Row " + rowNum + ": Job role for employee ID '" + empIdentifier + "' not found.");
	                        } else {
	                            Long employeeDeptId = jobRole.get().getDeptId();
	                            Optional<Department> dpt = departmentRepository.findById(employeeDeptId);
	                            List<Long> deptIdListForDesignation = designationDepartmentMapRepository
	                                .findDeptIdsByDesignationId(designation.getDesignationId());
	                            if (!deptIdListForDesignation.contains(employeeDeptId)) {
	                                errorMessages.add("Row " + rowNum + ": Employee ID '" + empIdentifier + "' with department Name '" + dpt.get().getName() +
	                                        "' does not belong to the department mapped to designation '" + designationName + "'.");
	                            } else {
	                                employee.setDesignationId(designation.getDesignationId());
	                            }
	                        }
	                    }
	                }
	            }

	            // Add employee to the list if no row-level error
	            employeesToUpdate.add(employee);
	        }

	        
	        if (!errorMessages.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(errorMessages);
	            return response; 
	        }

	       
	        employeeRepository.saveAll(employeesToUpdate);
	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("File uploaded and processed successfully.");

	    } catch (IOException e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	    }

	    return response;
	}



	
	
	private Long resolveEmployeeId(String empIdentifier) {
	    if (empIdentifier.startsWith("A-")) {
	        String idNum = empIdentifier.substring(2);
	        Employee emp = employeeRepository.findByEmployeementIdForOthers(Long.valueOf(idNum));
	        return emp != null ? emp.getEmpId() : null;
	    } else if (empIdentifier.startsWith("AP-")) {
	        String idNum = empIdentifier.substring(3);
	        Employee emp = employeeRepository.findByEmployeementIdForApmosysProduct(Long.valueOf(idNum));
	        return emp != null ? emp.getEmpId() : null;
	    }
	    else if (empIdentifier.startsWith("APR-")) {
	        String idNum = empIdentifier.substring(4);
	        Employee emp = employeeRepository.findByEmployeementIdForApprentice(Long.valueOf(idNum));
	        return emp != null ? emp.getEmpId() : null;
	    }
	    return null;
	}
	
	private String getCellValueAsString(Cell cell) {
	    if (cell == null) return null;

	    switch (cell.getCellTypeEnum()) {
	        case STRING:
	            return cell.getStringCellValue().trim();
	        case NUMERIC:
	            return String.valueOf((long) cell.getNumericCellValue()).trim();
	        case BOOLEAN:
	            return String.valueOf(cell.getBooleanCellValue()).trim();
	        default:
	            return null;
	    }
	}
	
	private String normalizeName(String name) {
	    return name == null ? "" : name.trim().replaceAll("\\s+", "").toLowerCase();
	}





}
