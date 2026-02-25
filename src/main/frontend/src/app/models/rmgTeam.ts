import { RmgMemberEndDate } from "./rmgMemberEndDate";
import { RmgTeamMember } from "./rmgTeamMember";

export class RmgTeam {

    teamId: any;
    teamName: any;
    isActive: any;
    spocId: any;
    spocName: any;
    teamLeadId: any;
    teamLeadName: any;
    isCustomEndDate: boolean;
    endDate: any;
    projectId: any
    projectType: any;
    clientName: any;

    poId: any;
    poRequirementMappingId: any;
    count: any;
    assignedPending: any;
    assignedApproved: any;

    deptIds: any[] = [];
    departmentNames: any;
    requirementType: 'Current Resource' | 'Old Resource' = 'Current Resource';
    newRmgTeamMember: RmgTeamMember = new RmgTeamMember();
    rmgTeamMemberList: RmgTeamMember[] = [];
    rmgCurrentTeamMemberList: RmgTeamMember[] = [];
    rmgOldTeamMemberList: RmgTeamMember[] = [];
    employeeExistingProjectDetails: any[] = [];
    rmgMemberEndDateList: RmgMemberEndDate[] = [];

    isupdate: boolean = false;
    isNotSaved: boolean = false;
    isAnyMemberSelected: boolean = false;
    isAllMemberSelected: boolean = false;
    isAnyNewMemberAdded: boolean = false;
    isTeamSelected: boolean = false;

    createdBy: any;
    updatedBy: any;

}