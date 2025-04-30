import { ProjectQuestion } from "./projectQuestion";

export class ProjectSubModule {

    subModule: any;
    subModuleId: any;
    moduleId: any;
    description: any;
    redmineId: any;
    assignedToUserId: any[] = [];
    assignedToUserNames: any;
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [];
    subSubModuleList: ProjectSubModule[] = [];
    isCollapsed: any = true;
    toTagEmployeeList: any[] = [];
    toAssignEmployeeList: any[] = [];
    indexName: any;
    isQuestionCollapsed: boolean = true;
    badgePathList = [
        { name: 'Details' },
        { name: 'Questions' },
        { name: 'Sub-SubModules' },
    ];
    currentActiveBadgeLevel = 'Details';
    actionType: any;
}
