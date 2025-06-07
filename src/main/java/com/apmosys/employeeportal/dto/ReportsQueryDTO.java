package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportsQueryDTO {
    private List<Integer> employeement_id;
    private String name;
    private List<Integer> dept_id;
    private List<Integer> job_role_id;
    private List<Integer> manager_id;
    private List<Integer> team_id;
    private List<Integer> project_id;
    private List<Integer> client_id;
    private String employmentstatus;
    private String date_of_joining;
    private String city;
    private String blood_group;
    private String gender;
    private List<Integer> probation_period;
    private List<Integer> notice_id;
    private String marital_status;
    private String bank_name;
    private String state;
    private String created_on;
    private List<Integer> created_by;
    private String experience;
    private String work_location;
    private Integer year;
}
