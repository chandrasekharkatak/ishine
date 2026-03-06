import { PoDetails } from "./poDetails";
import { RmgTeam } from "./rmgTeam";

export class RmgProject {

    projectId: any;
    poProjectId: any;
    projectName: any;
    projectType: any;
    poProjectType: any;
    internalProjectType: any;
    clientName: any;
    state: any;
    startDate: any;
    endDate: any;
    status: any;
    projectStatus: any;
    draftProjectStatus: any;
    isDraftProject: any;

    totalRequirements: any;
    assignedApproved: any;
    assignedPending: any;
    difference: any;
    isAnyTeamSelected: boolean = false;
    isAnyNewTeamAdded: boolean = false;
    addNewTeamToggle: boolean = true;
    isAllTeamsSelected: boolean = false;

    selectedTeamIds: any[] = [];
    departmentIds: any[] = [];
    projectOverheadIds: any[] = [];
    projectManagerIds: any[] = [];
    dbProjectManagerIds: any[] = [];
    poDetailsList: PoDetails[] = [];

    newTeamObj: RmgTeam = new RmgTeam();
    teamDetailsList: RmgTeam[] = [];

    updatedBy: any;
}