import { ProjectInsightBadgePath } from "./projectInsightBadgePath";
import { SurveyOption } from "./sureyOption";

export class ProjectInsightQuestionDetails {

    id: any;
    question: any;
    description: any;
    required: any;
    documentUpload: any;
    currentActiveBadgeLevel: any;
    optionType: any;
    parentId: any;
    parentType: any;

    optionsList: SurveyOption[] = [];
    badgePathList: ProjectInsightBadgePath[] = [];
    toTaggedEmployeeIdList: any[] = [];
    toAssignedEmployeeIdList: any[] = [];
    parentPathIds: any[] = [];

    createdBy: any;
    createdOn: any;
    updatedBy: any;
    updatedOn: any;
}