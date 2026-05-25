package com.apmosys.employeeportal.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apmosys.employeeportal.dto.InterviewDTO;
import com.apmosys.employeeportal.dto.InterviewVisibilityScope;
import com.apmosys.employeeportal.dto.LogDTO;
import com.apmosys.employeeportal.model.CommonProperties;
import com.apmosys.employeeportal.model.Interview;
import com.apmosys.employeeportal.repository.InterviewRepository;
import com.apmosys.employeeportal.utility.InterviewConstants;
import com.apmosys.employeeportal.utility.ServiceResponse;

@Service
public class InterviewService {

    @Autowired
    InterviewRepository interviewRepository;

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    private LogService logService;

    @Autowired
    private InterviewAccessService interviewAccessService;

    @Transactional
    public ServiceResponse scheduleInterview(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setFeatureName(InterviewConstants.FEATURE_NAME);
        apiLogInfo.setSubFeatureName(InterviewConstants.SUB_SCHEDULE);
        apiLogInfo.setApiUrl("/api/scheduleInterview");
        apiLogInfo.setLogLevel("INFO");

        try {
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            interviewAccessService.requireSubFeature(currentEmpId, InterviewConstants.SUB_SCHEDULE);

            String statusDateError = validateStatusChangeDatesForNew(interviewDTO);
            if (statusDateError != null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse(statusDateError);
                return response;
            }

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
            interview.setInterviewStatusChangeDate(interviewDTO.getInterviewStatusChangeDate());
            interview.setSelectionStatusChangeDate(interviewDTO.getSelectionStatusChangeDate());
            interview.setOnboardingStatusChangeDate(interviewDTO.getOnboardingStatusChangeDate());
            interview.setInterviewRemarks(interviewDTO.getInterviewRemarks());
            interview.setSelectionRemarks(interviewDTO.getSelectionRemarks());
            interview.setOnboardingRemarks(interviewDTO.getOnboardingRemarks());
            interview.setJd(interviewDTO.getJd());
            interview.setInterviewerName(interviewDTO.getInterviewerName());
            interview.setScheduledById(interviewDTO.getScheduledById());
            interview.setAdditionalNotes(interviewDTO.getAdditionalNotes());
            interview.setResumeFileName(interviewDTO.getResumeFileName());
            interview.setResumeFilePath(interviewDTO.getResumeFilePath());

            CommonProperties commonProperty = new CommonProperties();
            commonProperty.setCreatedBy(currentEmpId);
            commonProperty.setCreatedOn(new Timestamp(System.currentTimeMillis()));
            commonProperty.setUpdatedBy(currentEmpId);
            commonProperty.setUpdatedOn(LocalDateTime.now());
            interview.setCommonProperty(commonProperty);

            Interview saved = interviewRepository.save(interview);

            apiLogInfo.setApiRequest("Interview: " + interviewDTO.getTitle());
            apiLogInfo.setApiResponse("Interview scheduled successfully. ID: " + saved.getId());
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Interview scheduled successfully");
            response.setServiceResponse1(saved.getId());
        } catch (SecurityException ex) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ex.getMessage());
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
        apiLogInfo.setFeatureName(InterviewConstants.FEATURE_NAME);
        apiLogInfo.setSubFeatureName(InterviewConstants.SUB_EDIT);
        apiLogInfo.setApiUrl("/api/updateInterview");
        apiLogInfo.setLogLevel("INFO");

        try {
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            interviewAccessService.requireSubFeature(currentEmpId, InterviewConstants.SUB_EDIT);

            Interview existing = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (existing == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }
            if (!canAccessInterviewEntity(currentEmpId, existing)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied for this interview");
                return response;
            }

            String statusDateError = validateStatusChangeDatesForUpdate(
                    existing.getSelectionStatus(), interviewDTO.getSelectionStatus(), interviewDTO.getSelectionStatusChangeDate(),
                    existing.getOnboardingStatus(), interviewDTO.getOnboardingStatus(), interviewDTO.getOnboardingStatusChangeDate());
            if (statusDateError != null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse(statusDateError);
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
            existing.setInterviewStatusChangeDate(interviewDTO.getInterviewStatusChangeDate());
            existing.setSelectionStatusChangeDate(interviewDTO.getSelectionStatusChangeDate());
            existing.setOnboardingStatusChangeDate(interviewDTO.getOnboardingStatusChangeDate());
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

            CommonProperties existingCommon = existing.getCommonProperty();
            if (existingCommon == null) {
                existingCommon = new CommonProperties();
            }
            existingCommon.setUpdatedBy(currentEmpId);
            existingCommon.setUpdatedOn(LocalDateTime.now());
            existing.setCommonProperty(existingCommon);

            Interview saved = interviewRepository.save(existing);

            apiLogInfo.setApiRequest("Interview ID: " + interviewDTO.getId());
            apiLogInfo.setApiResponse("Interview updated successfully. ID: " + saved.getId());
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Interview updated successfully");
        } catch (SecurityException ex) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ex.getMessage());
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

    @Transactional
    public ServiceResponse updateInterviewStatus(InterviewDTO interviewDTO) {
        ServiceResponse response = new ServiceResponse();
        LogDTO apiLogInfo = new LogDTO();
        apiLogInfo.setFeatureName(InterviewConstants.FEATURE_NAME);
        apiLogInfo.setSubFeatureName(InterviewConstants.SUB_EDIT);
        apiLogInfo.setApiUrl("/api/updateInterviewStatus");
        apiLogInfo.setLogLevel("INFO");

        try {
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            interviewAccessService.requireSubFeature(currentEmpId, InterviewConstants.SUB_EDIT);

            Interview existing = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (existing == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }
            if (!canAccessInterviewEntity(currentEmpId, existing)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied for this interview");
                return response;
            }

            String newSelectionStatus = interviewDTO.getSelectionStatus() != null
                    ? interviewDTO.getSelectionStatus() : existing.getSelectionStatus();
            String newOnboardingStatus = interviewDTO.getOnboardingStatus() != null
                    ? interviewDTO.getOnboardingStatus() : existing.getOnboardingStatus();
            String statusDateError = validateStatusChangeDatesForUpdate(
                    existing.getSelectionStatus(), newSelectionStatus, interviewDTO.getSelectionStatusChangeDate(),
                    existing.getOnboardingStatus(), newOnboardingStatus, interviewDTO.getOnboardingStatusChangeDate());
            if (statusDateError != null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse(statusDateError);
                return response;
            }

            if (interviewDTO.getInterviewStatus() != null) {
                existing.setInterviewStatus(interviewDTO.getInterviewStatus());
            }
            if (interviewDTO.getSelectionStatus() != null) {
                existing.setSelectionStatus(interviewDTO.getSelectionStatus());
            }
            if (interviewDTO.getOnboardingStatus() != null) {
                existing.setOnboardingStatus(interviewDTO.getOnboardingStatus());
            }
            if (interviewDTO.getInterviewRemarks() != null) {
                existing.setInterviewRemarks(interviewDTO.getInterviewRemarks());
            }
            if (interviewDTO.getSelectionRemarks() != null) {
                existing.setSelectionRemarks(interviewDTO.getSelectionRemarks());
            }
            if (interviewDTO.getOnboardingRemarks() != null) {
                existing.setOnboardingRemarks(interviewDTO.getOnboardingRemarks());
            }
            if (interviewDTO.getInterviewStatusChangeDate() != null) {
                existing.setInterviewStatusChangeDate(interviewDTO.getInterviewStatusChangeDate());
            }
            if (interviewDTO.getSelectionStatusChangeDate() != null) {
                existing.setSelectionStatusChangeDate(interviewDTO.getSelectionStatusChangeDate());
            }
            if (interviewDTO.getOnboardingStatusChangeDate() != null) {
                existing.setOnboardingStatusChangeDate(interviewDTO.getOnboardingStatusChangeDate());
            }

            CommonProperties existingCommon = existing.getCommonProperty();
            if (existingCommon == null) {
                existingCommon = new CommonProperties();
            }
            existingCommon.setUpdatedBy(currentEmpId);
            existingCommon.setUpdatedOn(LocalDateTime.now());
            existing.setCommonProperty(existingCommon);

            Interview saved = interviewRepository.save(existing);

            apiLogInfo.setApiRequest("Interview ID: " + interviewDTO.getId());
            apiLogInfo.setApiResponse("Interview status updated successfully. ID: " + saved.getId());
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse("Interview status updated successfully");
        } catch (SecurityException ex) {
            response.setServiceStatus(ServiceResponse.STATUS_FAIL);
            response.setServiceResponse(ex.getMessage());
        } catch (Exception e) {
            apiLogInfo.setApiRequest("Interview ID: " + interviewDTO.getId());
            apiLogInfo.setApiError("Error updating interview status: " + e.getMessage());
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
        apiLogInfo.setFeatureName(InterviewConstants.FEATURE_NAME);
        apiLogInfo.setSubFeatureName(InterviewConstants.SUB_VIEW);
        apiLogInfo.setApiUrl("/api/getAllInterviews");
        apiLogInfo.setLogLevel("INFO");

        try {
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            if (!interviewAccessService.canViewList(currentEmpId)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied: View Interview");
                return response;
            }
            InterviewVisibilityScope visibilityScope = interviewAccessService.resolveVisibilityScope(currentEmpId);

            String startDate = interviewDTO.getStartDate();
            String endDate = interviewDTO.getEndDate();

            List<String> clients = interviewDTO.getClients();
            List<Long> departmentIds = interviewDTO.getDepartmentIds();
            List<Long> employeeIds = interviewDTO.getEmployeeIds();

            Integer page = interviewDTO.getPage() != null ? interviewDTO.getPage() : 0;
            Integer size = interviewDTO.getSize() != null ? interviewDTO.getSize() : 20;

            List<Object[]> results = interviewRepository.findAllInterviewsWithColumnFilters(
                    startDate, endDate,
                    clients, departmentIds, employeeIds,
                    interviewDTO.getTitleFilter(), interviewDTO.getDateFilter(),
                    interviewDTO.getClientFilter(), interviewDTO.getRoleFilter(),
                    interviewDTO.getProjectFilter(), interviewDTO.getDepartmentNameFilter(),
                    interviewDTO.getEmployeeNameFilter(), interviewDTO.getModeFilter(),
                    interviewDTO.getInterviewStatusFilter(), interviewDTO.getSelectionStatusFilter(),
                    interviewDTO.getOnboardingStatusFilter(), interviewDTO.getJdFilter(),
                    interviewDTO.getScheduledByNameFilter(), interviewDTO.getInterviewerNameFilter(),
                    visibilityScope,
                    interviewDTO.getSortColumn(), interviewDTO.getSortDirection(),
                    page, size);

            long totalElements = interviewRepository.countAllInterviewsWithColumnFilters(
                    startDate, endDate,
                    clients, departmentIds, employeeIds,
                    interviewDTO.getTitleFilter(), interviewDTO.getDateFilter(),
                    interviewDTO.getClientFilter(), interviewDTO.getRoleFilter(),
                    interviewDTO.getProjectFilter(), interviewDTO.getDepartmentNameFilter(),
                    interviewDTO.getEmployeeNameFilter(), interviewDTO.getModeFilter(),
                    interviewDTO.getInterviewStatusFilter(), interviewDTO.getSelectionStatusFilter(),
                    interviewDTO.getOnboardingStatusFilter(), interviewDTO.getJdFilter(),
                    interviewDTO.getScheduledByNameFilter(), interviewDTO.getInterviewerNameFilter(),
                    visibilityScope);

            List<InterviewDTO> interviewList = new ArrayList<>();
            for (Object[] row : results) {
                InterviewDTO dto = mapRowToDTO(row);
                interviewList.add(dto);
            }

            apiLogInfo.setApiRequest("Filters: " + startDate + " to " + endDate);
            apiLogInfo.setApiResponse("Retrieved " + interviewList.size() + " interviews out of " + totalElements);
            logService.logMyInfo(httpRequest, apiLogInfo);

            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(interviewList);
            response.setTotalElements((int) totalElements);
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
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            if (!interviewAccessService.canViewList(currentEmpId)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied: View Interview");
                return response;
            }
            Interview interview = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (interview == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }
            if (!canAccessInterviewEntity(currentEmpId, interview)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied for this interview");
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
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            interviewAccessService.requireSubFeature(currentEmpId, InterviewConstants.SUB_DELETE);

            Interview existing = interviewRepository.findById(interviewDTO.getId()).orElse(null);
            if (existing == null) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Interview not found");
                return response;
            }
            if (!canAccessInterviewEntity(currentEmpId, existing)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied for this interview");
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
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            if (!interviewAccessService.canViewList(currentEmpId)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied: View Interview");
                return response;
            }
            InterviewVisibilityScope scope = interviewAccessService.resolveVisibilityScope(currentEmpId);

            Map<String, Object> data = new HashMap<>();
            data.put("clients", interviewRepository.findAllClients());
            data.put("departments", buildScopedDepartments(scope));
            data.put("employees", buildScopedEmployees(scope));
            response.setServiceStatus(ServiceResponse.STATUS_SUCCESS);
            response.setServiceResponse(data);
        } catch (Exception e) {
            response.setServiceStatus(ServiceResponse.SOMETHING_WENT_WRONG);
            response.setServiceError(e.getMessage());
        }
        return response;
    }

    public boolean canDownloadResume(String fileName) {
        Long currentEmpId = interviewAccessService.getCurrentEmpId();
        if (!interviewAccessService.canViewList(currentEmpId)) {
            return false;
        }
        return interviewRepository.findFirstByResumeFilePathContaining(fileName)
                .map(interview -> canAccessInterviewEntity(currentEmpId, interview))
                .orElse(false);
    }

    

    public ServiceResponse getEmployeesByDepartmentId(Long departmentId) {
        ServiceResponse response = new ServiceResponse();
        try {
            Long currentEmpId = interviewAccessService.getCurrentEmpId();
            if (!interviewAccessService.canViewList(currentEmpId)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied: View Interview");
                return response;
            }
            InterviewVisibilityScope scope = interviewAccessService.resolveVisibilityScope(currentEmpId);
            if (!scope.isViewAll() && !scope.getVisibleDepartmentIds().contains(departmentId)) {
                response.setServiceStatus(ServiceResponse.STATUS_FAIL);
                response.setServiceResponse("Access denied for this department");
                return response;
            }
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
            dto.setInterviewStatusChangeDate(getString(row[16]));
            dto.setSelectionStatusChangeDate(getString(row[17]));
            dto.setOnboardingStatusChangeDate(getString(row[18]));
            dto.setJd(getString(row[19]));
            dto.setScheduledById(row[20] != null ? ((Number) row[20]).longValue() : null);
            dto.setInterviewerName(getString(row[21]));
            dto.setAdditionalNotes(getString(row[22]));
            dto.setResumeFileName(getString(row[23]));
            dto.setResumeFilePath(getString(row[24]));
            dto.setCreatedBy(row[25] != null ? ((Number) row[25]).longValue() : null);
            dto.setCreatedOn(getString(row[26]));
            dto.setUpdatedBy(row[27] != null ? ((Number) row[27]).longValue() : null);
            dto.setUpdatedOn(getString(row[28]));
            dto.setDepartmentName(getString(row[29]));
            dto.setEmployeeName(getString(row[30]));
            dto.setScheduledByName(getString(row[31]));
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
        dto.setInterviewStatusChangeDate(interview.getInterviewStatusChangeDate());
        dto.setSelectionStatusChangeDate(interview.getSelectionStatusChangeDate());
        dto.setOnboardingStatusChangeDate(interview.getOnboardingStatusChangeDate());
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

    private boolean canAccessInterviewEntity(Long currentEmpId, Interview interview) {
        Long createdBy = interview.getCommonProperty() != null ? interview.getCommonProperty().getCreatedBy() : null;
        return interviewAccessService.canAccessInterview(
                currentEmpId, interview.getEmployeeId(), createdBy, interview.getDepartmentId());
    }

    private List<Map<String, Object>> buildScopedDepartments(InterviewVisibilityScope scope) {
        List<Object[]> all = interviewRepository.findAllDepartments();
        if (scope.isViewAll()) {
            return buildIdNameList(all);
        }
        if (scope.getVisibleDepartmentIds().isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : all) {
            Long deptId = row[0] != null ? ((Number) row[0]).longValue() : null;
            if (deptId != null && scope.getVisibleDepartmentIds().contains(deptId)) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row[0]);
                item.put("name", row[1]);
                result.add(item);
            }
        }
        return result;
    }

    private List<Map<String, Object>> buildScopedEmployees(InterviewVisibilityScope scope) {
        if (scope.isViewAll()) {
            return buildIdNameList(interviewRepository.findAllEmployeesForInterviewGlobalFilter());
        }
        if (scope.getVisibleEmployeeIds().isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> all = buildIdNameList(interviewRepository.findAllEmployeesForInterviewGlobalFilter());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : all) {
            Long id = item.get("id") != null ? ((Number) item.get("id")).longValue() : null;
            if (id != null && scope.getVisibleEmployeeIds().contains(id)) {
                result.add(item);
            }
        }
        return result;
    }

    private static final List<String> INITIAL_STATUS_VALUES = Arrays.asList("Not Applicable", "Pending");

    private boolean isInitialStatus(String status) {
        return status != null && INITIAL_STATUS_VALUES.contains(status);
    }

    private boolean requiresStatusChangeDate(String originalStatus, String newStatus) {
        if (newStatus == null || Objects.equals(originalStatus, newStatus)) {
            return false;
        }
        return isInitialStatus(originalStatus) && !isInitialStatus(newStatus);
    }

    private boolean requiresStatusChangeDateForNew(String status) {
        return status != null && !isInitialStatus(status);
    }

    private boolean hasStatusChangeDate(String date) {
        return date != null && !date.trim().isEmpty();
    }

    private String validateStatusChangeDatesForNew(InterviewDTO dto) {
        if (requiresStatusChangeDateForNew(dto.getSelectionStatus())
                && !hasStatusChangeDate(dto.getSelectionStatusChangeDate())) {
            return "Selection status change date is required when status is not Pending";
        }
        if (requiresStatusChangeDateForNew(dto.getOnboardingStatus())
                && !hasStatusChangeDate(dto.getOnboardingStatusChangeDate())) {
            return "Onboarding status change date is required when status is not Not Applicable or Pending";
        }
        return null;
    }

    private String validateStatusChangeDatesForUpdate(
            String originalSelection, String newSelection, String selectionChangeDate,
            String originalOnboarding, String newOnboarding, String onboardingChangeDate) {
        if (requiresStatusChangeDate(originalSelection, newSelection)
                && !hasStatusChangeDate(selectionChangeDate)) {
            return "Selection status change date is required when moving from Pending";
        }
        if (requiresStatusChangeDate(originalOnboarding, newOnboarding)
                && !hasStatusChangeDate(onboardingChangeDate)) {
            return "Onboarding status change date is required when moving from Not Applicable or Pending";
        }
        return null;
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
