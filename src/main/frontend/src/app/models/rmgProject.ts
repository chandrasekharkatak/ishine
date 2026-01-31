import { PoDetails } from "./poDetails";

export class RmgProject {

    projectId: any;
    projectName: any;
    clientName: any;
    state: any;
    startDate: any;
    endDate: any;
    projectStatus: any;
    draftProjectStatus: any;
    isDraftProject: any;
    poProjectType: any;
    internalProjectType: any;
    departmentIds: any[] = [];
    projectOverheadIds: any[] = [];
    projectManagerIds: any[] = [];
    poDetailsList: PoDetails[] = [];
    updatedBy: any;
    projectType: any;
    status:any;
    poProjectId:any;
    
}