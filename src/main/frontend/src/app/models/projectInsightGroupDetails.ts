import { ProjectInsightQuestionDetails } from "./projectInsightQuestionDetails";

export class ProjectInsightGroupDetails {

    id: any;
    groupName: any;
    groupType: any;
    parentId: any;
    parentType: any;
    formId: any;
    questionList: ProjectInsightQuestionDetails[] = [];
    additionalInfo: Map<string, any>;
    createdBy: any;
    createdOn: any;
    updatedBy: any;
    updatedOn: any;
}