import { ProjectResponsePoint } from "./projectResponsePoint";
import { ProjectResponseOption } from "./projectResponsOption";
export class ProjectResponse {
    projectInsightResponseId: any;
    projectInsightResponseMetadataId: any;
    questionMasterId: any;
    options: any;
    optionsList: any[] = [];
    option: ProjectResponseOption = new ProjectResponseOption();
    response: any;
    responseList: any;
    responseType: any;
    responseBy: any;
    responseByEmpName: any;
    document: any;
    documentFileName: any;
    documentPath: any;
    showDocDiv: any = true;
    isDraft: any = 'Y';
    projectInsightResponsePointList: ProjectResponsePoint[] = [];
    isRecommendedChecked: any;
    approvedForKnowledgeHub: any;
    tags: any[] = [];
    newTag: any;
}