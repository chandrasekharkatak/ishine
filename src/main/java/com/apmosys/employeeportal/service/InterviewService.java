package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.InterviewDTO;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.Interview;
import com.apmosys.employeeportal.repository.InterviewRepository;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class InterviewService {

    @Autowired
    InterviewRepository interviewRepository;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    private LogService logService;

    @Transactional
    public ServiceResponse scheduleInterview(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Schedule Interview");
        apiLogInfo.setApiUrl("/api/scheduleInterview");
        apiLogInfo.setLogLevel("INFO");

        try {
            Interview interview = new Interview();
            interview.setTitle(interviewDTO.getTitle());
            interview.setDate(interviewDTO.getDate());
            interview.setTime(interviewDTO.getTime());
            interview.setClient(interviewDTO.getClient());
            interview.setRole(interviewDTO.getRole());
            interview.setProject(interviewDTO.getProject());
            interview.setDepartmentId(interviewDTO.getDepartmentId());
            interview.setEmployeeId(interviewDTO.getEmployeeId());
            interview.setMode(interviewDTO.getMode());
            interview.setInterviewStatus(interviewDTO.getInterviewStatus() != null ? interviewDTO.getInterviewStatus() : "Scheduled");
            interview.setSelectionStatus(interviewDTO.getSelectionStatus() != null ? interviewDTO.getSelectionStatus() : "Pending");
            interview.setOnboardingStatus(interviewDTO.getOnboardingStatus() != null ? interviewDTO.getOnboardingStatus() : "Not Applicable");
            interview.setInterviewRemarks(interviewDTO.getInterviewRemarks());
            interview.setSelectionRemarks(interviewDTO.getSelectionRemarks());
            interview.setOnboardingRemarks(interviewDTO.getOnboardingRemarks());
            interview.setJd(interviewDTO.getJd());
            interview.setInterviewerName(interviewDTO.getInterviewerName());
            interview.setScheduledById(interviewDTO.getScheduledById());
            interview.setAdditionalNotes(interviewDTO.getAdditionalNotes());
            interview.setResumeFileName(interviewDTO.getResumeFileName());
            interview.setResumeFilePath(interviewDTO.getResumeFilePath());

            interview.getCommonProperty().setCreatedBy(interviewDTO.getCreatedBy());
            interview.getCommonProperty().setCreatedOn(new Timestamp(System.currentTimeMillis()));
            interview.getCommonProperty().setUpdatedBy(interviewDTO.getCreatedBy());
            interview.getCommonProperty().setUpdatedOn(LocalDateTime.now());

            Interview saved = interviewRepository.save(interview);

            apiLogInfo.setApiRequest("Interview: " + interviewDTO.getTitle());
            apiLogInfo.setApiResponse("Interview scheduled successfully. ID: " + saved.getId());
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Interview scheduled successfully");
            response.setServiceResponse1(saved.getId());
        } catch (Exception e) {
            apiLogInfo.setApiRequest("Interview: " + interviewDTO.getTitle());
            apiLogInfo.setApiError("Error scheduling interview: " + e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.getStackTrace().toString());
        }
        return response;
    }

    @Transactional
    public ServiceResponse updateInterview(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Update Interview");
        apiLogInfo.setApiUrl("/api/updateInterview");
        apiLogInfo.setLogLevel("INFO");

        try {
            Interview existing = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (existing == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }

            existing.setTitle(interviewDTO.getTitle());
            existing.setDate(interviewDTO.getDate());
            existing.setTime(interviewDTO.getTime());
            existing.setClient(interviewDTO.getClient());
            existing.setRole(interviewDTO.getRole());
            existing.setProject(interviewDTO.getProject());
            existing.setDepartmentId(interviewDTO.getDepartmentId());
            existing.setEmployeeId(interviewDTO.getEmployeeId());
            existing.setMode(interviewDTO.getMode());
            existing.setInterviewStatus(interviewDTO.getInterviewStatus());
            existing.setSelectionStatus(interviewDTO.getSelectionStatus());
            existing.setOnboardingStatus(interviewDTO.getOnboardingStatus());
            existing.setInterviewRemarks(interviewDTO.getInterviewRemarks());
            existing.setSelectionRemarks(interviewDTO.getSelectionRemarks());
            existing.setOnboardingRemarks(interviewDTO.getOnboardingRemarks());
            existing.setJd(interviewDTO.getJd());
            existing.setInterviewerName(interviewDTO.getInterviewerName());
            existing.setAdditionalNotes(interviewDTO.getAdditionalNotes());
            if (interviewDTO.getResumeFileName() != null) {
                existing.setResumeFileName(interviewDTO.getResumeFileName());
            }
            if (interviewDTO.getResumeFilePath() != null) {
                existing.setResumeFilePath(interviewDTO.getResumeFilePath());
            }

            existing.getCommonProperty().setUpdatedBy(interviewDTO.getUpdatedBy());
            existing.getCommonProperty().setUpdatedOn(LocalDateTime.now());

            Interview saved = interviewRepository.save(existing);

            apiLogInfo.setApiRequest("Interview ID: " + interviewDTO.getId());
            apiLogInfo.setApiResponse("Interview updated successfully. ID: " + saved.getId());
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Interview updated successfully");
        } catch (Exception e) {
            apiLogInfo.setApiRequest("Interview ID: " + interviewDTO.getId());
            apiLogInfo.setApiError("Error updating interview: " + e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.getStackTrace().toString());
        }
        return response;
    }

    public ServiceResponse getAllInterviews(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setSubFeatureName("Get All Interviews");
        apiLogInfo.setApiUrl("/api/getAllInterviews");
        apiLogInfo.setLogLevel("INFO");

        try {
            List<Object[]> results;
            String startDate = interviewDTO.getStartDate();
            String endDate = interviewDTO.getEndDate();

            List<String> clients = interviewDTO.getClients();
            List<Long> departmentIds = interviewDTO.getDepartmentIds();
            List<Long> employeeIds = interviewDTO.getEmployeeIds();

            if ((clients == null || clients.isEmpty()) && (departmentIds == null || departmentIds.isEmpty()) && (employeeIds == null || employeeIds.isEmpty())) {
                results = interviewRepository.findAllInterviews(startDate, endDate);
            } else {
                results = new ArrayList<>();
                if (clients != null && !clients.isEmpty()) {
                    for (String client : clients) {
                        results.addAll(interviewRepository.findAllInterviewsFiltered(startDate, endDate, client, null, null));
                    }
                }
                if (departmentIds != null && !departmentIds.isEmpty()) {
                    for (Long deptId : departmentIds) {
                        results.addAll(interviewRepository.findAllInterviewsFiltered(startDate, endDate, null, deptId, null));
                    }
                }
                if (employeeIds != null && !employeeIds.isEmpty()) {
                    for (Long empId : employeeIds) {
                        results.addAll(interviewRepository.findAllInterviewsFiltered(startDate, endDate, null, null, empId));
                    }
                }
            }

            List<InterviewDTO> interviewList = new ArrayList<>();
            for (Object[] row : results) {
                InterviewDTO dto = mapRowToDTO(row);
                interviewList.add(dto);
            }

            String sortColumn = interviewDTO.getSortColumn();
            String sortDirection = interviewDTO.getSortDirection();
            if (sortColumn != null && sortDirection != null) {
                interviewList.sort((a, b) -> {
                    int cmp = 0;
                    try {
                        String valA = getFieldValue(a, sortColumn);
                        String valB = getFieldValue(b, sortColumn);
                        if (sortColumn.equals("date")) {
                            cmp = valA.compareTo(valB);
                        } else {
                            cmp = valA.compareToIgnoreCase(valB);
                        }
                    } catch (Exception e) {
                        cmp = 0;
                    }
                    return sortDirection.equals("asc") ? cmp : -cmp;
                });
            }

            Integer page = interviewDTO.getPage() != null ? interviewDTO.getPage() : 0;
            Integer size = interviewDTO.getSize() != null ? interviewDTO.getSize() : 20;
            int totalElements = interviewList.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            List<InterviewDTO> paginatedList = fromIndex < totalElements ? interviewList.subList(fromIndex, toIndex) : new ArrayList<>();

            apiLogInfo.setApiRequest("Filters: " + startDate + " to " + endDate);
            apiLogInfo.setApiResponse("Retrieved " + paginatedList.size() + " interviews out of " + totalElements);
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(paginatedList);
            response.setTotalElements(totalElements);
        } catch (Exception e) {
            apiLogInfo.setApiError("Error retrieving interviews: " + e.getMessage());
            apiLogInfo.setLogLevel("ERROR");
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
            response.setErrorStackTrace(e.getStackTrace().toString());
        }
        return response;
    }

    public ServiceResponse getInterviewById(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Interview interview = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (interview == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }
            InterviewDTO dto = mapEntityToDTO(interview);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(dto);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    @Transactional
    public ServiceResponse deleteInterview(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        try {
            Interview existing = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (existing == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }
            interviewRepository.delete(existing);
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Interview deleted successfully");
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    public ServiceResponse getDropdownData() {
        ServiceResponse response = new ServiceResponse();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("clients", interviewRepository.findAllClients());
            data.put("departments", buildIdNameList(interviewRepository.findAllDepartments()));
            data.put("employees", buildIdNameList(interviewRepository.findAllEmployees()));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(data);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    

    public ServiceResponse getEmployeesByDepartmentId(Long departmentId) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Map<String, Object>> employees = buildIdNameList(interviewRepository.findEmployeesByDepartmentId(departmentId));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(employees);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }
    public ServiceResponse getProjectsByClientName(String clientName) {
        ServiceResponse response = new ServiceResponse();
        try {
            List<Map<String, Object>> projects = buildProjectsWithClientList(interviewRepository.findProjectsByClientName(clientName));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(projects);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    private List<Map<String, Object>> buildIdNameList(List<Object[]> rawList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("name", row[1]);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildProjectsWithClientList(List<Object[]> rawList) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("name", row[1]);
            item.put("clientName", row[2]);
            result.add(item);
        }
        return result;
    }

    private InterviewDTO mapRowToDTO(Object[] row) {
        InterviewDTO dto = new InterviewDTO();
        try {
            dto.setId(row[0] != null ? ((Number) row[0]).longValue() : null);
            dto.setTitle(getString(row[1]));
            dto.setDate(getString(row[2]));
            dto.setTime(getString(row[3]));
            dto.setClient(getString(row[4]));
            dto.setRole(getString(row[5]));
            dto.setProject(getString(row[6]));
            dto.setDepartmentId(row[7] != null ? ((Number) row[7]).longValue() : null);
            dto.setEmployeeId(row[8] != null ? ((Number) row[8]).longValue() : null);
            dto.setMode(getString(row[9]));
            dto.setInterviewStatus(getString(row[10]));
            dto.setInterviewRemarks(getString(row[11]));
            dto.setSelectionStatus(getString(row[12]));
            dto.setSelectionRemarks(getString(row[13]));
            dto.setOnboardingStatus(getString(row[14]));
            dto.setOnboardingRemarks(getString(row[15]));
            dto.setJd(getString(row[16]));
            dto.setScheduledById(row[17] != null ? ((Number) row[17]).longValue() : null);
            dto.setInterviewerName(getString(row[18]));
            dto.setAdditionalNotes(getString(row[19]));
            dto.setResumeFileName(getString(row[20]));
            dto.setResumeFilePath(getString(row[21]));
            dto.setCreatedBy(row[22] != null ? ((Number) row[22]).longValue() : null);
            dto.setCreatedOn(getString(row[23]));
            dto.setUpdatedBy(row[24] != null ? ((Number) row[24]).longValue() : null);
            dto.setUpdatedOn(getString(row[25]));
            dto.setDepartmentName(getString(row[26]));
            dto.setEmployeeName(getString(row[27]));
            dto.setScheduledByName(getString(row[28]));
        } catch (Exception e) {
        }
        return dto;
    }

    private InterviewDTO mapEntityToDTO(Interview interview) {
        InterviewDTO dto = new InterviewDTO();
        dto.setId(interview.getId());
        dto.setTitle(interview.getTitle());
        dto.setDate(interview.getDate());
        dto.setTime(interview.getTime());
        dto.setClient(interview.getClient());
        dto.setRole(interview.getRole());
        dto.setProject(interview.getProject());
        dto.setDepartmentId(interview.getDepartmentId());
        dto.setEmployeeId(interview.getEmployeeId());
        dto.setMode(interview.getMode());
        dto.setInterviewStatus(interview.getInterviewStatus());
        dto.setSelectionStatus(interview.getSelectionStatus());
        dto.setOnboardingStatus(interview.getOnboardingStatus());
        dto.setInterviewRemarks(interview.getInterviewRemarks());
        dto.setSelectionRemarks(interview.getSelectionRemarks());
        dto.setOnboardingRemarks(interview.getOnboardingRemarks());
        dto.setJd(interview.getJd());
        dto.setInterviewerName(interview.getInterviewerName());
        dto.setScheduledById(interview.getScheduledById());
        dto.setAdditionalNotes(interview.getAdditionalNotes());
        dto.setResumeFileName(interview.getResumeFileName());
        dto.setResumeFilePath(interview.getResumeFilePath());
        if (interview.getCommonProperty() != null) {
            dto.setCreatedBy(interview.getCommonProperty().getCreatedBy());
            dto.setUpdatedBy(interview.getCommonProperty().getUpdatedBy());
            if (interview.getCommonProperty().getUpdatedOn() != null) {
                dto.setUpdatedOn(interview.getCommonProperty().getUpdatedOn().toString());
            }
        }
        return dto;
    }

    private String getString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private String getFieldValue(InterviewDTO dto, String field) {
        switch (field) {
            case "title": return dto.getTitle() != null ? dto.getTitle() : "";
            case "date": return dto.getDate() != null ? dto.getDate() : "";
            case "client": return dto.getClient() != null ? dto.getClient() : "";
            case "role": return dto.getRole() != null ? dto.getRole() : "";
            case "project": return dto.getProject() != null ? dto.getProject() : "";
            case "departmentName": return dto.getDepartmentName() != null ? dto.getDepartmentName() : "";
            case "employeeName": return dto.getEmployeeName() != null ? dto.getEmployeeName() : "";
            case "mode": return dto.getMode() != null ? dto.getMode() : "";
            case "interviewStatus": return dto.getInterviewStatus() != null ? dto.getInterviewStatus() : "";
            case "selectionStatus": return dto.getSelectionStatus() != null ? dto.getSelectionStatus() : "";
            case "onboardingStatus": return dto.getOnboardingStatus() != null ? dto.getOnboardingStatus() : "";
            case "scheduledByName": return dto.getScheduledByName() != null ? dto.getScheduledByName() : "";
            case "interviewerName": return dto.getInterviewerName() != null ? dto.getInterviewerName() : "";
            default: return "";
        }
    }

	
}
