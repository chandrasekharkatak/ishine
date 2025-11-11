import { ProjectInsightQuestionDetails } from "./projectInsightQuestionDetails";

export class FormNode {
    id: string;
    formName: string;
    fields: any[];
    formData: any;
    layoutConfig?: any[];
    parentId?: any;
    parentType?: any;
    groupList?: any[];
    questionList?: ProjectInsightQuestionDetails[];
    fieldDependencies?: { [key: string]: string };
    dependentFieldsMap?: { [key: string]: string[] };
}