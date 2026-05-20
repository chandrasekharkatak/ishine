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

import com.apmosys.employeeportal.dto.EmpMappingDTO;
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
        Map<Long, List<LocalDate>> datesTimesheetFilled, List<IshineToPoEmployeeDTO> employees) {

    // 1. Fetch all employees in one call
    List<IshineToPoEmployeeDTO> emps = projectPoDetailsRepository.findEmployeesWithTimesheetCount(empIds, startDate, endDate, poId);
    if (emps != null) {
        employees.addAll(emps);
    }

    // 2. Fetch all working dates for all employees in one call
    List<Object[]> results = employeeTimesheetsNewRepository.findTimesheetDatesByEmpIdsAndDateBetween(empIds, startDate, endDate, projectId);

    // 3. Group dates by Employee ID into the map
    if (results != null) {
        for (Object[] row : results) {
            Long empId = ((Number) row[0]).longValue();
            LocalDate date = ((java.sql.Date) row[1]).toLocalDate();
            
            datesTimesheetFilled.computeIfAbsent(empId, k -> new ArrayList<>()).add(date);
        }
    }

    // 4. Get min and max working date.

}

public void getTimesheetMinMaxDateMap(List<Long> empIds, LocalDate startDate, LocalDate endDate, Integer projectId, 
                                     Map<Long, LocalDate> minDateMap, Map<Long, LocalDate> maxDateMap) {
    List<Object[]> results = employeeTimesheetsNewRepository.findMaxAndMinDateOfTimesheet(empIds, startDate, endDate, projectId);
    if (results != null) {
        for (Object[] row : results) {
            Long empId = ((Number) row[0]).longValue();
            LocalDate maxDate = row[1] != null ? ((java.sql.Date) row[1]).toLocalDate() : null;
            LocalDate minDate = row[2] != null ? ((java.sql.Date) row[2]).toLocalDate() : null;

            if (minDate != null) minDateMap.put(empId, minDate);
            if (maxDate != null) maxDateMap.put(empId, maxDate);
        }
    }
}

public List<IshineToPoEmployeeDTO> mergeShadowTimesheets(
        List<IshineToPoEmployeeDTO> employees,
        List<Long> allMappedEmpIds,
        Map<Long, List<LocalDate>> datesTimesheetFilled,
        Long poId, LocalDate startDate, LocalDate endDate) {

    List<Object[]> rawShadowRows = employeeTimesheetsNewRepository
            .findTimesheetShadowDetailsByPoAndEmpIds(poId, allMappedEmpIds, startDate, endDate);

    Map<Long, List<ShadowEntryDTO>> shadowMap = buildShadowMap(rawShadowRows);

    Map<Long, IshineToPoEmployeeDTO> empMap = new HashMap<>();
    for (IshineToPoEmployeeDTO emp : employees) {
        empMap.put(emp.getIshineEmpId(), emp);
    }

    for (Map.Entry<Long, List<ShadowEntryDTO>> shadowEntry : shadowMap.entrySet()) {

        Long shadowEmpId             = shadowEntry.getKey();
        IshineToPoEmployeeDTO shadowEmp = empMap.get(shadowEmpId);

        Map<Long, List<ShadowEntryDTO>> groupedByMainEmp = shadowEntry.getValue().stream()
                .collect(Collectors.groupingBy(ShadowEntryDTO::getShadowForEmpId));

        for (Map.Entry<Long, List<ShadowEntryDTO>> mainEntry : groupedByMainEmp.entrySet()) {
            mergeShadowForMainEmployee(
                    mainEntry.getKey(), mainEntry.getValue(),
                    shadowEmp, empMap, datesTimesheetFilled);
        }
    }

    return new ArrayList<>(empMap.values());
}



private void mergeShadowForMainEmployee(
        Long mainEmpId,
        List<ShadowEntryDTO> shadowEntries,
        IshineToPoEmployeeDTO shadowEmp,
        Map<Long, IshineToPoEmployeeDTO> empMap,
        Map<Long, List<LocalDate>> datesTimesheetFilled) {

    List<LocalDate> alreadyCredited = datesTimesheetFilled.get(mainEmpId);

    // Filter out dates the main employee has already been credited for
    List<ShadowEntryDTO> newDays = (alreadyCredited != null && !alreadyCredited.isEmpty())
            ? shadowEntries.stream()
                .filter(f -> !alreadyCredited.contains(f.getDate()))
                .collect(Collectors.toList())
            : new ArrayList<>(shadowEntries);

    if (newDays.isEmpty()) return;

    List<LocalDate> newDates = newDays.stream()
            .map(ShadowEntryDTO::getDate)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    LocalDate shadowMinDate = newDates.stream().min(LocalDate::compareTo).orElse(null);
    LocalDate shadowMaxDate = newDates.stream().max(LocalDate::compareTo).orElse(null);

    IshineToPoEmployeeDTO mainEmp = empMap.get(mainEmpId);

    if (mainEmp == null) {
        mainEmp = buildMinimalEmpDTO(mainEmpId, shadowEmp);
        if (mainEmp == null) return;
        empMap.put(mainEmpId, mainEmp);
        datesTimesheetFilled.put(mainEmpId, new ArrayList<>());
    }

    List<LocalDate> existingShadowDates = mainEmp.getShadowTimeSheetDate();
    if (existingShadowDates == null) {
        mainEmp.setShadowTimeSheetDate(new ArrayList<>(newDates));
    } else {
        // Another shadow already contributed dates — merge without duplicates
        // (two shadows filling for same main emp on same day should still count once)
        for (LocalDate d : newDates) {
            if (!existingShadowDates.contains(d)) {
                existingShadowDates.add(d);
            }
        }
    }


    if (shadowMinDate != null &&
            (mainEmp.getStartDate() == null || shadowMinDate.isBefore(mainEmp.getStartDate())))
        mainEmp.setStartDate(shadowMinDate);

    if (shadowMaxDate != null &&
            (mainEmp.getEndDate() == null || shadowMaxDate.isAfter(mainEmp.getEndDate())))
        mainEmp.setEndDate(shadowMaxDate);

    mainEmp.setMsg("Shadow's Timesheet Count Added with the Resource!!");

    // Update credited dates so the next shadow doesn't double-count
    datesTimesheetFilled.computeIfAbsent(mainEmpId, k -> new ArrayList<>()).addAll(newDates);
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


/**
 * Returns a map of empId -> maximum endDate (as LocalDateTime) for the given
 * employees under the specified PO.
 *
 * Key rule: if ANY entry for an employee has a NULL end date, the result for
 * that employee is NULL — because NULL means "currently active / no end",
 * which is the logical maximum.
 *
 * @param empIds  list of employee IDs to check
 * @param poId    the PO ID
 * @return map where value is null if the employee is still active, otherwise
 *         the latest non-null end date
 */
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
        // endDate in entity is LocalDateTime — convert to LocalDate
        LocalDate endDate = row[1] != null
                ? ((java.time.LocalDateTime) row[1]).toLocalDate()
                : null;

        // If we've already found a null for this employee, keep null (it's the max)
        if (result.containsKey(empId) && result.get(empId) == null) {
            continue;
        }

        if (endDate == null) {
            // null beats any date — employee is still active
            result.put(empId, null);
        } else {
            result.merge(empId, endDate,
                    (existing, newVal) -> existing.isAfter(newVal) ? existing : newVal);
        }
    }

    return result;
}


}
