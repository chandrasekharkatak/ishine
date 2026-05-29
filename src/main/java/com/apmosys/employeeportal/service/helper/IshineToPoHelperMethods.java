package com.apmosys.employeeportal.service.helper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.DateRange;
import com.apmosys.employeeportal.dto.EmpMappingDTO;
import com.apmosys.employeeportal.dto.EmpRoleKey;
import com.apmosys.employeeportal.dto.IshineToPoEmpDetailsSharingDTO;
import com.apmosys.employeeportal.dto.IshineToPoEmployeeDTO;
import com.apmosys.employeeportal.dto.IshineToPoRequestDTO;
import com.apmosys.employeeportal.dto.ShadowEntryDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.utility.ServiceResponse;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.EmployeeTeamMapRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.ProjectPoDetailsRepository;


@Component
public class IshineToPoHelperMethods {
    
    @Autowired
    ProjectPoDetailsRepository projectPoDetailsRepository;

    @Autowired
    EmployeeTeamMapRepository employeeTeamMapRepository;

    @Autowired
    EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    EmployeeLeaveRepository employeeLeaveRepository;


    public ServiceResponse validateRequest(IshineToPoRequestDTO req) {
    if (req == null)
        return buildFailResponse("Request recieved from PO is null!!");
    if (req.getPoId() == null)
        return buildFailResponse("PO Id in request is null!!");
    if (req.getProjectId() == null)
        return buildFailResponse("Project Id in request is null!!");
    if (req.getStartDateOfBilling() == null)
        return buildFailResponse("Start Date in request is null!!");
    if (req.getEndDateOfBilling() == null)
        return buildFailResponse("End Date in request is null!!");
    if (req.getStartDateOfBilling().after(req.getEndDateOfBilling()))
        return buildFailResponse("Start Date cannot be after End Date!!");
    return null; // null means valid
}

public ServiceResponse validateProject(IshineToPoEmpDetailsSharingDTO dto) {
    if (dto == null || dto.getIshineProjectId() == null)
        return buildFailResponse("Project Details for the PO not found!!");
    return null;
}

public ServiceResponse validateProjectState(Project project) {
    if (project == null)
        return buildFailResponse("Project Details for the PO not found!!");
    if (project.getIsDraftProject() == null)
        return buildFailResponse("Resource onboarding has not started!!");
    return null;
}



public void fetchEmployeesWithTimesheets(
        List<Long> empIds,
        LocalDate startDate, LocalDate endDate,
        Long poId, Integer projectId,
        Map<EmpRoleKey, List<LocalDate>> datesTimesheetFilled, List<IshineToPoEmployeeDTO> employees) {

    // 1. Fetch all employees in one call
    List<IshineToPoEmployeeDTO> emps = projectPoDetailsRepository.findEmployeesWithTimesheetCount(empIds, startDate, endDate, poId);
    if (emps != null) {
        employees.addAll(emps);
    }

    // 2. Fetch all working dates for all employees in one call
    List<Object[]> results = employeeTimesheetsNewRepository.findTimesheetDatesByEmpIdsAndDateBetween(empIds, startDate, endDate, projectId, poId);

    // 3. Group dates by Employee ID + Role ID into the map
    if (results != null) {
        for (Object[] row : results) {
            Long empId = ((Number) row[0]).longValue();
            LocalDate date = ((java.sql.Date) row[1]).toLocalDate();
            Long roleId = ((Number) row[2]).longValue();
            
            EmpRoleKey key = new EmpRoleKey(empId, roleId);
            datesTimesheetFilled.computeIfAbsent(key, k -> new ArrayList<>()).add(date);
        }
    }

    // 4. Get min and max working date.

}

public void getTimesheetMinMaxDateMap(List<Long> empIds, LocalDate startDate, LocalDate endDate, Integer projectId, Long poId, 
                                     Map<EmpRoleKey, LocalDate> minDateMap, Map<EmpRoleKey, LocalDate> maxDateMap) {
    List<Object[]> results = employeeTimesheetsNewRepository.findMaxAndMinDateOfTimesheet(empIds, startDate, endDate, projectId, poId);
    if (results != null) {
        for (Object[] row : results) {
            Long empId = ((Number) row[0]).longValue();
            Long roleId = ((Number) row[1]).longValue();
            LocalDate maxDate = row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : null;
            LocalDate minDate = row[3] != null ? ((java.sql.Date) row[3]).toLocalDate() : null;

            EmpRoleKey key = new EmpRoleKey(empId, roleId);
            if (minDate != null) minDateMap.put(key, minDate);
            if (maxDate != null) maxDateMap.put(key, maxDate);
        }
    }
}

public List<IshineToPoEmployeeDTO> mergeShadowTimesheets(
        List<IshineToPoEmployeeDTO> employees,
        List<Long> allMappedEmpIds,
        Map<EmpRoleKey, List<LocalDate>> datesTimesheetFilled,
        Long poId, LocalDate startDate, LocalDate endDate,
        Map<EmpRoleKey, List<DateRange>> empRoleWindowMap) {

    List<Object[]> rawShadowRows = employeeTimesheetsNewRepository
            .findTimesheetShadowDetailsByPoAndEmpIds(poId, allMappedEmpIds, startDate, endDate);

    Map<Long, List<ShadowEntryDTO>> shadowMap = buildShadowMap(rawShadowRows);

    Map<EmpRoleKey, IshineToPoEmployeeDTO> empMap = new HashMap<>();
    for (IshineToPoEmployeeDTO emp : employees) {
        empMap.put(new EmpRoleKey(emp.getIshineEmpId(), emp.getEtmRoleId()), emp);
    }

    for (Map.Entry<Long, List<ShadowEntryDTO>> shadowEntry : shadowMap.entrySet()) {
        // shadowEntry.getKey() is the shadow's empId, but shadow itself might have multiple roles.
        // We only use the shadowEmp for minimal DTO copying, which might be slightly inaccurate if
        // the shadow has multiple roles, but it's acceptable for minimal DTO creation.
        IshineToPoEmployeeDTO shadowEmp = employees.stream()
                .filter(e -> e.getIshineEmpId().equals(shadowEntry.getKey()))
                .findFirst().orElse(null);

        Map<Long, List<ShadowEntryDTO>> groupedByMainEmp = shadowEntry.getValue().stream()
                .collect(Collectors.groupingBy(ShadowEntryDTO::getShadowForEmpId));

        for (Map.Entry<Long, List<ShadowEntryDTO>> mainEntry : groupedByMainEmp.entrySet()) {
            mergeShadowForMainEmployee(
                    mainEntry.getKey(), mainEntry.getValue(),
                    shadowEmp, empMap, datesTimesheetFilled, empRoleWindowMap);
        }
    }

    return new ArrayList<>(empMap.values());
}



private void mergeShadowForMainEmployee(
        Long mainEmpId,
        List<ShadowEntryDTO> shadowEntries,
        IshineToPoEmployeeDTO shadowEmp,
        Map<EmpRoleKey, IshineToPoEmployeeDTO> empMap,
        Map<EmpRoleKey, List<LocalDate>> datesTimesheetFilled,
        Map<EmpRoleKey, List<DateRange>> empRoleWindowMap) {

    for (ShadowEntryDTO shadowEntry : shadowEntries) {
        LocalDate date = shadowEntry.getDate();
        if (date == null) continue;

        EmpRoleKey matchingKey = findKeyForDate(mainEmpId, date, empRoleWindowMap);
        if (matchingKey == null) continue; // Shadow date doesn't fall in any active role window for main emp

        List<LocalDate> alreadyCredited = datesTimesheetFilled.get(matchingKey);
        if (alreadyCredited != null && alreadyCredited.contains(date)) {
            continue; // Main emp already has a timesheet credited for this date
        }

        IshineToPoEmployeeDTO mainEmp = empMap.get(matchingKey);
        if (mainEmp == null) {
            mainEmp = buildMinimalEmpDTO(mainEmpId, shadowEmp);
            if (mainEmp == null) continue;
            // Since this is a newly built minimal DTO, it doesn't have an etmRoleId yet.
            // Assign the one we found.
            mainEmp.setEtmRoleId(matchingKey.getRoleId());
            empMap.put(matchingKey, mainEmp);
            datesTimesheetFilled.put(matchingKey, new ArrayList<>());
        }

        List<LocalDate> existingShadowDates = mainEmp.getShadowTimeSheetDate();
        if (existingShadowDates == null) {
            existingShadowDates = new ArrayList<>();
            mainEmp.setShadowTimeSheetDate(existingShadowDates);
        }
        
        if (!existingShadowDates.contains(date)) {
            existingShadowDates.add(date);
        }

        if (mainEmp.getStartDate() == null || date.isBefore(mainEmp.getStartDate())) {
            mainEmp.setStartDate(date);
        }
        if (mainEmp.getEndDate() == null || date.isAfter(mainEmp.getEndDate())) {
            mainEmp.setEndDate(date);
        }

        mainEmp.setMsg("Shadow's Timesheet Count Added with the Resource!!");
        datesTimesheetFilled.computeIfAbsent(matchingKey, k -> new ArrayList<>()).add(date);
    }
}

private EmpRoleKey findKeyForDate(Long empId, LocalDate date,
        Map<EmpRoleKey, List<DateRange>> empRoleWindowMap) {
    for (Map.Entry<EmpRoleKey, List<DateRange>> entry : empRoleWindowMap.entrySet()) {
        if (!entry.getKey().getEmpId().equals(empId)) continue;
        for (DateRange range : entry.getValue()) {
            if (range.contains(date)) {
                return entry.getKey();
            }
        }
    }
    return null;
}

public ServiceResponse buildFailResponse(String message) {
    ServiceResponse response = new ServiceResponse();
    response.setServiceStatus(ServiceResponse.STATUS_FAIL);
    response.setServiceResponse(message);
    return response;
}


private Map<Long, List<ShadowEntryDTO>> buildShadowMap(List<Object[]> rawRows) {
    // Key = shadowEmpId (who filled as shadow)
    // Value = list of {shadowFor, date, timesheetId}
    Map<Long, List<ShadowEntryDTO>> shadowMap = new HashMap<>();

    for (Object[] row : rawRows) {
        Long empId       = ((Number) row[0]).longValue();
        Long shadowForId = row[1] != null ? ((Number) row[1]).longValue() : null;
        LocalDate date   = row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : null;
        Long timesheetId = ((Number) row[3]).longValue();

        // Only index rows where this employee is acting as a shadow for someone else
        if (shadowForId != null && !shadowForId.equals(empId)) {
            shadowMap
                .computeIfAbsent(empId, k -> new ArrayList<>())
                .add(new ShadowEntryDTO(empId, shadowForId, date, timesheetId));
        }
    }
    return shadowMap;
}


private IshineToPoEmployeeDTO buildMinimalEmpDTO(Long mainEmpId, IshineToPoEmployeeDTO shadowEmp) {
    Employee mainEmpData = employeeRepository.findByEmpId(mainEmpId);
    if (mainEmpData == null) return null;

    IshineToPoEmployeeDTO mainEmp = new IshineToPoEmployeeDTO();
    mainEmp.setEmpId("true".equalsIgnoreCase(mainEmpData.getIsApmosysProduct())
            ? "AP-" + mainEmpData.getEmployeementId()
            : "A-"  + mainEmpData.getEmployeementId());
    mainEmp.setEmpName(mainEmpData.getName());
    mainEmp.setIsApmosysProduct(mainEmpData.getIsApmosysProduct());
    mainEmp.setIshineEmpId(mainEmpData.getEmpId());
    mainEmp.setNoOfWorkingDays(0L);
    mainEmp.setBillableDays(0L);
    mainEmp.setShadowTimeSheetDate(new ArrayList<>()); 
    if (shadowEmp != null) {
        mainEmp.setRoleName(shadowEmp.getRoleName());
        mainEmp.setExp(shadowEmp.getExp());
        mainEmp.setDepartmentName(shadowEmp.getDepartmentName());
        mainEmp.setRoleId(shadowEmp.getRoleId());
        mainEmp.setClientSideId(shadowEmp.getClientSideId());
        mainEmp.setPoId(shadowEmp.getPoId());
//        mainEmp.setStartDate(shadowEmp.getStartDate());
//        mainEmp.setEndDate(shadowEmp.getEndDate());
    }
    return mainEmp;
}

public void getWeekOffCount(LocalDate startDate,LocalDate endDate,List<Long> empIds, Integer projectId,Map<Long,List<LocalDate>> weekOffMap){

         List<Object[]> results = employeeLeaveRepository
			            .getDatesBasedOnDayType(startDate, endDate,empIds, projectId,4);
    
        mappingDates(results,weekOffMap);	   

}

public void getWorkingOnANonWorkingMapCount(LocalDate startDate,LocalDate endDate,List<Long> empIds, Integer projectId,Map<Long,List<LocalDate>> workingOnANonWorkingMap){

         List<Object[]> results = employeeLeaveRepository
			            .getDatesBasedOnDayType(startDate, endDate,empIds, projectId,3);
    
        mappingDates(results,workingOnANonWorkingMap);

}

public void getClientHolidayMapCount(LocalDate startDate,LocalDate endDate,List<Long> empIds, Integer projectId,Map<Long,List<LocalDate>> clientHolidayMap){

         List<Object[]> results = employeeLeaveRepository
			            .getDatesBasedOnDayType(startDate, endDate,empIds, projectId,7);
        mappingDates(results,clientHolidayMap);

}

public void getCompOffMapCount(LocalDate startDate,LocalDate endDate,List<Long> empIds, Integer projectId,Map<Long,List<LocalDate>> compOffMap){

         List<Object[]> results = employeeLeaveRepository
			            .getDatesBasedOnDayType(startDate, endDate,empIds, projectId,9);
        mappingDates(results,compOffMap);

}




public void mappingDates(List<Object[]> results,Map<Long,List<LocalDate>>passedMap){

     for (Object[] row : results) {
			        Long empId = ((Number) row[0]).longValue();
					LocalDate dates = ((java.sql.Date) row[1]).toLocalDate();
					passedMap.computeIfAbsent(empId, e -> new ArrayList<>() ).add(dates);

			    }
}



public Map<Long, LocalDate> getMaxEndDatePerEmployee(
        List<Long> empIds, Long poId) {

    Map<Long, LocalDate> result = new HashMap<>();

    if (empIds == null || empIds.isEmpty() || poId == null) {
        return result;
    }

    List<Object[]> rows = employeeTeamMapRepository
            .findEmpIdAndEndDateByEmpIdsAndPoId(empIds, poId);

    if (rows == null || rows.isEmpty()) {
        return result;
    }

    for (Object[] row : rows) {
        Long empId = ((Number) row[0]).longValue();
      
        LocalDate endDate = row[2] != null
                ? ((java.time.LocalDateTime) row[2]).toLocalDate()
                : null;
        
      
        if (result.containsKey(empId) && result.get(empId) == null) {
            continue;
        }

        if (endDate == null) {
            result.put(empId, null);
        } else {
            result.merge(empId, endDate,
                    (existing, newVal) -> existing.isAfter(newVal) ? existing : newVal);
        }
    }

    return result;
}


}
