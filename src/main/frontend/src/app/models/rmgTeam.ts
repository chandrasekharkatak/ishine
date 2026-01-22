import { RmgResourceRequirement } from "./rmgResourceRequirement";

export class RmgTeam {

    teamId: any;
    teamName: any;
    poId: any;
    isActive: any;
    spocId: any;
    spocName: any;
    createdBy: any;
    updatedBy: any;
    deptIds: any[] = [];
    departmentNames: any;
    rmgResourceRequirementList: RmgResourceRequirement[] = [];
    isTeamSelected: boolean = false;
}