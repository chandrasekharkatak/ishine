import { ProjectQuestion } from "./projectQuestion";
import { ProjectSubModule } from "./projectSubModule";

export class ProjectModule {
    assignedTo: any;
    redmineId: any;
    moduleId: any;
    module: any;
    description: any;
    subModuleList:ProjectSubModule[] = [];
    projectQuestion:ProjectQuestion[] = [];
    isCollapsed:any= false;
    assignedToUserNames:any;
}
