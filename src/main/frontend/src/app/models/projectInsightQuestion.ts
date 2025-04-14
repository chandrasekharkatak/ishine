import { ProjectMilestone } from "./projectMilestone";
import { ProjectModule } from "./projectModule";
import { ProjectQuestion } from "./projectQuestion";
import { ProjectSubModule } from "./projectSubModule";

export class ProjectInsight {

    projectId: any;
    projectName: any;
    projectManagerId: any;
    projectManagerName: any;
    empId: any;
    employeeRole: any;
    isActive: any;
    description: any;
    assignedToUserId: any[] = [];
    assignedToUserNames: any;
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [new ProjectQuestion()];
    projectInsightMilestoneList: ProjectMilestone[] = [new ProjectMilestone()];
    isCollapsed: any = false;
    performanceTabName: any;
    projectInsightQuestionTemplate: any;
    createdOn: any;
    createdBy: any;
    updatedOn: any;
    updatedBy: any;
    toTagEmployeeList:any[]=[];
    toAssignEmployeeList:any[]=[];
    isQuestionCollapsed:boolean = false;
}
