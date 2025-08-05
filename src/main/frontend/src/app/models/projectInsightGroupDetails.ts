import { ProjectInsightQuestionDetails } from "./projectInsightQuestionDetails";

export class ProjectInsightGroupDetails {

    id: any;
    groupTitle: any;
    groupType: any;
    parentId: any;
    parentType: any;
    formId: any;
    isDraft: any
    questionList: ProjectInsightQuestionDetails[] = [];
    additionalInfo: Map<string, any>;
    createdBy: any;
    createdOn: any;
    updatedBy: any;
    updatedOn: any;
}