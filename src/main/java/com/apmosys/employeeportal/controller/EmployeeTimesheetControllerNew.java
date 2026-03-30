package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.Exception.TimesheetValidationFailedException;
import com.apmosys.employeeportal.dto.FileNameRequest;
import com.apmosys.employeeportal.dto.FileNameResponse;
import com.apmosys.employeeportal.dto.GetEmployeeSummaryOnExportDTO;
import com.apmosys.employeeportal.dto.GetProjectListForDateAndEmpIdPayload;
import com.apmosys.employeeportal.dto.FinalBulkUploadDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.FinalDocumentDownloadPayloadDTO;
import com.apmosys.employeeportal.dto.GetTimesheetDashboardCountForEmployeeDTO;
import com.apmosys.employeeportal.dto.TimesheetApprovalNewDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.EmployeeTimesheetDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDeleteRequestDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetStatusUpdateRequestDTO;
import com.apmosys.employeeportal.exception.UnauthorizedAccessException;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.service.TimesheetDocumentServiceNew;
import com.apmosys.employeeportal.service.TimesheetServiceNew;
import com.apmosys.employeeportal.service.helper.TimesheetEncryptionHelper;
import com.apmosys.employeeportal.utility.FileNameGenerator;
import com.apmosys.employeeportal.utility.ServiceResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * New Controller for Hierarchical Timesheet APIs
 * 
 * This controller handles all new hierarchical timesheet operations:
 * - EmployeeTimesheet (one per day per employee)
 * - ProjectTimesheets (multiple per day)
 * - Activities (nested under projects)
 * 
 * @author System
 * @version 2.0
 */
@RestController
@RequestMapping(path = "/api/v2/timesheet")
@Slf4j
public class EmployeeTimesheetControllerNew {
	
	@Autowired
	TimesheetServiceNew timesheetServiceNew;
	
	@Autowired
	TimesheetEncryptionHelper timesheetEncryptionHelper;

	@Autowired
	TimesheetDocumentServiceNew timesheetDocumentServiceNew;

	@Autowired
	com.apmosys.employeeportal.service.TimesheetService timesheetService;

	
	
	@Value("${timesheet.minus.days.for.bulk.upload}")
	private Integer minusDays;

	@Value("${check.minus.days.for.bulk.upload}")
	private Boolean checkMinusDaysForBulkUpload;
	/**
	 * API 1.1: Create Timesheet (New Hierarchical Structure)
	 * Endpoint: POST /api/v2/timesheet/create
	 * 
	 * Creates EmployeeTimesheet, ProjectTimesheets, and Activities in a single transaction.
	 * 
	 * NEW CONTRACT: Accepts list of multipart files for document uploads
	 * Multiple documents can be uploaded for multiple projects in a single timesheet
	 * 
	 * @param encryptedDto Encrypted timesheet DTO (new contract structure)
	 * @param documents List of multipart files for document uploads (one per project)
	 *                  Documents are linked to projects via documentData in DTO
	 * @return ServiceResponse with created timesheet data
	 */
	@PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse createTimesheet(
	        @RequestPart("dto") String encryptedDto,
	        @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {

	    if (encryptedDto == null || encryptedDto.trim().isEmpty()) {
	        throw new TimesheetValidationFailedException("Request data is required.");
	    }

	    EmployeeTimesheetDTO dto;
	    try {
	        dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNewMapping(encryptedDto);
	    } catch (Exception e) {
	        log.error("Decryption/parsing failed - error: {}", e.getMessage(), e);
	        throw new TimesheetValidationFailedException("Invalid or corrupted request data. Please try again.");
	    }

	    return timesheetServiceNew.createTimesheet(dto, documents);
	}
	
	@JobRoleAccess(featureIds = {15, 16, 24})
    @GetMapping("/getByEmployee")
    public ServiceResponse getTimesheetsByEmployee(
            @RequestParam Long empId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return timesheetServiceNew.getTimesheetsByEmployee(empId, startDate, endDate);
    }
    
    /**
     * UPDATE - Update existing timesheet
     * PUT /api/v2/timesheet/update
     * 
     * NEW CONTRACT: Accepts list of multipart files for document uploads
     * Multiple documents can be uploaded/updated for multiple projects
     * 
     * UPDATE-SPECIFIC VALIDATIONS:
     * - timesheetId must be provided and valid
     * - Timesheet must exist (validated in service layer)
     * - Date cannot be locked (validated in service layer)
     * - Cannot update timesheets older than lock period
     * 
     * @param timesheetId Timesheet ID to update (required)
     * @param encryptedDto Encrypted timesheet DTO (new contract structure)
     * @param documents List of multipart files for document uploads (one per project)
     *                  Documents are linked to projects via documentData in DTO
     * @return ServiceResponse with updated timesheet data
     */
    @JobRoleAccess(featureIds = {15})
    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse updateTimesheet(
            @RequestPart("dto") String encryptedDto,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {

    	Long timesheetId=null;

        if (encryptedDto == null || encryptedDto.trim().isEmpty()) {
            throw new TimesheetValidationFailedException("Request data is required.");
        }

        EmployeeTimesheetDTO dto;
        try {
            dto = timesheetEncryptionHelper.decryptAndParseTimesheetDtoNewMapping(encryptedDto);
			 timesheetId=dto.getTimesheetId();
			if (timesheetId == null) {
				throw new TimesheetValidationFailedException("Timesheet to update is required.");
			}
            log.debug("Decrypted DTO for update - timesheetId: {}, empId: {}, date: {}",
                    timesheetId, dto.getEmpId(), dto.getDate());
        } catch (Exception e) {
            log.error("Decryption/parsing failed for update - timesheetId: {}, error: {}",
                     e.getMessage(), e);
            throw new TimesheetValidationFailedException("Invalid or corrupted request data. Please try again.");
        }

        return timesheetServiceNew.updateTimesheet(timesheetId, dto, documents);
    }
    
    /**
     * Lightweight summary API used by My Timesheets mini dashboard.
     * Returns totalFilled, totalApproved, totalRejected for given empId and date range.
     */
    @JobRoleAccess(featureIds = {15, 16, 24})
    @PostMapping("/summary")
    public ServiceResponse getMyTimesheetSummary(@RequestBody com.apmosys.employeeportal.dto.TimesheetDTO timesheetDTO) {
        return timesheetService.getMyTimesheetSummary(timesheetDTO);
    }
	
	/**
	 * API 1.3: Get Timesheet by ID
	 * Endpoint: GET /api/v2/timesheet/{timesheetId}
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@GetMapping(value = "/{timesheetId}")
	public ServiceResponse getTimesheetById(@PathVariable Long timesheetId) {
		ServiceResponse response = timesheetServiceNew.getTimesheetById(timesheetId);
		return response;
	}
	
	/**
	 * API 1.4: Get Timesheet by Date
	 * Endpoint: POST /api/v2/timesheet/by-date
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@PostMapping(value = "/by-date")
	public ServiceResponse getTimesheetByDate(@RequestBody EmployeeTimesheetDTO requestDTO) {
		ServiceResponse response = timesheetServiceNew.getTimesheetByDate(requestDTO);
		return response;
	}

	/**
	 * API 1.8: Delete Timesheet
	 * Endpoint: DELETE /api/v2/timesheet/{timesheetId}
	 */
	@JobRoleAccess(featureIds = {15, 16})
	@DeleteMapping(value = "/{timesheetId}")
	public ServiceResponse deleteTimesheet(@PathVariable Long timesheetId) {
		ServiceResponse response = timesheetServiceNew.deleteTimesheet(timesheetId);
		return response;
	}
	
	@JobRoleAccess(featureIds = {15,16})
	@RequestMapping(value = "/getAllMyTimesheetsByEmpId", method = RequestMethod.POST)
	public ServiceResponse getAllMyTimesheetsByEmpId(@RequestBody TimesheetDTO timesheetDTO) {

		ServiceResponse response = timesheetServiceNew.getAllMyTimesheetsByEmpId(timesheetDTO);
		return response;
	}

	@JobRoleAccess(featureIds = {15,16})
	@PostMapping("/getTimesheetMetadataByEmpId")
	public ServiceResponse getTimesheetMetadataByEmpId(@RequestBody TimesheetDTO timesheetDTO) {
		return timesheetServiceNew.getTimesheetMetadataByEmpId(timesheetDTO);
	}
	
	@JobRoleAccess(featureIds = {15})
	 @PostMapping("/getActiveProjectsAndClientSideIdByEmpId")
	 public ServiceResponse getActiveProjectsAndClientSideIdByEmpId(@RequestBody Long empId) {
	     return timesheetServiceNew.getActiveProjectsAndClientSideIdByEmpId(empId);
	 }

	/**
	 * API 1.11: Get Document Data by Doc ID, this is for viewing the doc
	 * Endpoint: GET /api/v2/timesheet/getDocumentDataByDocId
	 */
	@JobRoleAccess(featureIds = { 15, 16, 24 })
	@GetMapping("/getDocumentDataByDocId")
	public ResponseEntity<Resource> getDocumentDataByDocId(@RequestParam Long docId, @RequestParam Boolean approvedDocType) throws IOException {

		Resource resource = timesheetServiceNew.getDocumentDataByDocId(docId, approvedDocType);

		if (resource == null) {
			return ResponseEntity.notFound().build();
		}

		Path path = resource.getFile().toPath();

		String contentType = Files.probeContentType(path);
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"inline; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	/**
	 * Document preview by ID - supports path /api/v2/timesheet/document/getById/{docId}
	 * approvedDocType: false = Filled (TimesheetDocumentDetailsNew), true = Approved (FinalDocumentNew)
	 */
	@JobRoleAccess(featureIds = { 15, 16, 24 })
	@GetMapping("/document/getById/{docId}")
	public ResponseEntity<Resource> getDocumentById(
			@PathVariable Long docId,
			@RequestParam(required = false) Boolean approvedDocType) throws IOException {
		Resource resource = timesheetServiceNew.getDocumentDataByDocId(docId, approvedDocType);
		if (resource == null) {
			return ResponseEntity.notFound().build();
		}
		Path path = resource.getFile().toPath();
		String contentType = Files.probeContentType(path);
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}

	/**
	 * API: Get autofill template for last working-day timesheet.
	 * Endpoint: POST /api/v2/timesheet/autofill
	 *
	 * Request body: EmployeeTimesheetDTO with empId and date (target date for which user is filling).
	 * Behaviour:
	 * - Looks for last working-day timesheet strictly before target date.
	 * - Verifies all projects are still active for the employee.
	 * - Returns full hierarchical DTO (without documents) to be used as autofill template.
	 */
	@PostMapping("/autofill")
	public ServiceResponse getAutofillTimesheetTemplate(@RequestBody EmployeeTimesheetDTO requestDTO) {

		if (requestDTO == null || requestDTO.getEmpId() == null || requestDTO.getDate() == null) {
			ServiceResponse response = new ServiceResponse();
			response.setServiceStatus(ServiceResponse.STATUS_FAIL);
			response.setServiceResponse("Employee and date are required for autofill.");
			response.setServiceError("Missing empId or date in autofill request");
			return response;
		}

		return timesheetServiceNew.getAutofillTimesheetTemplate(requestDTO.getEmpId(), requestDTO.getDate());
	}

	@JobRoleAccess(featureIds = {15})
	@PostMapping(value = "/bulkFinalDocumentUpload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse bulkFinalDocumentUpload(
			@RequestPart("finalFile") MultipartFile file,
			@RequestParam("fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
			@RequestParam("toDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
			@RequestParam("empId") Long empId) throws Exception{
		
		System.out.println("Received file: " + file.getOriginalFilename());
		System.out.println("From Date: " + fromDate);
		System.out.println("To Date: " + toDate);
		
		ServiceResponse reponse = new ServiceResponse();
		reponse = timesheetDocumentServiceNew.replaceAllTemporaryFileWithFinalFile(file,fromDate,toDate,empId);
		return reponse;
	}

	@JobRoleAccess(featureIds = {15})
	@DeleteMapping(value = "/deleteBulkFinalDocument/{bulkApproverDocId}")
	public ServiceResponse deleteBulkFinalDocument(@PathVariable Long bulkApproverDocId) {
		ServiceResponse response = timesheetDocumentServiceNew.deleteBulkApprovedDocuments(bulkApproverDocId);
		return response;
	}

	 @PostMapping(value = "/getTimesheetDashboardCountForEmployee")
	 public ServiceResponse getTimesheetDashboardCountForEmployee(@RequestBody GetTimesheetDashboardCountForEmployeeDTO payload) {  
		    Integer month = (Integer) payload.getMonth();
		    Integer year = (Integer) payload.getYear();
		    Long empId = Long.valueOf(payload.getEmpId());
			Boolean isClientDashboard =  Boolean.valueOf(payload.getIsClientDashboard());
//			String billableType=String.valueOf(payload.getSelectedBillableType());
		    List<String> billableTypes = payload.getSelectedBillableTypes(); // use the list
			String employeeActive = String.valueOf(payload.getSelectedEmployeeStatus());
		 ServiceResponse reponse= timesheetServiceNew.getTimesheetDashboardCountForEmployee(month,year,empId,isClientDashboard,billableTypes,employeeActive,payload.getClientSideFilter());
		  return reponse;
	 }

	 @PostMapping("/bulkApproveOrRejectTimesheet")
	 public ServiceResponse bulkApproveOrRejectTimesheet(@RequestBody TimesheetApprovalNewDTO data) {
		 ServiceResponse reponse= timesheetServiceNew.bulkApproveOrRejectTimesheet(data);
		  return reponse;
	 }
	 
     // changes have been made in the timesheetservicenew and new timesheet repo 
	 
	 @PostMapping(value = "/getEmployeeViewForClientAttendanceStatus")
		public ServiceResponse getEmployeeViewForClientAttendanceStatus(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
			 ServiceResponse reponse= timesheetServiceNew.getEmployeeViewForClientAttendanceStatus(object);
			 return reponse;
		}
	 
	 

	@JobRoleAccess(featureIds = {15,16})
	@PostMapping(value = "/bulkFinalUploadProjectBased", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ServiceResponse bulkFinalUploadProjectBased(
			@RequestPart("finalFile") MultipartFile file, @RequestPart("finalBulkUploadDTO") FinalBulkUploadDTO finalBulkUploadDTO ) {

		ServiceResponse reponse= timesheetServiceNew.bulkFinalUploadProjectBased(finalBulkUploadDTO, file);
		return reponse;
	}

	@JobRoleAccess(featureIds = {15,16})
	@GetMapping("/getPreviousMinusDays")
	public ServiceResponse getPreviousMinusDays() {
		ServiceResponse response = new ServiceResponse();
		try {
			response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
			Map<String, Object> map = new HashMap<>();
			map.put("minusDays", minusDays);
			map.put("checkMinusDaysForBulkUpload", checkMinusDaysForBulkUpload);
			response.setServiceResponse(map);
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
			response.setServiceError(ServiceResponse.STATUS_FAIL);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return response;
		}
	}

	@PostMapping(value = "/getMyReporteesAndClientSideProjectsInMonthYear")
	public ServiceResponse getMyReporteesAndClientSideProjectsInMonthYear(@RequestBody TimesheetDTO timesheetDTO) {  
		 ServiceResponse reponse= timesheetServiceNew.getMyReporteesAndClientSideProjectsInMonthYear(timesheetDTO);
		 return reponse;
	}
	
	/* The changes have been made in EmployeeTimesheetsNewRepository for the project view 
	 * Similarly in TimesheetDashboardService for the following method
	 * and the same controller is present in TimesheetController pointing to timesheetService(old)
	 * 
	 */
	 @GetMapping(value = "/getTimesheetDashboardCountForProject")
	 public ServiceResponse getTimesheetDashboardCountForProject(@RequestParam Integer month, @RequestParam Integer year,@RequestParam Long empId,
			 @RequestParam Boolean isClientDashboard,@RequestParam List<String> billableType,@RequestParam String projectActive) {  
		 ServiceResponse reponse= timesheetServiceNew.getTimesheetDashboardCountForProject(month,year,empId,isClientDashboard,billableType,projectActive);
		  return reponse;
	 }
	
	 /* The Required changes have been made in repository methods 
	  * 
	  * */
	 @PostMapping(value = "/getProjectViewForClientAttendanceStatus")
	 public ServiceResponse getProjectViewForClientAttendanceStatus(@RequestBody TimesheetDTO timesheetDTO) {

		 ServiceResponse reponse= timesheetServiceNew.getProjectViewForClientAttendanceStatus(timesheetDTO);
	     return reponse;
	     
	 }
	 
	 @PostMapping(value = "/getEmployeeSummaryOnExport")
		public ServiceResponse getEmployeeSummaryOnExport(@RequestBody GetEmployeeSummaryOnExportDTO object) {  
			 ServiceResponse reponse= timesheetServiceNew.getEmployeeSummaryOnExportAccordingToStatus(object);
			 return reponse;
		}

	@PostMapping("/getDocumentsByEmpAndDate")
	public ResponseEntity<Resource> getDocumentsByEmpAndDate(@RequestParam(required = true) Long empId, @RequestParam(required = true) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date, @RequestParam(required = true) Integer projectId ) {
		Resource resource = timesheetServiceNew.getDocumentsByEmpAndDate(empId,date,projectId);

		if(resource == null){
			return ResponseEntity.noContent().build();
		}

		String contentType = "application/octet-stream";
		String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";
		String fileName = resource.getFilename();
		 contentType = timesheetServiceNew.detectContentType(fileName);

		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
				.body(resource);
	}

	@PostMapping("/downloadFinalDocuments")
	public void downloadFinalDocuments(@RequestBody List<FinalDocumentDownloadPayloadDTO> dto, 
									HttpServletResponse response) {
		try {
			timesheetServiceNew.streamFinalDocumentsZip(
					dto,
					response
			);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

		@PostMapping("/checkEditAllowed")
		public ResponseEntity<Boolean> checkEditAllowed(@RequestBody Map<String, Object> payload) {

			Long timesheetId = Long.valueOf(payload.get("timesheetId").toString());
			String dateStr = payload.get("date").toString();

			// convert to LocalDate (expects yyyy-MM-dd)
			LocalDate date = LocalDate.parse(dateStr);

			boolean isAllowed = timesheetServiceNew.isEditAllowed(timesheetId, date);

			return ResponseEntity.ok(isAllowed);
		}
	@GetMapping("/half-day/{empId}")
    public ResponseEntity<List<LocalDate>> getAllHalfDayLeave(@PathVariable Long empId) {
        List<LocalDate> halfDayDates = timesheetServiceNew.getAllHalfDayLeaves(empId);
        return ResponseEntity.ok(halfDayDates);
    }

	@PostMapping("/generate-name")
    public ResponseEntity<FileNameResponse> generateFileName(
            @RequestBody FileNameRequest request
    ) {
		try{

			String fileName = FileNameGenerator.generate(request.getProjectId(), request.getExtension(), request.getDocType());
			return ResponseEntity.ok(new FileNameResponse(fileName));
		}catch(Exception e){
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
    }
		
		@GetMapping("/getMyLastFilledLocationIdForProjectAndEmp")
		public ServiceResponse getMyLastFilledLocationIdForProjectAndEmp(@RequestParam Long empId, @RequestParam Integer projectId) {
			ServiceResponse response = new ServiceResponse();
			response = timesheetServiceNew.getMyLastFilledLocationIdForProjectAndEmp(projectId,empId);
			return response;
		}
		
		@GetMapping("/fetchDeptBaseProjectAndClientRelatedDataForEmployee")
		public ServiceResponse createTimesheetForEmployeeWithoutProject(@RequestParam Long empId) {
			ServiceResponse response = new ServiceResponse();
			response = timesheetServiceNew.fetchDeptBaseProjectAndClientRelatedDataForEmployee(empId);
			return response;
		}
		
		
		/*Migrated*/
		// @JobRoleAccess(featureIds = {15})
		 @PostMapping("/getProjectListForDateAndEmpId")
		 public ServiceResponse getProjectListForDateAndEmpId(@RequestBody GetProjectListForDateAndEmpIdPayload payload) {
		     return timesheetServiceNew.getProjectListForDateAndEmpId(payload);
		 }
}


