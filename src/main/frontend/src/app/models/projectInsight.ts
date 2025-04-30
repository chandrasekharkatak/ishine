import { ProjectInsightEntity } from "./projectInsightEntity";
import { ProjectMilestone } from "./projectMilestone";
import { ProjectQuestion } from "./projectQuestion";

export class ProjectInsight {

    projectId: any;
    projectName: any;
    projectManagerId: any;
    projectManagerName: any;
    empId: any;
    employeeRole: any;
    isActive: any;
    description: any;
    performanceTabName: any;
    toAssignEmployeeList: any[] = [];
    assignedToUserId: any[] = [];
    assignedToUserNames: any;
    toTagEmployeeList: any[] = [];
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [];
    projectInsightMilestoneList: ProjectMilestone[] = [];
    deletedProjectInsightEntityList: ProjectInsightEntity[] = [];
    isFinalSubmitted: any = 'N';
    responseBy: any;
    createdOn: any;
    createdBy: any;
    updatedOn: any;
    updatedBy: any;
    badgePathList = [
        { name: 'Details' },
        { name: 'Questions' },
        { name: 'Milestones' },
    ];
    currentActiveBadgeLevel = 'Details';
    pointsBy: any;
    isPointsDrafted: any = true;
    transferToKnowledgeHub: any;
    isExcelUploaded: boolean;
}
