import { ProjectQuestion } from "./projectQuestion";

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


    milestone: any;
    projectQuestion:ProjectQuestion[] = [new ProjectQuestion()];
}
