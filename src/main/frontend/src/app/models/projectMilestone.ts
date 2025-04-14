import { ProjectModule } from "./projectModule";
import { ProjectQuestion } from "./projectQuestion";

export class ProjectMilestone {

    empId: any;
    employeeRole: any;
    milestone: any;
    milestoneId: any;
    description: any;
    redmineId: any;
    assignedToUserId: any[] = [];
    assignedToUserNames: any;
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [new ProjectQuestion()];
    moduleList: ProjectModule[] = [new ProjectModule()];
    isCollapsed: any = true;
    performanceTabName: any;
    createdOn: any;
    createdBy: any;
    updatedOn: any;
    updatedBy: any;
    toTagEmployeeList:any[]=[];
    toAssignEmployeeList:any[]=[];
    isQuestionCollapsed:boolean = true;
}

