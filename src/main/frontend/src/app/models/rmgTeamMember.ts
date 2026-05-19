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
    removePermanently: boolean = false;

    poId: any;
    poNo: any;
    poStartDate: any;
    poEndDate: any;
    poRequirementMappingId: any;
    lineItemStartDate: any;
    lineItemEndDate: any;

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
    selectedEtmIds: number[] = [];
    projectIds: number[] = [];
    mappedDefaultProjectId: any;
    otherActiveProjectIds: any[] = [];
    otherActiveProjects: EmployeeOtherActiveProject[] = [];

    isOverboardedRole: boolean = false;
    isNotSaved: boolean = false;
    isMemberSelected: boolean = false;
    isEndDateVisible: boolean = false;
    memberMinStartDate: any;
    memberMaxEndDate: any;
    memberList:any[] = [];
    projectList: any[] = [];
    teamList: any[] = [];
    poDetailsList: any[] = [];
    filteredPoDetailsList: any[] = [];
    resourceRequirementList: any[] = [];
    filteredActiveResourceRequirement: any[] = [];
    selectedProject?: EmployeeOtherActiveProject;
    roleFilterActionLabel: 'Show Active PO Roles' | 'Show All PO Roles' = 'Show Active PO Roles';
    poFilterActionLabel: 'Show Active PO' | 'Show All PO' = 'Show Active PO';

    // Optional calling page/module context for backend validation (e.g., 'RMG').
    validationContext?: string;
    // Optional source identifier for validation flow (e.g., 'CURRENT_TEAM_TEMPLATE').
    validationSource?: string;
    // Optional validation/help message to show in UI (e.g., future assignment guidance).
    validationMessage?: string;
    /** Raw overlap rows from CURRENT_TNM_PROJECT_OVERLAPPING (CURRENT_TEAM_TEMPLATE only). */
    tnmOverlapDetail?: any;

    requiredCount: any;
    actualAssigned: any;
    projectNewStartDate: any;
    isRestricted: any;
}