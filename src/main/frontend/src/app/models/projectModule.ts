import { ProjectQuestion } from "./projectQuestion";
import { ProjectSubModule } from "./projectSubModule";

export class ProjectModule {
    assignTo: any;
    redmineId: any;
    moduleId: any;
    module: any;
    description: any;
    subModuleList:ProjectSubModule[] = [];
    projectQuestion:ProjectQuestion[] = [];
}
