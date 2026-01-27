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
}