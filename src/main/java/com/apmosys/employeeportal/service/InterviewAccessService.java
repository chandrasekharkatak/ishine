package com.apmosys.employeeportal.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.apmosys.employeeportal.dto.InterviewVisibilityScope;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.JobRole;
import com.apmosys.employeeportal.model.RoleFeatureMap;
import com.apmosys.employeeportal.model.SubFeatureMaster;
import com.apmosys.employeeportal.model.UserSession;
import com.apmosys.employeeportal.repository.DepartmentRepository;
import com.apmosys.employeeportal.repository.EmployeeLeaveRepository;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.JobRoleRepository;
import com.apmosys.employeeportal.repository.RoleFeatureMapRepository;
import com.apmosys.employeeportal.repository.SubFeatureMasterRepository;
import com.apmosys.employeeportal.repository.UserSessionRepository;
import com.apmosys.employeeportal.utility.InterviewConstants;

@Service
public class InterviewAccessService {

    @Autowired
    private HttpServletRequest httpRequest;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JobRoleRepository jobRoleRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeLeaveRepository employeeLeaveRepository;

    @Autowired
    private SubFeatureMasterRepository subFeatureMasterRepository;

    @Autowired
    private RoleFeatureMapRepository roleFeatureMapRepository;

    public Long getCurrentEmpId() {
        String sessionToken = httpRequest.getHeader("Authorization");
        if (sessionToken != null && sessionToken.startsWith("Bearer ")) {
        	String[] parts = sessionToken.substring(7).split("\\|"); // 7 removes "Bearer "
            String token = parts[0];
            String empId = parts.length > 1 ? parts[1] : null;
            return empId ==null || empId.equals("")?null:Long.parseLong(empId);
        }
        return null;
    }

    public boolean hasSubFeature(Long empId, String subFeatureName) {
        if (empId == null || !StringUtils.hasText(subFeatureName)) {
            return false;
        }
        Employee employee = employeeRepository.findByEmpId(empId);
        if (employee == null || employee.getJobRoleId() == null) {
            return false;
        }
        SubFeatureMaster subFeature = subFeatureMasterRepository.findBySubFeatureName(subFeatureName);
        if (subFeature == null) {
            return false;
        }
        RoleFeatureMap mapping = roleFeatureMapRepository.findByJobRoleIdAndSubFeatureMasterId(
                employee.getJobRoleId(), subFeature.getSubFeatureMasterId());
        return mapping != null;
    }

    public void requireSubFeature(Long empId, String subFeatureName) {
        if (!hasSubFeature(empId, subFeatureName)) {
            throw new SecurityException("Access denied: " + subFeatureName);
        }
    }

    public boolean canViewList(Long empId) {
        return hasSubFeature(empId, InterviewConstants.SUB_VIEW);
    }

    public boolean canSchedule(Long empId) {
        return hasSubFeature(empId, InterviewConstants.SUB_SCHEDULE);
    }

    public boolean canEdit(Long empId) {
        return hasSubFeature(empId, InterviewConstants.SUB_EDIT);
    }

    public boolean canDelete(Long empId) {
        return hasSubFeature(empId, InterviewConstants.SUB_DELETE);
    }

    public InterviewVisibilityScope resolveVisibilityScope(Long empId) {
        if (empId == null) {
            return InterviewVisibilityScope.none();
        }
        Employee employee = employeeRepository.findByEmpId(empId);
        if (employee == null || employee.getJobRoleId() == null) {
            return InterviewVisibilityScope.none();
        }
        JobRole jobRole = jobRoleRepository.findByjobRoleId(employee.getJobRoleId());
        if (jobRole == null) {
            return InterviewVisibilityScope.none();
        }

        if (isFullAccessRole(jobRole)) {
            return InterviewVisibilityScope.all();
        }

        // HOD / Department Head: department scope only — never the full manager hierarchy tree
        if (isHodUser(empId, jobRole)) {
            List<Long> deptIds = resolveHodDepartmentIds(empId, jobRole);
            if (deptIds.isEmpty()) {
                return InterviewVisibilityScope.none();
            }
            List<Long> empIds = employeeRepository.findActiveEmpIdsByDepartmentIds(deptIds);
            return InterviewVisibilityScope.restricted(empIds, deptIds);
        }

        if (isManagerRole(jobRole)) {
            List<Long> teamIds = employeeLeaveRepository.fetchEmployeeIdsByHirarchy(empId);
            if (teamIds != null && teamIds.size() > 1) {
                return InterviewVisibilityScope.restricted(teamIds, null);
            }
        }

        return InterviewVisibilityScope.restricted(List.of(empId), null);
    }

    public boolean canAccessInterview(Long empId, Long interviewEmployeeId, Long interviewCreatedBy, Long interviewDepartmentId) {
        if (!canViewList(empId)) {
            return false;
        }
        InterviewVisibilityScope scope = resolveVisibilityScope(empId);
        if (scope.isViewAll()) {
            return true;
        }
        if (!scope.hasVisibility()) {
            return false;
        }
        Set<Long> allowedEmps = new HashSet<>(scope.getVisibleEmployeeIds());
        if (interviewEmployeeId != null && allowedEmps.contains(interviewEmployeeId)) {
            return true;
        }
        if (interviewCreatedBy != null && allowedEmps.contains(interviewCreatedBy)) {
            return true;
        }
        if (interviewDepartmentId != null && scope.getVisibleDepartmentIds().contains(interviewDepartmentId)) {
            return true;
        }
        return false;
    }

    private boolean isFullAccessRole(JobRole jobRole) {
        String employeeRole = jobRole.getEmployeeRole() != null ? jobRole.getEmployeeRole().trim() : "";
        String roleName = jobRole.getName() != null ? jobRole.getName().trim() : "";
        if (equalsAnyIgnoreCase(employeeRole, "RMG", "HR", "Admin", "SuperAdmin")) {
            return true;
        }
        if (equalsAnyIgnoreCase(roleName, "Director", "Admin", "Super Admin", "SuperAdmin")) {
            return true;
        }
        String roleLower = roleName.toLowerCase();
        return roleLower.contains("director") && !roleLower.contains("department head");
    }

    /** Registered as department.hod_id or job role is HOD / Department Head. */
    private boolean isHodUser(Long empId, JobRole jobRole) {
        return departmentRepository.existsByHodId(empId) || isHodRole(jobRole);
    }

    private boolean isHodRole(JobRole jobRole) {
        String employeeRole = jobRole.getEmployeeRole() != null ? jobRole.getEmployeeRole().trim() : "";
        if (equalsAnyIgnoreCase(employeeRole, "HOD")) {
            return true;
        }
        String roleName = jobRole.getName() != null ? jobRole.getName().trim().toLowerCase() : "";
        return roleName.contains("department head")
                || roleName.endsWith(" dept head")
                || (roleName.contains("head") && roleName.contains("department"));
    }

    /**
     * Departments this HOD may see: from department.hod_id, else job_role.dept_id for HOD-titled roles.
     */
    private List<Long> resolveHodDepartmentIds(Long empId, JobRole jobRole) {
        if (departmentRepository.existsByHodId(empId)) {
            List<Long> fromTable = departmentRepository.findDeptIdsByHodId(empId);
            return fromTable != null ? fromTable : new ArrayList<>();
        }
        Set<Long> deptIds = new LinkedHashSet<>();
        if (jobRole.getDeptId() != null) {
            deptIds.add(jobRole.getDeptId());
        }
        if (deptIds.isEmpty()) {
            Long deptId = departmentRepository.findDepartmentofCurrentuser(jobRole.getJobRoleId());
            if (deptId != null) {
                deptIds.add(deptId);
            }
        }
        return new ArrayList<>(deptIds);
    }

    private boolean isManagerRole(JobRole jobRole) {
        if (isHodRole(jobRole)) {
            return false;
        }
        String employeeRole = jobRole.getEmployeeRole() != null ? jobRole.getEmployeeRole().trim() : "";
        if (equalsAnyIgnoreCase(employeeRole, "Manager", "Reporting Manager", "ReportingManager")) {
            return true;
        }
        String roleName = jobRole.getName() != null ? jobRole.getName().trim().toLowerCase() : "";
        return roleName.contains("manager") && !roleName.contains("department head");
    }

    private boolean equalsAnyIgnoreCase(String value, String... candidates) {
        for (String candidate : candidates) {
            if (candidate.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
