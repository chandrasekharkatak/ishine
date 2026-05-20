export class MigrateTeams {

    sourceProjectId: any;
    targetProjectId: any;
    targetPoId: any;
    targetTeamId: any;
    targetRoleId: any;
    targetPoRequirementMappingId: any
    targetEmpTeamDepartmentId:any;
    currentUserEmpId: any;
    migrationTeamIds: any[] = [];
    mergeTeam: boolean = false;
    isAllMemberSelected: boolean = false;

    empId: any;
    projectType: any = 'Bench';
    isInternalProject: boolean = true;
    employeeRole: any;

    poDetailsList: any[] = [];
    teamList: any[] = [];
    resourceRequirementList: any[] = [];
    employeeRoles: any[] = [];
    updatedBy: any;
    empIds: any;
    etmIds: any[] = [];

}