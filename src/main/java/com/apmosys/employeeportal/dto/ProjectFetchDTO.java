package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.apmosys.employeeportal.model.ProjectManagerMapping;

@Getter
@Setter
@NoArgsConstructor
public class ProjectFetchDTO {
	private Long id;
	private Integer projectId;
	private Timestamp createdOn;
	private String name;
	private String state;
	private Integer clientId;
	private Long poProjectId;
	private String active;
	private String syncProject;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime updatedOn;
	private String isDraftProject;
	private String poEndDate;
	private String poNo;
	private String poProjectType;
	private String poStartDate;
	private String apmosysRM;
	private String clientRM;
	private String deptId;
	private Boolean isRenewable;
	private String status;
	private String apmosysRmEmail;
	private String projectCompletionDate;
	private String projectStatus;
	private String internalProjectType;
	private String clientName;
	private String draftStatus;
	private String isTeamCreated;
	private String projectViewId;
	private String projectType;
	private Integer isActive;
	private List<ProjectManagersDTO> projectManagers;
	private List<Long> projectManagerId;
	private List<ProjectOverheadsDTO> projectOverheads;
	private List<Long> projectOverheadId;
	private List<String> department;
	private List<ResourceRequirementDTO> resourceRequirements;
    private String projectName;
    private String clientLocation;
    
    private String departmentNames;
    private String draftProjectStatus;
    private String displayProjectId;
    private String projectManager;
    private String poProjectStatus;
    private Timestamp createdOn1;

    //internal projects
    public ProjectFetchDTO(Object[] row) {
        this.projectId = row[0] != null ? ((Number) row[0]).intValue() : null;          
        this.name = (String) row[1];                                              
        this.poNo = (String) row[2];                                                    
        this.clientId = row[3] != null ? ((Number) row[3]).intValue() : null;          
        this.poProjectId = row[4] != null ? ((Number) row[4]).longValue() : null;       
        this.active = (String) row[5];                                                  
        this.poProjectType = (String) row[6];                                            
        this.projectManager = (String) row[7];                                           
        this.clientName = (String) row[8];                                              
        this.clientRM = (String) row[9];                                                 
        this.deptId = row[10] != null ? row[10].toString() : null;                     
        this.apmosysRM = (String) row[11];                                              
        this.poStartDate = row[12] != null ? row[12].toString() : null;                
        this.poEndDate = row[13] != null ? row[13].toString() : null;                 
        this.state = (String) row[14];                                                 
        this.createdOn = row[15] != null ? (Timestamp) row[15] : null;                 
        this.status = (String) row[16];                                        
        this.projectCompletionDate = row[17] != null ? row[17].toString() : null;      
        this.projectStatus = (String) row[18];                                          
        this.internalProjectType = (String) row[19];                                                                 
        this.draftStatus = (String) row[20];                                   
        this.projectViewId = (String) row[21];                                      
        this.departmentNames = (String) row[22]; 
//        this.projectViewId = projectViewId;
    }

    public static ProjectFetchDTO createFromTotalProject(Object[] row) {
        ProjectFetchDTO dto = new ProjectFetchDTO();
        dto.projectId = row[0] != null ? ((Number) row[0]).intValue() : null;    
        dto.createdOn = row[1] != null ? (Timestamp) row[1] : null;                  
        dto.name = (String) row[2];                                                  
        dto.state = (String) row[3];                                                 
        dto.clientId = row[4] != null ? ((Number) row[4]).intValue() : null;         
        dto.poProjectId = row[5] != null ? ((Number) row[5]).longValue() : null;     
        dto.active = (String) row[6];                                               
        dto.syncProject = (String) row[7];                                           
        dto.createdBy = row[8] != null ? ((Number) row[8]).longValue() : null;       
        dto.updatedBy = row[9] != null ? ((Number) row[9]).longValue() : null;       
        dto.updatedOn = row[10] != null ? 
            (row[10] instanceof Timestamp ? ((Timestamp) row[10]).toLocalDateTime() : null) : null; 
        dto.isDraftProject = (String) row[11];                                      
        dto.poEndDate = row[12] != null ? row[12].toString() : null;                 
        dto.poNo = (String) row[13];                                               
        dto.poProjectType = (String) row[14];                                        
        dto.poStartDate = row[15] != null ? row[15].toString() : null;               
        dto.apmosysRM = (String) row[16];                                            
        dto.clientRM = (String) row[17];                                             
        dto.deptId = row[18] != null ? row[18].toString() : null;                    
        dto.isRenewable = row[19] != null ? (Boolean) row[19] : null;               
        dto.status = (String) row[20];                                               
        dto.apmosysRmEmail = (String) row[21];                                       
        dto.projectCompletionDate = row[22] != null ? row[22].toString() : null;     
        dto.projectStatus = (String) row[23];                                        
        dto.internalProjectType = (String) row[24];                                  
        dto.clientName = (String) row[25];                                         
        dto.draftStatus = (String) row[26];                                          
        dto.displayProjectId = (String) row[27];
        return dto;
    }
    

    
	public ProjectFetchDTO(Integer projectId, Timestamp createdOn, String projectName, String state, Integer clientId,
			Long poProjectId, String syncProject, Long createdBy, Long updatedBy,
			LocalDateTime updatedOn, String isDraftProject, String poEndDate, String poNo, String poProjectType,
			String poStartDate, String apmosysRM, String clientRM, String deptId, Boolean isRenewable, String status,
			String apmosysRmEmail, String projectCompletionDate, String projectStatus, String internalProjectType,String clientName,
			String draftStatus, String isTeamCreated) {
		this.projectId = projectId;
		this.createdOn = createdOn;
		this.name = projectName;
		this.state = state;
		this.clientId = clientId;
		this.poProjectId = poProjectId;
		this.syncProject = syncProject;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.isDraftProject = isDraftProject;
		this.poEndDate = poEndDate;
		this.poNo = poNo;
		this.poProjectType = poProjectType;
		this.poStartDate = poStartDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.deptId = deptId;
		this.isRenewable = isRenewable;
		this.status = status;
		this.apmosysRmEmail = apmosysRmEmail;
		this.projectCompletionDate = projectCompletionDate;
		this.projectStatus = projectStatus;
		this.internalProjectType = internalProjectType;
		this.clientName = clientName;
		this.draftStatus = draftStatus;
		this.isTeamCreated = isTeamCreated;
	}
	
	public ProjectFetchDTO(Integer projectId, Timestamp createdOn, String projectName, String state, Integer clientId,
			Long poProjectId, String active, String syncProject, Long createdBy, Long updatedBy,
			LocalDateTime updatedOn, String isDraftProject, String poEndDate, String poNo, String poProjectType,
			String poStartDate, String apmosysRM, String clientRM, String deptId, Boolean isRenewable, String status,
			String apmosysRmEmail, String projectCompletionDate, String projectStatus,
			String draftStatus, String clientName) {
		this.projectId = projectId;
		this.createdOn = createdOn;
		this.name = projectName;
		this.state = state;
		this.clientId = clientId;
		this.poProjectId = poProjectId;
		this.active = active;
		this.syncProject = syncProject;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.isDraftProject = isDraftProject;
		this.poEndDate = poEndDate;
		this.poNo = poNo;
		this.poProjectType = poProjectType;
		this.poStartDate = poStartDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.deptId = deptId;
		this.isRenewable = isRenewable;
		this.status = status;
		this.apmosysRmEmail = apmosysRmEmail;
		this.projectCompletionDate = projectCompletionDate;
		this.projectStatus = projectStatus;
		this.draftStatus = draftStatus;
		this.clientName = clientName;
	}
	public ProjectFetchDTO(Integer projectId, Timestamp createdOn, String projectName, String state, Integer clientId,
			Long poProjectId, String active, String syncProject, Long createdBy, Long updatedBy,
			LocalDateTime updatedOn, String isDraftProject, String poEndDate, String poNo, String poProjectType,
			String poStartDate, String apmosysRM, String clientRM, String deptId, Boolean isRenewable, String status,
			String apmosysRmEmail, String projectCompletionDate, String projectStatus, String internalProjectType,String clientName,
			String draftStatus, String projectViewId) {
		this.projectId = projectId;
		this.createdOn = createdOn;
		this.name = projectName;
		this.state = state;
		this.clientId = clientId;
		this.poProjectId = poProjectId;
		this.active = active;
		this.syncProject = syncProject;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.isDraftProject = isDraftProject;
		this.poEndDate = poEndDate;
		this.poNo = poNo;
		this.poProjectType = poProjectType;
		this.poStartDate = poStartDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.deptId = deptId;
		this.isRenewable = isRenewable;
		this.status = status;
		this.apmosysRmEmail = apmosysRmEmail;
		this.projectCompletionDate = projectCompletionDate;
		this.projectStatus = projectStatus;
		this.projectType = internalProjectType;
		this.clientName = clientName;
		this.draftStatus = draftStatus;
		this.projectViewId = projectViewId;
//		this.projectType = projectType;
	}

	public ProjectFetchDTO(Integer projectId, Date createdOn, String projectName, String state, Integer clientId,
			Long poProjectId, String active, String syncProject, Long createdBy, Long updatedBy,
			LocalDateTime updatedOn, String isDraftProject, String poEndDate, String poNo, String poProjectType,
			String poStartDate, String apmosysRM, String clientRM, String deptId, Boolean isRenewable, String status,
			String apmosysRmEmail, String projectCompletionDate, String projectStatus, String internalProjectType,
			String clientName,
			String draftStatus, String projectViewId) {
		this.projectId = projectId;
		this.createdOn = (Timestamp) createdOn;
		this.name = projectName;
		this.state = state;
		this.clientId = clientId;
		this.poProjectId = poProjectId;
		this.active = active;
		this.syncProject = syncProject;
		this.createdBy = createdBy;
		this.updatedBy = updatedBy;
		this.updatedOn = updatedOn;
		this.isDraftProject = isDraftProject;
		this.poEndDate = poEndDate;
		this.poNo = poNo;
		this.poProjectType = poProjectType;
		this.poStartDate = poStartDate;
		this.apmosysRM = apmosysRM;
		this.clientRM = clientRM;
		this.deptId = deptId;
		this.isRenewable = isRenewable;
		this.status = status;
		this.apmosysRmEmail = apmosysRmEmail;
		this.projectCompletionDate = projectCompletionDate;
		this.projectStatus = projectStatus;
		this.projectType = internalProjectType;
		this.clientName = clientName;
		this.draftStatus = draftStatus;
		this.projectViewId = projectViewId;
		this.id = poProjectId;
		// this.projectType = projectType;
	}
	public ProjectFetchDTO(Integer projectId,String projectName,String isDraftProject,Integer isActive,String projectStatus){
		this.projectId = projectId;
		this.name = projectName;
		this.isDraftProject = isDraftProject;
		this.isActive = isActive;
		this.projectStatus = projectStatus;
	}
}
