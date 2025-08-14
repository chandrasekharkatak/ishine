import { SurveyOption } from "./sureyOption";

export class ProjectInsightQuestionLibraryEntry {

    id: any;
    question: any;
    description: any;
    optionType: any;
    deptIds: any[] = [];
    depts: any[] = [];
    optionsList: SurveyOption[] = [];

    createdBy: any;
    createdOn: any;
    updatedBy: any;
    updatedOn: any
}