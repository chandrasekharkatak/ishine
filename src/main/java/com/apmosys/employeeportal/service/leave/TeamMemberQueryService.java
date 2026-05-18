package com.apmosys.employeeportal.service.leave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.repository.EmployeeRepository;

/**
 * Resolves team membership for leave authorization without coupling validators to {@code EmployeeLeaveService}.
 */
@Service
public class TeamMemberQueryService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public List<Long> getTeamMemberEmpIds(Long empId) {
        if (empId == null) {
            return Collections.emptyList();
        }
        List<Object[]> list = employeeRepository.getAllTeamMemberView(empId);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> empList = new ArrayList<>();
        list.forEach((object) -> empList.add(
                object[0] != null ? Long.parseLong(object[0].toString()) : null));
        return empList;
    }
}
