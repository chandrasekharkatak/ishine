import { EmployeeOtherActiveProject } from "./employeeOtherActiveProject";

export class RmgTeamMember {

    projectId: any;
    projectName: any;
    projectStatus: any;
    projectType: any = 'Bench';
    poProjectType: any;
    internalProjectType: any;

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
    poRequirementMappingId: any;
    role: any;
    experience: any;
    department: any;
    isPrmActive: any;
    employeeRole: any;
    employeeRoles: any[] = [];

    empId: any
    memberName: any;
    jobRoleName: any;
    currentExp: any;
    totalExp: any;
    startDate: any;
    endDate: any;
    isShadow: any;
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