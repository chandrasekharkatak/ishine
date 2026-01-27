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

    poId: any;
    poNo: any;
    poRequirementMappingId: any;
    role: any;
    experience: any;
    department: any;
    isPrmActive: any;

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
    memberDepartment: any
    employementId: any;
    billableType: any;
    prevExp: any;
    isNotSaved: boolean = false;

    isMemberSelected: boolean = false;
    otherActiveProjectIds: any[] = [];
    employeeRole: any;
    employeeRoles: any[] = [];

    clientId: any;
    clientName: any;

    poDetailsList: any[] = [];
    teamList: any[] = [];
    resourceRequirementList: any[] = [];

    createdBy: any;
    updatedBy: any;
    isCustomDate: any;
    rescEndDate: any;
    rescRemovedBy: any;
}