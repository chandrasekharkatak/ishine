package com.apmosys.employeeportal.service.leave;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.dto.LeaveDTO;
import com.apmosys.employeeportal.dto.ProjectNameAndPrjoectIdDTO;
import com.apmosys.employeeportal.dto.TimesheetDTO_new.ProjectTimesheetDTO;
import com.apmosys.employeeportal.model.DayTypeMasterNew;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTimesheetLocationMapping;
import com.apmosys.employeeportal.model.EmployeeTimesheetsNew;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetLocationMappingRepository;
import com.apmosys.employeeportal.repository.EmployeeTimesheetsNewRepository;
import com.apmosys.employeeportal.repository.HolidayRepository;
import com.apmosys.employeeportal.repository.ProjectRepository;
import com.apmosys.employeeportal.service.ProjectTimesheetService;

/**
 * Persists system-generated leave rows in the new timesheet model (header + location + project rows).
 * Not routed through {@code TimesheetServiceNew} — that path targets interactive user entry and different validations.
 */
@Service
public class LeaveAppliedTimesheetWriter {

    private static final Logger log = LoggerFactory.getLogger(LeaveAppliedTimesheetWriter.class);

    @Autowired
    private EmployeeTimesheetsNewRepository employeeTimesheetsNewRepository;

    @Autowired
    private EmployeeTimesheetLocationMappingRepository employeeTimesheetLocationMappingRepository;

    @Autowired
    private ProjectTimesheetService projectTimesheetService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    /**
     * After a leave application is saved: clean conflicting manual timesheets (when &gt; 0.5 days) and create leave day rows.
     */
    public void syncTimesheetsAfterLeaveApplication(LeaveDTO leaveDTO, LocalDate fromDate, LocalDate toDate,
            DayTypeMasterNew leaveDayType) {
        long elapsedDays = ChronoUnit.DAYS.between(fromDate, toDate);
        List<Object[]> holidayList = holidayRepository.getHolidayWeekOffSize(
                leaveDTO.getFromDate(), leaveDTO.getToDate(), leaveDTO.getState());

        if (leaveDTO.getNoOfDays() != null && leaveDTO.getNoOfDays() > 0.5) {
            List<EmployeeTimesheetsNew> existingTimeSheet = employeeTimesheetsNewRepository
                    .findByEmpIdAndDateBetween(leaveDTO.getEmpId(), fromDate, toDate);
            if (existingTimeSheet != null && !existingTimeSheet.isEmpty()) {
                for (EmployeeTimesheetsNew ts : existingTimeSheet) {
                    if (!Boolean.TRUE.equals(ts.getIsSystemGenerated())) {
                        Long currentTsId = ts.getTimesheetId();
                        employeeTimesheetsNewRepository.cleanTimesheetById(currentTsId);
                    }
                }
            }
        }

        if (elapsedDays == 0) {
            boolean isHalfDay = Objects.equals(leaveDTO.getFromDateDayType(), 0.5f)
                    || Objects.equals(leaveDTO.getToDateDayType(), 0.5f);
            if (!isHalfDay) {
                LocalDateTime startOfDay = fromDate.atStartOfDay();
                LocalDateTime endOfDay = fromDate.atTime(LocalTime.MAX);
                saveRelationalLeaveTimesheet(leaveDTO, fromDate, startOfDay, endOfDay, leaveDayType);
            }
        } else {
            LocalDate tempDateToday = fromDate;
            while (!tempDateToday.isAfter(toDate)) {
                boolean isStartHalf = tempDateToday.isEqual(fromDate)
                        && (leaveDTO.getFromDateDayType() != null && leaveDTO.getFromDateDayType() == 0.5f);
                boolean isEndHalf = tempDateToday.isEqual(toDate)
                        && (leaveDTO.getToDateDayType() != null && leaveDTO.getToDateDayType() == 0.5f);
                if (!isStartHalf && !isEndHalf) {
                    boolean isHoliday = false;
                    if ("false".equalsIgnoreCase(leaveDTO.getIsWeekOffsExcluded()) && holidayList != null) {
                        for (Object[] holiday : holidayList) {
                            if (holiday[1] != null && tempDateToday.equals(LocalDate.parse(holiday[1].toString()))) {
                                isHoliday = true;
                                break;
                            }
                        }
                    }
                    if (!isHoliday) {
                        LocalDateTime startOfThisDay = tempDateToday.atStartOfDay();
                        LocalDateTime endOfThisDay = tempDateToday.atTime(LocalTime.MAX);
                        saveRelationalLeaveTimesheet(leaveDTO, tempDateToday, startOfThisDay, endOfThisDay, leaveDayType);
                    }
                }
                tempDateToday = tempDateToday.plusDays(1);
            }
        }
    }

    /**
     * Creates one day of system leave timesheet (header, default location mapping, project rows).
     */
    public void saveRelationalLeaveTimesheet(LeaveDTO leaveDTO, LocalDate date, LocalDateTime startOfDay,
            LocalDateTime endOfDay, DayTypeMasterNew leaveDayType) {
        Long userId = (leaveDTO.getCreatedBy() != null) ? leaveDTO.getCreatedBy() : leaveDTO.getUpdatedBy()!= null
            ? leaveDTO.getUpdatedBy().longValue()
            : leaveDTO.getLeaveStatusUpdatedBy() != null
                ? leaveDTO.getLeaveStatusUpdatedBy().longValue()
                : null;
        if (userId == null) {
            throw new RuntimeException("Cannot save timesheet: Both CreatedBy and UpdatedBy are null in LeaveDTO.");
        }
        if (leaveDTO.getCreatedBy() == null) {
            leaveDTO.setCreatedBy(userId);
        }

        EmployeeTimesheetsNew tsHeader = new EmployeeTimesheetsNew();
        tsHeader.setEmpId(leaveDTO.getEmpId());
        tsHeader.setDate(date);
        tsHeader.setDayTypeId(leaveDayType.getDayTypeId());
        tsHeader.setLeaveTypeMasterId(leaveDTO.getLeaveTypeMasterId().longValue());
        tsHeader.setTotalWorkingMinutes(0);
        tsHeader.setStatus(2);
        tsHeader.setIsNightShift(false);
        tsHeader.setCurrentManagerId(leaveDTO.getManagerId().longValue());
        tsHeader.setCreatedBy(leaveDTO.getCreatedBy());
        tsHeader.setCreatedOn(LocalDateTime.now());
        tsHeader.setDescription(leaveDayType.getDayType());

        tsHeader = employeeTimesheetsNewRepository.save(tsHeader);
        Long newTsId = tsHeader.getTimesheetId();

        EmployeeTimesheetLocationMapping locMapping = EmployeeTimesheetLocationMapping.builder()
                .timesheetId(newTsId)
                .locationTypeId(4)
                .locationInTime(null)
                .locationOutTime(null)
                .build();
        locMapping = employeeTimesheetLocationMappingRepository.save(locMapping);

        List<ProjectNameAndPrjoectIdDTO> projectDTOList = employeeTimesheetsNewRepository.getProjectListForDateAndEmpId(
                leaveDTO.getEmpId(), startOfDay, endOfDay);

        if (projectDTOList != null && !projectDTOList.isEmpty()) {
            for (ProjectNameAndPrjoectIdDTO projDto : projectDTOList) {
                try {
                    ProjectTimesheetDTO projectDTO = new ProjectTimesheetDTO();
                    projectDTO.setTimesheetId(newTsId);
                    projectDTO.setLocationMappingId(locMapping.getLocationMappingId());
                    projectDTO.setProjectId(projDto.getProjectId());
                    projectDTO.setStatus(2);
                    projectDTO.setActivities(null);
                    projectTimesheetService.create(newTsId, projectDTO, leaveDTO.getCreatedBy());
                } catch (Exception e) {
                    log.error("Failed to create project status for Project ID: {}", projDto.getProjectId(), e);
                    throw new RuntimeException("Error creating project-level timesheet status", e);
                }
            }
        } else {
            try {
                ProjectTimesheetDTO defaultProject = new ProjectTimesheetDTO();
                defaultProject.setTimesheetId(newTsId);
                defaultProject.setLocationMappingId(locMapping.getLocationMappingId());
                defaultProject.setStatus(2);
                defaultProject.setActivities(null);

                int resolvedProjectId = 0;
                Employee emp = employeeRepository.findByEmpId(leaveDTO.getEmpId());
                if (emp != null && emp.getJobRoleId() != null) {
                    JobRole jobRole = jobRoleRepository.findById(emp.getJobRoleId()).orElse(null);
                    if (jobRole != null && jobRole.getDeptId() != null) {
                        Optional<Integer> benchProjectId = projectRepository
                                .findBenchProjectIdByDeptId(jobRole.getDeptId());
                        if (benchProjectId.isPresent()) {
                            resolvedProjectId = benchProjectId.get();
                        }
                    }
                }
                defaultProject.setProjectId(resolvedProjectId);
                projectTimesheetService.create(newTsId, defaultProject, leaveDTO.getCreatedBy());
            } catch (Exception e) {
                throw new RuntimeException("Error creating default project timesheet", e);
            }
        }
    }
}
