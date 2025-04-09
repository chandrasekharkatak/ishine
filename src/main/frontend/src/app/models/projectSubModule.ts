import { ProjectQuestion } from "./projectQuestion";

export class ProjectSubModule {
    assignedTo: any;
    redmineId: any;
    subModuleId: any;
    subModule: any;
    description: any;
    projectQuestion:ProjectQuestion[] = [];
    isCollapsed:any= false;
    assignedToUserNames:any
    taggedForHelp:any[]=[];
    taggedToUserNames:any;
}
