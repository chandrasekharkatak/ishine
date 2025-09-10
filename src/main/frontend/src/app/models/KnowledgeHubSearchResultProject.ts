import { KnowledgeHubSearchResultObject } from "./KnowledgeHubSearchResultObject";
import { KnowledgeHubSearchResultObjectField } from "./KnowledgeHubSearchResultObjectField";

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
}