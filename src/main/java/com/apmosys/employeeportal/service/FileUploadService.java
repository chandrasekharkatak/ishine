package com.apmosys.employeeportal.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
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

	                Optional<Employee> optionalEmployee = Optional.ofNullable(employeeRepository.findByEmployeementId(employmentId));
	                
	                
	                if (optionalEmployee.isPresent()) {
	                	Employee employee = optionalEmployee.get();
	                    employee.setBillable(billable);
	                    employee.setBillableType(billableType);
	                
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
	@Scheduled(cron = "0 0 7 ? * *")
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
            mailService.sendMailWithAttachment(mails.toString(),"bansi.prasad@apmosys.com",subject,"Dear Vice Presidents, <br><br>"
                    + "I hope this email finds you well. Please find attached the " + subject + " document containing the latest billable employee data.<br><br>"
                    		+ "Thank you for your attention to this matter.<br><br>"
                    		+ "Best regards,<br>",file);
            System.out.println("Data exported and email sent successfully!");
		
		} catch (Exception e) {
			e.printStackTrace();
			
		}
	}
	
	
	
}
