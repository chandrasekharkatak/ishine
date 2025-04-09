import { ProjectModule } from "./projectModule";
import { ProjectQuestion } from "./projectQuestion";
import { ProjectSubModule } from "./projectSubModule";

export class ProjectInsightQuestion {
    questionId: any;
    empId:any;
    isActive: any;
    description: any;
    createdOn: any;
    createdBy: any;
    updatedOn: any;
    updatedBy: any;
    projectId: any;
    projectManagerId: any;
    projectManagerName: any;
    projectName: any;
    employeeRole: any;
    projectInsightQuestionList:any;
    projectInsightQuestionTemplate:any;
    taggedForHelp:any[]=[];
    taggedToUserNames:any;
    milestone: any;
    milestoneId: any;
    assignedTo: any;
    redmineId: any;
    applicationQuestion:ProjectQuestion[] = [new ProjectQuestion()];
    projectQuestion:ProjectQuestion[] = [];
    moduleList:ProjectModule[] = [];
    isCollapsed:any= false;
    assignedToUserNames:any;
    performanceTabName:any;
}
