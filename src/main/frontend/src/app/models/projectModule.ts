import { ProjectQuestion } from "./projectQuestion";
import { ProjectSubModule } from "./projectSubModule";

export class ProjectModule {
    module: any;
    moduleId: any;
    milestoneId: any;
    description: any;
    redmineId: any;
    assignedToUserId: any;
    assignedToUserNames: any;
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [];
    subModuleList: ProjectSubModule[] = [];
    isCollapsed: any = true;
    toTagEmployeeList: any[] = [];
    toAssignEmployeeList: any[] = [];
    isQuestionCollapsed: boolean = true;
    badgePathList = [
        { name: 'Details' },
        { name: 'Questions' },
        { name: 'SubModules' },
    ];
    currentActiveBadgeLevel = 'Details';
    actionType: any;
}
