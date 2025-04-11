import { ProjectResponse } from "./projectResponse";
export class ProjectQuestion{
    questionId:any;
    question:any;
    optionType:any;
    options:any;
    required:any = false;
    description:any;
    response:any;
    responseList:any[]= [];
    documentUpload:any = false;
    name:any;
	employeementId:any;
    userType:any;
    optionsList:any[]= [];
    isCollapsed:any;
    uploadedFile:any;
    uploadedFileName:any;
    entityId:any;
    entityType:any;
    assignedToUserNames:any;
    projectResponseList:ProjectResponse[]=[];
    taggedToUserId: any[] = [];
    taggedToUserNames:any;
    toTagEmployeeList:any[]=[];
    toAssignEmployeeList:any[]=[];
}
