import { Team } from "./team";

export class PoDetails {

    id: any;
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
    assignedAndApproved: any;
    assignedButPending: any;
    difference: any;
    newTeamObj: Team = new Team();
}