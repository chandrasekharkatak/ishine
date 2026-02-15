package com.apmosys.employeeportal.dto;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MigrateTeam {
    private Integer sourceProjectId;
    private Integer targetProjectId;
    private Long targetPoId;
    private Long targetTeamId;
    private Long currentUserEmpId;
    private List<Long> migrationTeamIds;
    private List<Long> empIds;
    private boolean mergeTeam;
    private Long targetRoleId;
}
