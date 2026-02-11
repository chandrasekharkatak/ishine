import { RmgTeamMember } from "./rmgTeamMember";

export class RmgResourceRequirement {

    poRequirementMappingId: any;
    poId: any;
    role: any;
    experience: any;
    department: any;
    isPrmActive: any;
    newRmgTeamMember: RmgTeamMember = new RmgTeamMember();
    rmgTeamMemberList: RmgTeamMember[] = [];
    rmgCurrentTeamMemberList: RmgTeamMember[] = [];
    rmgOldTeamMemberList: RmgTeamMember[] = [];
    requirementType: 'Current Resource' | 'Old Resource' = 'Current Resource';
    isRequirementSelected: boolean = false;
    isAnyMemberSelected: boolean = false;
    isAnyNewMemberAdded: boolean = false;
    employeeExistingProjectDetails: any[] = [];
    displayValue: any;
    count: any;
    isNewRequirementInTeam: boolean = false;
    isupdate: boolean = false;
    projectId: any;
    teamId: any;
    updatedBy: any;
    clientName: any;
    projectType: any;
    endDate: any;
    isCustomEndDate: boolean;
    assignedPending: any;
    assignedApproved: any;
}