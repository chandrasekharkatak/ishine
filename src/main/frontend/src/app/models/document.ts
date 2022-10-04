export class Document{
    employeeDocumentId:any;	
	empId:any;	
	employeementId:any;	
	documentName:any; //Uploaded Image name
	documentType:any;

    uploadStatus:any = "Pending";
    documentBytes:any;

    constructor(documentType?:string){
        this.documentType = documentType;
    }
}