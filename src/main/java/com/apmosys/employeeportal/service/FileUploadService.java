package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.apmosys.employeeportal.dto.EmployeeDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Designation;
//import com.apmosys.employeeportal.model.Applications;
import com.apmosys.employeeportal.model.Domain;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Specialization;
import com.apmosys.employeeportal.repository.DesignationRepository;
//import com.apmosys.employeeportal.repository.ApplicationRepository;
import com.apmosys.employeeportal.repository.DomainRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.SpecializationRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apmosys.employeeportal.Exception.BadRequestException;

@Service
@EnableAsync
public class FileUploadService {

//	@Autowired
//	DomainRepository domainRepository;
//	
//	@Autowired
//	SpecializationRepository specializationRepository;
//	
//	@Autowired
//	ApplicationRepository applicationRepository;
	
	@Autowired
	EmployeeRepository employeeRepository;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	private LogService logService;
	
	@Autowired
	private HttpServletRequest httpRequest;
	
	@Value("${billable.mail}")
	private String billableMail;
	
	
	
	@Value("${spring.profiles.active}")
	private String profile;
	
	@Autowired
	DesignationRepository designationRepository;
	
	
	private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);

//	public ServiceResponse uploadFile(MultipartFile file) throws IOException {
//		ServiceResponse response = new ServiceResponse();
//        Workbook workbook = new XSSFWorkbook(file.getInputStream());
//        Sheet sheet = workbook.getSheetAt(0);
//        Map<String, Domain> domainCache = new HashMap<>();
//        Map<String, Specialization> specializationCache = new HashMap<>();
//
//        for (Row row : sheet) {
//            if (row.getRowNum() == 0) continue; // Skip header row
//
//            String domainName = row.getCell(0).getStringCellValue();
//            String specializationName = row.getCell(1).getStringCellValue();
////            String applicationName = row.getCell(2).getStringCellValue();
//            Cell applicationCell = row.getCell(2);
//            String applicationName = "Other"; // Default value
//
//            if (applicationCell != null && applicationCell.getCellTypeEnum() == CellType.STRING) {
//                applicationName = applicationCell.getStringCellValue().trim();
//            }
//
//            Domain domain = domainCache.computeIfAbsent(domainName, name -> {
//                Domain d = domainRepository.findByDomainName(name);
//                if (d == null) {
//                    d = new Domain();
//                    d.setDomainName(name);
//                    d.setIsActive("true");
//                    d = domainRepository.save(d);
//                }
//                return d;
//            });
//
//            String specKey = domain.getDomainId() + ":" + specializationName;
//            Specialization specialization = specializationCache.computeIfAbsent(specKey, key -> {
//                Specialization s = specializationRepository.findBySpecializationNameAndDomainId(specializationName, domain.getDomainId());
//                if (s == null) {
//                    s = new Specialization();
//                    s.setSpecializationName(specializationName);
//                    s.setDomainId(domain.getDomainId());
//                    s.setIsActive("true");
//                    s = specializationRepository.save(s);
//                }
//                return s;
//            });
//
//            Applications application = new Applications();
//            application.setApplicationName(applicationName);
//            application.setSpecializationId(specialization.getSpecializationId());
//            application.setCreatedOn(LocalDate.now());
//            applicationRepository.save(application);
//        }
//        workbook.close();
//        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//        response.setServiceResponse("File uploaded and processed successfully.");
//        
//        return response;
//    }

	public ServiceResponse saveExcelData(MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
		ServiceResponse response = new ServiceResponse();
		 try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
	            Sheet sheet = workbook.getSheetAt(0);
	            Iterator<Row> rows = sheet.iterator();
	            rows.next(); // Skip header row

	            while (rows.hasNext()) {
	                Row currentRow = rows.next();

	                Long employmentId = (long) currentRow.getCell(0).getNumericCellValue();
	                String billable = currentRow.getCell(1).getStringCellValue();
	                String billableType = currentRow.getCell(2).getStringCellValue();
	                String gender = currentRow.getCell(3).getStringCellValue();
	                
	                Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employmentId));
	                
	                
	                if (optionalEmployee.isPresent()) {
	                	Employee employee = optionalEmployee.get();
	                    employee.setBillable(billable);
	                    employee.setBillableType(billableType);
	                    employee.setGender(gender);
	                    
		                employeeRepository.save(employee);
	                } 
            
                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
                response.setServiceResponse("File uploaded and processed successfully.");
           
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong");
            
        }
		return response;
	}
	
//	public ServiceResponse saveExcelDataForManagerMapping(MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
//		ServiceResponse response = new ServiceResponse();
//		 try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
//	            Sheet sheet = workbook.getSheetAt(0);
//	            Iterator<Row> rows = sheet.iterator();
//	            rows.next(); // Skip header row
//
//	            while (rows.hasNext()) {
//	                Row currentRow = rows.next();
//
//	                Long employmentId = (long) currentRow.getCell(0).getNumericCellValue();
//	                String billable = currentRow.getCell(1).getStringCellValue();
//	                String billableType = currentRow.getCell(2).getStringCellValue();
//	                String gender = currentRow.getCell(3).getStringCellValue();
//	                String manager = currentRow.getCell(4).getStringCellValue();
//	                
//	                Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employmentId));
//	                Optional<Employee> findManager = Optional.ofNullable(employeeRepository.findByName(manager));
//	                
//	                
//	                if (optionalEmployee.isPresent()) {
//	                	Employee employee = optionalEmployee.get();
//	                	Employee getManager = findManager.get();
//	                	employee.setBillable(billable);
//	                    employee.setBillableType(billableType);
//	                    employee.setGender(gender);
//	                    if(manager != null)
//	                    employee.setManagerId(getManager.getEmpId());
//
//	                    employeeRepository.save(employee);
//	                } 
//            
//                response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
//                response.setServiceResponse("File uploaded and processed successfully.");
//           
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
//            response.setServiceResponse("Something went wrong");
//            
//        }
//		return response;
//	}
	
	public ServiceResponse designationBulkUpload(MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
	    ServiceResponse response = new ServiceResponse();
	    List<Long> inactiveEmployees = new ArrayList<>();  // List to store IDs of inactive employees
	    List<String> errorMessages = new ArrayList<>();    // List to store error messages with row numbers

	    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rows = sheet.iterator();
	        rows.next(); // Skip header row
	        int rowNum = 1; // To track the row number

	        while (rows.hasNext()) {
	            Row currentRow = rows.next();
	            rowNum++;

	            Long employmentId = null;
	            String designationName = null;

	            try {
	                employmentId = (long) currentRow.getCell(0).getNumericCellValue();
	                designationName = currentRow.getCell(1).getStringCellValue().trim().toLowerCase();
	            } catch (Exception e) {
	                errorMessages.add("Row " + rowNum + ": Invalid data format.");
	                continue; // Skip processing this row
	            }

	            if (employmentId == null || designationName == null || designationName.isEmpty()) {
	                errorMessages.add("Row " + rowNum + ": Either Employee ID or Designation Name is missing.");
	                continue; // Skip processing this row
	            }

	            Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employmentId));
	            Optional<Designation> findDesignationName = Optional.ofNullable(designationRepository.findByDesignationNameIgnoreCase(designationName));

	            if (optionalEmployee.isPresent()) {
	                Employee employee = optionalEmployee.get();

	                if ("InActive".equalsIgnoreCase(employee.getEmploymentstatus())) {
	                    inactiveEmployees.add(employmentId);  // Add inactive employee ID to the list
	                    continue;  // Skip further processing for this employee
	                }

	                if (findDesignationName.isPresent()) {
	                    Designation getDesignation = findDesignationName.get();
	                    employee.setDesignationId(getDesignation.getDesignationId());
	                    employeeRepository.save(employee);
	                } else {
	                    errorMessages.add("Row " + rowNum + ": Designation '" + designationName + "' not found.");
	                }
	            } else {
	                errorMessages.add("Row " + rowNum + ": Employee ID '" + employmentId + "' not found.");
	            }
	        }

	        // Handle any errors related to missing data
	        if (!inactiveEmployees.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse("Inactive employees found: " + inactiveEmployees.toString());
	            return response;
	        }

	        if (!errorMessages.isEmpty()) {
	            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
	            response.setServiceResponse(String.join(", ", errorMessages));
	            return response;
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("File uploaded and processed successfully.");
	    } catch (IOException e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong.");
	    }
	    return response;
	}


	public ServiceResponse confirmationDateBulkUpload(MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
        ServiceResponse response = new ServiceResponse();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            rows.next(); // Skip header row

            while (rows.hasNext()) {
                Row currentRow = rows.next();

                Long employmentId = (long) currentRow.getCell(0).getNumericCellValue();
                Date confirmationDate = currentRow.getCell(1).getDateCellValue();

                Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employmentId));

                if (optionalEmployee.isPresent()) {
                    Employee employee = optionalEmployee.get();
                    if (confirmationDate != null) {
                        employee.setEmployeeConfirmationDate(confirmationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
                    }

                    employeeRepository.save(employee);
                }
            }

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Confirmation dates uploaded and processed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceResponse("Something went wrong during file processing.");
        }
        return response;
    }              	
	                		
	                	
	               
	                	
	                
	               
	
	public ServiceResponse saveExcelDataForManagerMapping(MultipartFile file) throws EncryptedDocumentException, InvalidFormatException {
	    ServiceResponse response = new ServiceResponse();
	    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
	        Sheet sheet = workbook.getSheetAt(0);
	        Iterator<Row> rows = sheet.iterator();
	        rows.next(); // Skip header row

	        while (rows.hasNext()) {
	            Row currentRow = rows.next();

	            Long employmentId = (long) currentRow.getCell(0).getNumericCellValue();
	            String billable = currentRow.getCell(1).getStringCellValue();
	            String billableType = currentRow.getCell(2).getStringCellValue();
	            String gender = currentRow.getCell(3).getStringCellValue();
	            String manager = currentRow.getCell(4).getStringCellValue().trim().toLowerCase(); // Convert to lowercase
	            
	            Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employmentId));
	            Optional<Employee> findManager = Optional.ofNullable(employeeRepository.findByNameIgnoreCase(manager));
	            
	            if (optionalEmployee.isPresent()) {
                	Employee employee = optionalEmployee.get();
                	Employee getManager = findManager.get();
                	employee.setBillable(billable);
                    employee.setBillableType(billableType);
                    employee.setGender(gender);
                    if(manager != null)
                    employee.setManagerId(getManager.getEmpId());

                    employeeRepository.save(employee);
	            }
	        }

	        response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
	        response.setServiceResponse("File uploaded and processed successfully.");
	    } catch (IOException e) {
	        e.printStackTrace();
	        response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
	        response.setServiceResponse("Something went wrong");
	    }
	    return response;
	}

	
	
	
	
//	export billable type and users daily basis excel report and attach on mail by cron
	
	public static void writeToExcel(List<EmployeeDTO> employeeList, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            int rowNum = 0;

            String[] headers = {"EmployeementId", "EmployeeName", "Email", "Billable", "BillableType", "DepartmentName", "ManagerName", "HodName","Project","Client"};
            Row headerRow = sheet.createRow(rowNum++);
//            for (int i = 0; i < headers.length; i++) {
//                headerRow.createCell(i).setCellValue(headers[i]);
//            }
            // Create a bold font style for headers
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);


            // Set headers with the bold style
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }
            
            for (EmployeeDTO data : employeeList) {
                Row row = sheet.createRow(rowNum++);
//                row.createCell(0).setCellValue(data.getEmpId());
                row.createCell(0).setCellValue("A-"+data.getEmployeementId());
                row.createCell(1).setCellValue(data.getEmployeeName());
                row.createCell(2).setCellValue(data.getEmail());
                row.createCell(3).setCellValue(data.getBillable());
                row.createCell(4).setCellValue(data.getBillableType());
                row.createCell(5).setCellValue(data.getDepartmentName());
                row.createCell(6).setCellValue(data.getManagerName());
                row.createCell(7).setCellValue(data.getHodName());   
                row.createCell(8).setCellValue(data.getProjectName());
                row.createCell(9).setCellValue(data.getClientName());
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Manually set a wider width for the email column if needed
            int emailColumnIndex = 2; // Column index for the email
            sheet.setColumnWidth(emailColumnIndex, 30 * 256); // 30 characters wide

            int projectColumnIndex = 8; // Column index for the project
            sheet.setColumnWidth(projectColumnIndex, 30 * 256);

            int clientColumnIndex = 9; // Column index for the client
            sheet.setColumnWidth(clientColumnIndex, 40 * 256);
            
            
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
        }
    }
	
	
//	@Scheduled(cron = "0 */4 * * * *")
//	@Scheduled(cron = "0 0 7 ? * *")
	@Async
	@Scheduled(cron = "${VP_mails}")
	public void execute() {
	    try {
	        List<Object[]> findVpEmail = employeeRepository.findAllVPsEmail();

	        for (Object[] vpObj : findVpEmail) {
	            String email = vpObj[0] != null ? vpObj[0].toString() : null;
	            Long deptId = vpObj[1] != null ? Long.parseLong(vpObj[1].toString()) : null;

	            if (email == null || deptId == null) continue;

	            List<Object[]> employees = employeeRepository.getEmployeesWithBillableType(deptId);
	            List<EmployeeDTO> dtoList = new ArrayList<>();

	            for (Object[] object : employees) {
	                EmployeeDTO dto = new EmployeeDTO();
	                dto.setEmployeementId(object[1] != null ? Long.parseLong(object[1].toString()) : null);
	                dto.setEmployeeName(object[2] != null ? object[2].toString() : null);
	                dto.setEmail(object[3] != null ? object[3].toString() : null);
	                dto.setBillable(object[4] != null ? object[4].toString() : null);
	                dto.setBillableType(object[5] != null ? object[5].toString() : null);
	                dto.setDepartmentName(object[6] != null ? object[6].toString() : null);
	                dto.setManagerName(object[7] != null ? object[7].toString() : null);
	                dto.setHodName(object[8] != null ? object[8].toString() : null);
	                dto.setProjectName(object[9] != null ? object[9].toString() : null);
	                dto.setClientName(object[10] != null ? object[10].toString() : null);
	                dtoList.add(dto);
	            }

	            // Export to Excel
	            String filePath = "EmployeeBillableData_" + deptId + ".xlsx";
	            writeToExcel(dtoList, filePath);
	            File file = new File(filePath);

	            String subject = "Regarding billable and non-billable employees data";
	            mailService.sendMailWithoutAttachment(
	                email,
	                subject,
	                "Dear Vice Presidents, <br><br>" 
	                        + "Hope this email finds you well. Please find the attached document containing the latest billable and non billable employee data.<br><br>"
	                        		+ "Thank you for your attention to this matter.<br><br>"
	                        		+ "Best regards,<br>",

	                file
	            );

	            logger.info("Email sent to VP: {}", email);
	        }
	    } catch (Exception e) {
	        logger.error("Error occurred in VP_mails billable and non billable data", e);
	    }

	    logger.info("Finished VP_mails billable and non billable data");
	}

	// added report feature only for directors
	
	public static void writeToExcelBillableReport(List<EmployeeDTO> employeeList, String filePath) throws IOException {
	    try (Workbook workbook = new XSSFWorkbook()) {
	        Sheet sheet = workbook.createSheet("Data");
	        int rowNum = 0;

	        String[] headers = {"Billable Type", "Count of Employees"};
	        Row headerRow = sheet.createRow(rowNum++);

	        // Create a bold font style for headers
	        Font headerFont = workbook.createFont();
	        headerFont.setBold(true);
	        CellStyle headerCellStyle = workbook.createCellStyle();
	        headerCellStyle.setFont(headerFont);
	        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
	        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        headerCellStyle.setBorderBottom(BorderStyle.THIN);
	        headerCellStyle.setBorderTop(BorderStyle.THIN);
	        headerCellStyle.setBorderLeft(BorderStyle.THIN);
	        headerCellStyle.setBorderRight(BorderStyle.THIN);

	        // Set headers with the bold style
	        for (int i = 0; i < headers.length; i++) {
	            var cell = headerRow.createCell(i);
	            cell.setCellValue(headers[i]);
	            cell.setCellStyle(headerCellStyle);
	        }

	        // Create a cell style for department rows with yellow background
	        CellStyle departmentCellStyle = workbook.createCellStyle();
	        departmentCellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
	        departmentCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	        departmentCellStyle.setBorderBottom(BorderStyle.THIN);
	        departmentCellStyle.setBorderTop(BorderStyle.THIN);
	        departmentCellStyle.setBorderLeft(BorderStyle.THIN);
	        departmentCellStyle.setBorderRight(BorderStyle.THIN);
	        Font departmentFont = workbook.createFont();
	        departmentFont.setBold(true);
	        departmentCellStyle.setFont(departmentFont);

	        String currentDepartment = null;
	        for (EmployeeDTO data : employeeList) {
	           
	            if (!data.getDepartmentName().equals(currentDepartment)) {
	                // Create a merged cell for department name with yellow background
	                currentDepartment = data.getDepartmentName();
	                Row departmentRow = sheet.createRow(rowNum++);
	                Cell departmentCell = departmentRow.createCell(0);
	                departmentCell.setCellValue(currentDepartment);
	                departmentCell.setCellStyle(departmentCellStyle);
	                sheet.addMergedRegion(new CellRangeAddress(departmentRow.getRowNum(), departmentRow.getRowNum(), 0, headers.length - 1));
	            }
	            Row row = sheet.createRow(rowNum++);
	            row.createCell(0).setCellValue(data.getBillableType());
	            row.createCell(1).setCellValue(data.getCount_of_employees());
	        }

	        for (int i = 0; i < headers.length; i++) {
	            sheet.autoSizeColumn(i);
	        }

	        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
	            workbook.write(fileOut);
	        }
	    }
	}

//	
//	
//	@Scheduled(cron = "0 0 16 * * ?")
	
//	@Async
//	@Scheduled(cron = "${department_wise_billable_report}")
//	public void executeBillableReportMethod() {
//		logger.info("Starting executeBillableReportMethod");
//		
//	    try {
//	        List<Object[]> employees = employeeRepository.getEmployeesBillableDataDepartmentWise();
//	        List<EmployeeDTO> dtoList = new ArrayList<>();
//
//	        employees.forEach((object)->{
//				EmployeeDTO dto = new EmployeeDTO();
//				
//				dto.setDepartmentName(object[0] != null ? object[0].toString() : null);		
//				dto.setBillableType(object[1] != null ? object[1].toString() : null);
//				dto.setCount_of_employees(object[2] != null ? Long.parseLong(object[2].toString()) : null);			
//				
//				dtoList.add(dto);
//				});
//
//	        String filePath = "Department_BillableData.xlsx";
//	        writeToExcelBillableReport(dtoList, filePath);
//	        logger.info("Excel file written to {}", filePath);
//
//	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//	        LocalDate todayDate = LocalDate.now();
//	        
//	        String today = LocalDate.now().format(formatter);
//	        
//	        
////	        String subject = "Department wise Billable type document on - " + today;
//	        String subject = "Department wise Billable type "+profile+" document "+ "On - " + today;
//	        File file = new File(filePath);
//	        
////	        mail boddy added
//	        
//			StringBuilder html = new StringBuilder();
//			html.append("<html>\n" +
//		            "  <head>\n" +
//		            "    <style>\n" +
//		            "      table, th, td {\n" +
//		            "        border: 1px solid black;\n" +
//		            "      }\n" +
//		            "      table {\n" +
//		            "        border-collapse: collapse;\n" +
//		            "      }\n" +
//		            "    </style>\n" +
//		            "  </head>\n" +
//		            "  <body>\n" +
//		            "    <table>\n" +
//		            "      <tr>\n" +
//		            "        <th>Department</th>\n" +
//		            "        <th>Billable Type</th>\n" +
//		            "        <th>Count of employees</th>\n" +
//		            "      </tr>\n");
//			// add rows to the table
//			for(EmployeeDTO dto: dtoList) {
//				html.append("      <tr>\n");
//				  // add cells to the row
//				  html.append("        <td>" + dto.getDepartmentName() + "</td>\n");
//				  html.append("        <td>" + dto.getBillableType() + "</td>\n");
//				  html.append("        <td>" + dto.getCount_of_employees() + "</td>\n");
//				  html.append("      </tr>\n");
//			}
//			
//			html.append("    </table>\n" +
//			            "  </body>\n" +
//			            "</html>");
//			
//			logger.info("HTML content created: {}", html.toString());
//	        
//	        System.out.println("htm content "+html.toString());
//	        
//	        mailService.sendMailWithoutAttachmentWithMailBody(billableMail.toString(),subject,"Dear Directors, <br><br>"
//                    + "I hope this email finds you well. Please find attached the " + "Department wise Billable type document" +" report that containing the latest billable employee data.<br><br>"
//                    		+ "Thank you for your attention to this matter.<br><br>"
//                    		+ "Best regards,<br>"+ html.toString(),file);
//	        
////	        added for cross check
//	        
//	        mailService.sendMailWithoutAttachmentWithMailBody(billableMailTwo.toString(),subject,"Dear Directors, <br><br>"
//                    + "I hope this email finds you well. Please find attached the " + "Department wise Billable type document" +" report that containing the latest billable employee data.<br><br>"
//                    		+ "Thank you for your attention to this matter.<br><br>"
//                    		+ "Best regards,<br>"+ html.toString(),file);
//
//	        System.out.println("Data exported and email sent successfully!");
//	        logger.info("Data exported and email sent successfully!");
//	        
//	        
//
//	    } catch (Exception e) {
//	        e.printStackTrace();
//	        
//	        logger.error("Error occurred in executeBillableReportMethod", e);
//        }
//
//        logger.info("Finished executeBillableReportMethod");
//	}

	
	@Async
	@Scheduled(cron = "${department_wise_billable_report}")
	public void executeBillableReportMethod() {
	    logger.info("Starting executeBillableReportMethod");

	    try {
	        List<Object[]> employees = employeeRepository.getEmployeesBillableDataDepartmentWise();
	        List<EmployeeDTO> dtoList = new ArrayList<>();

	        employees.forEach((object) -> {
	            EmployeeDTO dto = new EmployeeDTO();
	            dto.setDepartmentName(object[0] != null ? object[0].toString() : null);
	            dto.setBillableType(object[1] != null ? object[1].toString() : null);
	            dto.setCount_of_employees(object[2] != null ? Long.parseLong(object[2].toString()) : null);
	            dtoList.add(dto);
	        });

	        String filePath = "Department_BillableData.xlsx";
	        writeToExcelBillableReport(dtoList, filePath);
	        logger.info("Excel file written to {}", filePath);

	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	        String today = LocalDate.now().format(formatter);

	        String subject = "Department wise Billable type document on - " + today;
	        File file = new File(filePath);

	        // Send the full report to directors
	        String directorsMailBody = "Dear Directors, <br><br>"
	                + "I hope this email finds you well. Please find attached the Department wise Billable type document report that contains the latest billable employee data.<br><br>"
	                + "Thank you for your attention to this matter.<br><br>"
	                + "Best regards,<br>"
	                + generateHtmlTable(dtoList);

	        mailService.sendMailWithoutAttachmentWithMailBody(billableMail.toString(), subject, directorsMailBody, file,"");

	        // Send department-specific reports to HODs
	        sendDepartmentSpecificReports(dtoList, subject);

	        System.out.println("Data exported and email sent successfully!");
	        logger.info("Data exported and email sent successfully!");

	    } catch (Exception e) {
	        e.printStackTrace();
	        logger.error("Error occurred in executeBillableReportMethod", e);
	    }

	    logger.info("Finished executeBillableReportMethod");
	}

	private void sendDepartmentSpecificReports(List<EmployeeDTO> dtoList, String subject) throws IOException {
	    Map<String, List<EmployeeDTO>> departmentMap = dtoList.stream()
	            .collect(Collectors.groupingBy(EmployeeDTO::getDepartmentName));

	    for (Map.Entry<String, List<EmployeeDTO>> entry : departmentMap.entrySet()) {
	        String departmentName = entry.getKey();
	        List<EmployeeDTO> departmentList = entry.getValue();

	        String hodEmail = getHodEmailByDepartment(departmentName);  // Implement this method to get HOD email by department
	        if (hodEmail != null) {
	            String departmentFilePath = "Department_BillableData_" + departmentName + ".xlsx";
	            writeToExcelBillableReport(departmentList, departmentFilePath);
	            File departmentFile = new File(departmentFilePath);

	            String departmentMailBody = "Dear HOD,<br><br>"
	                    + "I hope this email finds you well. Please find attached the Billable type document for your department on - " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ".<br><br>"
	                    + "Thank you for your attention to this matter.<br><br>"
	                    + "Best regards,<br>"
	                    + generateHtmlTable(departmentList);

	            // Send mail to HOD and keep Directors in CC
	            try {
					mailService.sendMailWithoutAttachmentWithMailBody(hodEmail, subject, departmentMailBody, departmentFile,billableMail.toString());
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	    }
	}

	private String generateHtmlTable(List<EmployeeDTO> dtoList) {
	    StringBuilder html = new StringBuilder();
	    html.append("<html>\n" +
	            "  <head>\n" +
	            "    <style>\n" +
	            "      table, th, td {\n" +
	            "        border: 1px solid black;\n" +
	            "      }\n" +
	            "      table {\n" +
	            "        border-collapse: collapse;\n" +
	            "      }\n" +
	            "    </style>\n" +
	            "  </head>\n" +
	            "  <body>\n" +
	            "    <table>\n" +
	            "      <tr>\n" +
	            "        <th>Department</th>\n" +
	            "        <th>Billable Type</th>\n" +
	            "        <th>Count of employees</th>\n" +
	            "      </tr>\n");

	    for (EmployeeDTO dto : dtoList) {
	        html.append("      <tr>\n");
	        html.append("        <td>").append(dto.getDepartmentName()).append("</td>\n");
	        html.append("        <td>").append(dto.getBillableType()).append("</td>\n");
	        html.append("        <td>").append(dto.getCount_of_employees()).append("</td>\n");
	        html.append("      </tr>\n");
	    }

	    html.append("    </table>\n" +
	            "  </body>\n" +
	            "</html>");
	    return html.toString();
	}

//	private String getHodEmailByDepartment(String departmentName) {
//	    // Implement logic to get the HOD email by department name
//		
//	    Map<String, String> hodEmails = new HashMap<>();
//	    
//	    //uat mails
//	    hodEmails.put("functional test", "priyadarshini.singh@apmosys.com");
//	    hodEmails.put("INHOUSE", "sakti.das@apmosys.com");
//	    hodEmails.put("Devops", "prarthana.lenka@apmosys.com");
//	    hodEmails.put("FUNCTIONAL TESTING", "rupali.maharanya@apmosys.com");
//	    hodEmails.put("HRM", "neha.borase@apmosys.com");
//	    
//	    //prod mails
//	    
////	    hodEmails.put("Super Admin", "admin3@apmosys.com");
////	    hodEmails.put("Accounts", "finance@apmosys.com");
////	    hodEmails.put("APM", "pradeep.paidisetty@apmosys.com");
////	    hodEmails.put("Application Performance Monitoring", "ramshankar.h@apmosys.com");
////	    hodEmails.put("Automation Testing", "prabhat.padhy@apmosys.com");
////	    hodEmails.put("Business Development", "bishnu.rath@apmosys.com");
////	    hodEmails.put("Development", "bansi.prasad@apmosys.com");
////	    hodEmails.put("Functional Testing", "jayashree.pradhan@apmosys.com");
////	    hodEmails.put("HR", "lituja.mishra@apmosys.com");
////	    hodEmails.put("IT", "prabhat.padhy@apmosys.com");
////	    hodEmails.put("Performance Testing", "pradeep.paidisetty@apmosys.com");
////	    hodEmails.put("Production Support", "ramshankar.h@apmosys.com");
////	    hodEmails.put("Security Testing", "suchitra.mishra@apmosys.com");
////	    hodEmails.put("Admin", "finance@apmosys.com");
////	    hodEmails.put("Director", "bibhu@apmosys.com");
////	    hodEmails.put("Resource Management Group", "lituja.mishra@apmosys.com");
////	    hodEmails.put("Presales", "bansi.prasad@apmosys.com");
////	    hodEmails.put("Production Support 24x7", "ramshankar.h@apmosys.com");
////	    hodEmails.put("Unknown Department", "lituja.mishra@apmosys.com");
////	    hodEmails.put("RPA", "prabhat.padhy@apmosys.com");
////	    hodEmails.put("Products and RND", "prabhat.padhy@apmosys.com");
////	    hodEmails.put("Consultant", "sangeeta@apmosys.com");
//	    
//	 // Add more departments and their respective HOD emails here
//	 // hodEmails.put("Consultant", "sangeeta@apmosys.com");
//	    
//	    return hodEmails.get(departmentName);
//	}

	private Map<String, String> fetchHodEmails() {
        List<Object[]> results = employeeRepository.getHodDepartmentEmail();
        Map<String, String> hodEmails = new HashMap<>();
        for (Object[] result : results) {
            String departmentName = (String) result[0];
            String hodEmail = (String) result[1];
            hodEmails.put(departmentName, hodEmail);
        }
        return hodEmails;
    }

    private String getHodEmailByDepartment(String departmentName) {
        Map<String, String> hodEmails = fetchHodEmails();
        return hodEmails.get(departmentName);
    }

	@Value("${project.insight.upload.directory}")
	private String UPLOAD_DIR;


	// public List<String> projectInsightBulkUpload(List<MultipartFile> files) {
	// 	Map<String, String> response = new ConcurrentHashMap<>();
	// 	ExecutorService executor = null;
		
	// 	try {
	// 		executor = Executors.newFixedThreadPool(Math.min(files.size(), 4)); // Max 4 threads
	// 		String specificFolder = "uploads/project_insight";

	// 		List<Future<Void>> futures = new ArrayList<>();

	// 		for (MultipartFile file : files) {
	// 			if (file == null || file.isEmpty()) {
	// 				response.put(file != null ? file.getOriginalFilename() : "null", "EMPTY_FILE");
	// 				continue;
	// 			}

	// 			Callable<Void> task = () -> {
	// 				String originalFileName = file.getOriginalFilename();
	// 				try {
	// 					String extension = originalFileName.contains(".") 
	// 						? originalFileName.substring(originalFileName.lastIndexOf('.'))
	// 						: "";
						
	// 					String uploadedFileName = "uploaded_" + UUID.randomUUID().toString() + extension;

	// 					Path uploadDir = Paths.get(specificFolder);
	// 					Files.createDirectories(uploadDir); // Create directory if it doesn't exist
						
	// 					Path filePath = uploadDir.resolve(uploadedFileName);
	// 					Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

	// 					response.put(originalFileName, uploadedFileName);
	// 				} catch (IOException exception) {
	// 					exception.printStackTrace();
	// 					response.put(originalFileName, "ERROR_UPLOADING");
	// 				}
	// 				return null;
	// 			};

	// 			futures.add(executor.submit(task));
	// 		}

	// 		// Wait for all tasks to complete
	// 		for (Future<Void> future : futures) {
	// 			try {
	// 				future.get(30, TimeUnit.SECONDS); // Add timeout
	// 			} catch (TimeoutException e) {
	// 				response.put("TIMEOUT", "Upload took too long");
	// 			} catch (InterruptedException | ExecutionException e) {
	// 				e.printStackTrace();
	// 			}
	// 		}
			
	// 	} finally {
	// 		if (executor != null) {
	// 			executor.shutdown();
	// 			try {
	// 				if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
	// 					executor.shutdownNow();
	// 				}
	// 			} catch (InterruptedException e) {
	// 				executor.shutdownNow();
	// 				Thread.currentThread().interrupt();
	// 			}
	// 		}
	// 	}
		
	// 	return response;
	// }

	public List<String> projectInsightBulkUpload(List<MultipartFile> files, String projectName) {
		List<String> uploadedFileNames = Collections.synchronizedList(new ArrayList<>());
		ExecutorService executor = null;

		try {
			executor = Executors.newFixedThreadPool(Math.min(files.size(), 4)); // Max 4 threads
			String specificFolder = UPLOAD_DIR + "/"+ projectName;
			List<Future<Void>> futures = new ArrayList<>();

			for (MultipartFile file : files) {
				if (file == null || file.isEmpty()) {
					continue; // skip empty
				}

				Callable<Void> task = () -> {
					String originalFileName = file.getOriginalFilename();
					try {
						String extension = originalFileName != null && originalFileName.contains(".")
							? originalFileName.substring(originalFileName.lastIndexOf('.'))
							: "";

						String uploadedFileName = "uploaded_("+ originalFileName +")_" + UUID.randomUUID() + extension;

						Path uploadDir = Paths.get(specificFolder);
						Files.createDirectories(uploadDir);

						Path filePath = uploadDir.resolve(uploadedFileName);
						Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

						uploadedFileNames.add(uploadedFileName);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
					return null;
				};

				futures.add(executor.submit(task));
			}

			for (Future<Void> future : futures) {
				try {
					future.get(30, TimeUnit.SECONDS);
				} catch (TimeoutException e) {
					System.err.println("Upload took too long");
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}

		} finally {
			if (executor != null) {
				executor.shutdown();
				try {
					if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
						executor.shutdownNow();
					}
				} catch (InterruptedException e) {
					executor.shutdownNow();
					Thread.currentThread().interrupt();
				}
			}
		}

		return uploadedFileNames;
	}


	public boolean deleteUploadedFile(String fileName, String projectName) {
        try {
			String specificFolder = UPLOAD_DIR + "/"+ projectName;
            Path filePath = Paths.get(specificFolder).resolve(fileName).normalize();
            
            Path uploadDir = Paths.get(specificFolder).toAbsolutePath().normalize();
            if (!filePath.toAbsolutePath().normalize().startsWith(uploadDir)) {
                throw new SecurityException("Invalid file path");
            }
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Boolean> deleteMultipleFiles(List<String> fileNames, String projectName) {
        Map<String, Boolean> result = new HashMap<>();
        for (String fileName : fileNames) {
            result.put(fileName, deleteUploadedFile(fileName, projectName));
        }
        return result;
    }

	public byte[] downloadFile(String fileNames, String projectName) {
		if (fileNames == null || fileNames.isEmpty()) {
            throw new BadRequestException("File names cannot be null or empty");
        }

		String specificFolder = UPLOAD_DIR + "/"+ projectName;

		Path filePath = Paths.get(specificFolder).resolve(fileNames).normalize();

		if(!Files.exists(filePath)) {
			throw new BadRequestException("File not found");
		}

		try {
			byte[] fileBytes = Files.readAllBytes(filePath);
			return fileBytes;
		} catch (IOException e) {
			throw new BadRequestException("File not found");
		}
	}
		
	public byte[] viewProjectInsightFile(String fileNames, String projectName) {
		if(fileNames == null || fileNames.isEmpty()) {
			throw new BadRequestException("File names cannot be null or empty");
		}

		String specificFolder = UPLOAD_DIR + "/"+ projectName;
		
		Path filePath = Paths.get(specificFolder).resolve(fileNames).normalize();
		
		if(!Files.exists(filePath)) {
			throw new BadRequestException("File not found");
		}
		
		try {
			byte[] fileBytes = Files.readAllBytes(filePath);
			return fileBytes;
		} catch (IOException e) {
			throw new BadRequestException("File not found");
		}
	}
}

