import { ProjectResponseOption } from "./projectResponsOption";

export class ProjectResponse {
    responseByEmpName: any;
    responseByEmpId: any;
    responseId: any;
    response: any;
    responseList: any;
    entityId: any;
    entityType: any;
    uploadedFile: any;
    uploadedFileName: any;
    documentPath: any;
    option:ProjectResponseOption = new ProjectResponseOption();
    optionsList:any[]= [];
    options:any;
    isDraft:any;

}