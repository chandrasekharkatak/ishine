import { ProjectModule } from "./projectModule";
import { ProjectQuestion } from "./projectQuestion";

export class ProjectMilestone {

    empId: any;
    employeeRole: any;
    milestone: any;
    milestoneId: any;
    projectId: any;
    description: any;
    redmineId: any;
    assignedToUserId: any[] = [];
    assignedToEmployeementId: any[] = [];
    assignedToUserNames: any;
    taggedToUserId: any[] = [];
    taggedToUserNames: any;
    questionList: ProjectQuestion[] = [];
    moduleList: ProjectModule[] = [];
    isCollapsed: any = true;
    performanceTabName: any;
    createdOn: any;
    createdBy: any;
    updatedOn: any;
    updatedBy: any;
    toTagEmployeeList: any[] = [];
    toAssignEmployeeList: any[] = [];
    isQuestionCollapsed: boolean = true;
    badgePathList = [
        { name: 'Details', isUpdatedFromExcelUpload: 'No' },
        { name: 'Questions', isUpdatedFromExcelUpload: 'No' },
        { name: 'Modules', isUpdatedFromExcelUpload: 'No' },
    ];
    currentActiveBadgeLevel = 'Details';
    actionType: any;
    isUpdatedFromExcelUpload:any;
}

