import { KnowledgeHubSearchResultObject } from "./KnowledgeHubSearchResultObject";
import { KnowledgeHubSearchResultObjectField } from "./KnowledgeHubSearchResultObjectField";
import { ProjectInsightFacetCategoryDTO } from "./projectInsightFacetCategoryDTO";

export class KnowledgeHubSearchResultProject {
    projectId: any;
    projectName: any;
    knowledgeHubSearchResultObjectList: KnowledgeHubSearchResultObject[];
    knowledgeHubSearchResultObjectFields: KnowledgeHubSearchResultObjectField[];
    skip: any;
    limit: any;
    currentObjectPage: number;
    objectPageSize: number;
    totalObjectPageSize: number;
    totalObjectOccurenceCount: number;
    totalProjectOccurenceCount: number;
    totalGroupCount: number;
}

export class KnowledgeHubSearchResultWrapper {
    knowledgeHubSearchResultProjectList: KnowledgeHubSearchResultProject[];
    skip: any;
    limit: any;
    totalCount: any;
    facetCategories: ProjectInsightFacetCategoryDTO[];
}