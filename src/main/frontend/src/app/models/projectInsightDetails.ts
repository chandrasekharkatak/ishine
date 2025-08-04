import { ProjectInsightClient } from "./projectInsightClient";
import { ProjectInsightDepartment } from "./projectInsightDepartment";
import { ProjectInsightDomain } from "./projectInsightDomain";

export class ProjectInsightProjectDetails {

    id: any;
    projectName: any;
    projectId: any;
    isDraft: any;
    formId: any;
    client: ProjectInsightClient = new ProjectInsightClient();
    domains: ProjectInsightDomain[] = [];
    departments: ProjectInsightDepartment[] = [];
    additionalInfo: Map<string, any>;
    createdBy: any;
    createdOn: any;
    updatedBy: any;
    updatedOn: any;

}