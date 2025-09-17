import { ProjectInsightFacetCategory } from "./projectInsightFacetCategory";
import { SurveyOption } from "./sureyOption";

export class ProjectInsightQuestionLibraryEntry {

    id: any;
    question: any;
    description: any;
    optionType: any;
    deptIds: any[] = [];
    depts: any[] = [];
    optionsList: SurveyOption[] = [];
    facetCategoryList?: ProjectInsightFacetCategory[] = [];
    newFacetCategory?: any;
    facetCategoryIds?: any[];
    facetValueIds?: any[];

    createdBy: any;
    createdOn: any;
    updatedBy: any;
    updatedOn: any
}