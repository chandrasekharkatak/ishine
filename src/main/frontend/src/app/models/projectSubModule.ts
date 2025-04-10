import { ProjectQuestion } from "./projectQuestion";

export class ProjectSubModule {

    subModule: any;
    subModuleId: any;
    description: any;
    redmineId: any;
    assignedToUserId: any[] = [];
    assignedToUserNames: any;
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [];
    subSubModuleList: ProjectSubModule[] = [];
    isCollapsed: any = false;
    toTagEmployeeList: any[] = [];
    toAssignEmployeeList: any[] = [];
    indexName: any;
}
