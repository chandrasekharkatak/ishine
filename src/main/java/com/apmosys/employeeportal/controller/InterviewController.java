package com.apmosys.employeeportal.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.apmosys.employeeportal.JobRoleAccess;
import com.apmosys.employeeportal.dto.InterviewDTO;
import com.apmosys.employeeportal.service.InterviewService;
import com.apmosys.employeeportal.utility.InterviewConstants;
import com.apmosys.employeeportal.utility.ServiceResponse;

@RestController
@RequestMapping(path = "/api")
public class InterviewController {

    @Autowired
    InterviewService interviewService;

    @Value("${file.location.documents.interview:./uploads/interviews}")
    private String interviewFileLocation;

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/scheduleInterview", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse scheduleInterview(
            @RequestPart("dto") InterviewDTO interviewDTO,
            @RequestPart(value = "resume", required = false) MultipartFile resume) {

        String filePath = null;
        String fileName = null;

        if (resume != null && !resume.isEmpty()) {
            try {
                Path uploadDir = Paths.get(interviewFileLocation);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String timestamp = String.valueOf(System.currentTimeMillis());
                String originalFilename = resume.getOriginalFilename();
                String newFileName = "resume_" + timestamp + "_" + originalFilename;

                Path filePathObj = uploadDir.resolve(newFileName);
                Files.write(filePathObj, resume.getBytes());

                filePath = "/" + newFileName;
                fileName = originalFilename;
            } catch (IOException e) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                errorResponse.setServiceResponse("Failed to upload resume: " + e.getMessage());
                return errorResponse;
            }
        }

        interviewDTO.setResumeFileName(fileName);
        interviewDTO.setResumeFilePath(filePath);

        ServiceResponse response = interviewService.scheduleInterview(interviewDTO);
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/updateInterview", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ServiceResponse updateInterview(
            @RequestPart("dto") InterviewDTO interviewDTO,
            @RequestPart(value = "resume", required = false) MultipartFile resume) {

        if (resume != null && !resume.isEmpty()) {
            try {
                Path uploadDir = Paths.get(interviewFileLocation);
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                String timestamp = String.valueOf(System.currentTimeMillis());
                String originalFilename = resume.getOriginalFilename();
                String newFileName = "resume_" + timestamp + "_" + originalFilename;

                Path filePathObj = uploadDir.resolve(newFileName);
                Files.write(filePathObj, resume.getBytes());

                interviewDTO.setResumeFileName(originalFilename);
                interviewDTO.setResumeFilePath("/" + newFileName);
            } catch (IOException e) {
                ServiceResponse errorResponse = new ServiceResponse();
                errorResponse.setServiceStatus(ServiceResponse.STATUS_FAIL);
                errorResponse.setServiceResponse("Failed to upload resume: " + e.getMessage());
                return errorResponse;
            }
        }

        ServiceResponse response = interviewService.updateInterview(interviewDTO);
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/getAllInterviews", method = RequestMethod.POST)
    public ServiceResponse getAllInterviews(@RequestPart("dto") InterviewDTO interviewDTO) {
        ServiceResponse response = interviewService.getAllInterviews(interviewDTO);
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/getInterviewById", method = RequestMethod.POST)
    public ServiceResponse getInterviewById(@RequestPart("dto") InterviewDTO interviewDTO) {
        ServiceResponse response = interviewService.getInterviewById(interviewDTO);
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/deleteInterview", method = RequestMethod.POST)
    public ServiceResponse deleteInterview(@RequestPart("dto") InterviewDTO interviewDTO) {
        ServiceResponse response = interviewService.deleteInterview(interviewDTO);
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/getInterviewDropdownData", method = RequestMethod.GET)
    public ServiceResponse getInterviewDropdownData() {
        ServiceResponse response = interviewService.getDropdownData();
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/getEmployeesByDepartment", method = RequestMethod.GET)
    public ServiceResponse getEmployeesByDepartment(@RequestParam("departmentId") Long departmentId) {
        ServiceResponse response = interviewService.getEmployeesByDepartmentId(departmentId);
        return response;
    }
    
    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/getProjectsByClient", method = RequestMethod.GET)
    public ServiceResponse getProjectsByClient(@RequestParam("clientName") String clientName) {
        ServiceResponse response = interviewService.getProjectsByClientName(clientName);
        return response;
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/interview/resume/{fileName}", method = RequestMethod.GET)
    public ResponseEntity<Resource> downloadResume(@PathVariable("fileName") String fileName) throws IOException {
        if (!interviewService.canDownloadResume(fileName)) {
            return ResponseEntity.status(403).build();
        }
        Path filePath = Paths.get(interviewFileLocation).resolve(fileName).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @JobRoleAccess(featureIds = {InterviewConstants.INTERVIEW_FEATURE_ID})
    @RequestMapping(value = "/updateInterviewStatus", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ServiceResponse updateInterviewStatus(@RequestBody InterviewDTO interviewDTO) {
        ServiceResponse response = interviewService.updateInterviewStatus(interviewDTO);
        return response;
    }
}
