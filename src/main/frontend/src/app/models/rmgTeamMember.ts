import { EmployeeOtherActiveProject } from "./employeeOtherActiveProject";

export class RmgTeamMember {

    projectId: any;
    projectName: any;
    projectStatus: any;
    projectType: any = 'Bench';
    calculatedProjectType: any;
    isInternalProject: boolean;
    poProjectType: any;
    internalProjectType: any;
    projectStartDate: any;

    teamId: any;
    teamName: any;
    isTeamActive: any;
    deptIds: any;
    spocId: any;
    spocName: any;
    etmId: any;
    isCustomDate: any;
    rescEndDate: any;
    rescRemovedBy: any;

    poId: any;
    poNo: any;
    poStartDate: any;
    poEndDate: any;
    poRequirementMappingId: any;

    roleId: any;
    role: any;
    experience: any;
    department: any;
    displayRequirement: any;
    isPrmActive: any;
    employeeRole: any;
    employeeRoles: any[] = [];

    empId: any
    empTeamDepartmentId: any;
    empTeamDepartmentName: any;
    memberName: any;
    jobRoleName: any;
    employmentStatus: any;
    currentExp: any;
    totalExp: any;
    startDate: any;
    dbStartDate: any
    endDate: any;
    dbEndDate: any;
    isShadow: any;
    dbIsShadow: any;
    isMemberActive: any;
    defaultProject: any;
    dbDefaultProject: any;
    memberDepartment: any
    employementId: any;
    billableType: any;
    prevExp: any;

    clientId: any;
    clientName: any;

    createdBy: any;
    updatedBy: any;
    selectedEmpIds: number[] = [];
    projectIds: number[] = [];
    mappedDefaultProjectId: any;
    otherActiveProjectIds: any[] = [];
    otherActiveProjects: EmployeeOtherActiveProject[] = [];

    isNotSaved: boolean = false;
    isMemberSelected: boolean = false;
    projectList: any[] = [];
    poDetailsList: any[] = [];
    teamList: any[] = [];
    resourceRequirementList: any[] = [];
    selectedProject?: EmployeeOtherActiveProject;
}