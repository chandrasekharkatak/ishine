import { ProjectInsightProjectDetails } from "./projectInsightDetails";
import { ProjectInsightFormDetails } from "./projectInsightFormDetails";
import { ProjectInsightGroupDetails } from "./projectInsightGroupDetails";
import { ProjectInsightQuestionDetails } from "./projectInsightQuestionDetails";

export class ProjectInsightDetailsDTO {

    projectInsightProjectDetails: ProjectInsightProjectDetails = new ProjectInsightProjectDetails();
    projectInsightGroupDetails: ProjectInsightGroupDetails = new ProjectInsightGroupDetails();
    projectInsightQuestionDetails: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();
    projectInsightFormDetails: ProjectInsightFormDetails = new ProjectInsightFormDetails();
}