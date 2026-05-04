export class HierarchyUser{
    name:any;
    cssClass:any;
    title:any;
    childs:any[] = []
    empId:any;
	managerId:any;
    id:any;
    nodeUid?: string;
    disabled?: boolean;

    constructor(name?:any,cssClass?:any, title?:any, empId?:any, managerId?:any){
        this.name= name;
        this.cssClass= cssClass;
        this.title= title;
        this.empId=empId;
	    this.managerId=managerId;
    }
}