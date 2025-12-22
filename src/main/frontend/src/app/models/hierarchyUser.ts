export class HierarchyUser{
    name:any;
    cssClass:any;
    title:any;
    childs:any[] = []
    empId:any;
	managerId:any;
    id:any;

    constructor(name?:any,cssClass?:any, title?:any, empId?:any, managerId?:any){
        this.name= name;
        this.cssClass= cssClass;
        this.title= title;
        this.empId=empId;
	    this.managerId=managerId;
    }
}