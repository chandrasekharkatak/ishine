import { RmgTeam } from "./rmgTeam";
import { Team } from "./team";

export class PoDetails {

    id: any;
    poId: any;
    projectId: any;
    poNo: any;
    apmosysRM: any;
    clientRm: any;
    apmosysRmEmail: any;
    poStartDate: any;
    poEndDate: any;
    activeFlag: any;
    prevPO: any;
    nextPO: any;
    msg: any;
    clientLocationId: any;
    assigned: any;
    totalRequirements: any;
    assignedApproved: any;
    assignedPending: any;
    difference: any;
    newTeamObj: RmgTeam = new RmgTeam();
    teamList: RmgTeam[] = [];
    selectedTeamIds: any[] = [];
    isPoSelected: boolean = false;
    isAnyTeamSelected: boolean = false;
    isAnyNewTeamAdded: boolean = false;
    updatedBy: any;
    isHod: boolean = false;
    isupdate: boolean = true;
    projectType: any;

}