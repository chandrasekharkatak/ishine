package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.apmosys.employeeportal.dto.EmployeeDTO;
//import com.apmosys.employeeportal.model.Applications;
import com.apmosys.employeeportal.model.Domain;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Specialization;
//import com.apmosys.employeeportal.repository.ApplicationRepository;
import com.apmosys.employeeportal.repository.DomainRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.SpecializationRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
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
	
	@Value("${billable.mail}")
	private String billableMail;

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
	
	@Scheduled(cron = "${VP_mails}")
	public void execute() {
		try {
			List<Object[]> employees = employeeRepository.getEmployeesWithBillableType();
			List<EmployeeDTO> dtoList = new ArrayList<EmployeeDTO>();
			List<Object[]> findVpEmail = employeeRepository.findAllVPsEmail();
			List<Employee> vpMails = new ArrayList<Employee>();
			
			findVpEmail.forEach((object)->{
				Employee vpDto = new Employee();
				vpDto.setEmail(object[0] != null ? object[0].toString() : null);
				
				vpMails.add(vpDto);
				
			});
			

			StringBuilder mails = new StringBuilder("");
//			vpMails.forEach(object ->{
//				mails.append(object).append(",");
//			});
			vpMails.forEach(object -> {
                if (object.getEmail() != null && !object.getEmail().isEmpty()) {
                    if (mails.length() > 0) {
                        mails.append(",");
                    }
                    mails.append(object.getEmail());
                }
            });
			
			employees.forEach((object)->{
				EmployeeDTO dto = new EmployeeDTO();
				
//				dto.setEmpId(object[0] != null ? Long.parseLong(object[0].toString()) : null);
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
				});
			
			 String filePath = "EmployeeBillableData.xlsx";
	            writeToExcel(dtoList, filePath);

	            // Prepare email body and attachment
	            String subject = "Regarding Billable non Billable data";
	            File file = new File(filePath);
	            
//	            iterate mail
	            for (Employee employee : vpMails) {
	            	mailService.sendMailWithoutAttachment(employee.getEmail(),subject,"Dear Vice Presidents, <br><br>"
	                        + "I hope this email finds you well. Please find attached the " + subject + " document containing the latest billable employee data.<br><br>"
	                        		+ "Thank you for your attention to this matter.<br><br>"
	                        		+ "Best regards,<br>",file);
	                System.out.println("Data exported and email sent successfully!");
				}
	            
            
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
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

//	@Scheduled(cron = "0 */2 * * * *")
	@Scheduled(cron = "${department_wise_billable_report}")
	public void executeBillableReportMethod() {
	    try {
	        List<Object[]> employees = employeeRepository.getEmployeesBillableDataDepartmentWise();
	        List<EmployeeDTO> dtoList = new ArrayList<>();

	        employees.forEach((object)->{
				EmployeeDTO dto = new EmployeeDTO();
				
				dto.setDepartmentName(object[0] != null ? object[0].toString() : null);		
				dto.setBillableType(object[1] != null ? object[1].toString() : null);
				dto.setCount_of_employees(object[2] != null ? Long.parseLong(object[2].toString()) : null);			
				
				dtoList.add(dto);
				});

	        String filePath = "Department_BillableData.xlsx";
	        writeToExcelBillableReport(dtoList, filePath);

	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	        LocalDate todayDate = LocalDate.now();
	        
	        String today = LocalDate.now().format(formatter);
	        
	        
	        String subject = "Department wise Billable type document on - " + today;
	        File file = new File(filePath);
	        
//	        mail boddy added
	        
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
			// add rows to the table
			for(EmployeeDTO dto: dtoList) {
				html.append("      <tr>\n");
				  // add cells to the row
				  html.append("        <td>" + dto.getDepartmentName() + "</td>\n");
				  html.append("        <td>" + dto.getBillableType() + "</td>\n");
				  html.append("        <td>" + dto.getCount_of_employees() + "</td>\n");
				  html.append("      </tr>\n");
			}
			
			html.append("    </table>\n" +
			            "  </body>\n" +
			            "</html>");
	        
	        
	        System.out.println("htm content "+html.toString());
	        
	        mailService.sendMailWithoutAttachmentWithMailBody(billableMail.toString(),subject,"Dear Directors, <br><br>"
                    + "I hope this email finds you well. Please find attached the " + "Department wise Billable type document" +" report that containing the latest billable employee data.<br><br>"
                    		+ "Thank you for your attention to this matter.<br><br>"
                    		+ "Best regards,<br>"+ html.toString(),file);

	        System.out.println("Data exported and email sent successfully!");

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	
	
}
