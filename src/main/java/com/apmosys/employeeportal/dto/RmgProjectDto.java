package com.apmosys.employeeportal.dto;

import java.util.List;

import com.apmosys.employeeportal.utility.TypeConversionUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RmgProjectDto {

    private Integer projectId;
    private Long poProjectId;
    private String projectName;
    private String clientName;
    private String state;

    private String startDate;
    private String endDate;
    private String status; // PO status
    private String projectStatus; // IShine status
    private String draftProjectStatus; // Is_Draft status
    private String isDraftProject;

    private String poProjectType;
    private String internalProjectType;

    private List<Long> departmentIds;
    private List<Long> projectOverheadIds;
    private List<Long> projectManagerIds;
    private List<PoDetailsDto> poDetailsList;

    private Long updatedBy;
    private String projectType;

    public RmgProjectDto(Integer projectId, String projectName, String clientName, String state, String startDate,
            String endDate, String projectStatus, String draftProjectStatus, String isDraftProject,
            String poProjectType, String internalProjectType) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.clientName = clientName;
        this.state = state;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectStatus = projectStatus;
        this.draftProjectStatus = draftProjectStatus;
        this.isDraftProject = isDraftProject;
        this.poProjectType = poProjectType;
        this.internalProjectType = internalProjectType;
    }

    public RmgProjectDto(Object[] row) {
        this.projectId = TypeConversionUtil.safeParseInt(row[0]);
        this.projectName = TypeConversionUtil.getSafeString(row[1]);
        this.clientName = TypeConversionUtil.getSafeString(row[2]);
        this.state = TypeConversionUtil.getSafeString(row[3]);
        this.startDate = TypeConversionUtil.getSafeString(row[4]);
        this.endDate = TypeConversionUtil.getSafeString(row[5]);
        this.projectStatus = TypeConversionUtil.getSafeString(row[6]);
        this.draftProjectStatus = TypeConversionUtil.getSafeString(row[7]);
        this.poProjectType = TypeConversionUtil.getSafeString(row[8]);
        this.internalProjectType = TypeConversionUtil.getSafeString(row[9]);
        this.status = TypeConversionUtil.getSafeString(row[10]);
        this.poProjectId = TypeConversionUtil.safeParseLong(row[11]);
    }

}
