export class Project{
    projectId:any;
	clientName:any;
	clientLocation:any;
	state:any;
	projectName:any;
	description:any;
	projectManagerId:any;
	departmentName:any;
	clientId:any;
	clientLocationId:any;
	departmentList:any[] = [];
	allClientLocationList;
	deptId:any;
	active:any;
	syncProject:any;
	createdByName: any;
	empId: any;
	createdOn: any;
	createdBy: any;
	updatedBy:any;
	updatedByName: any;
	teamList: any[] = [];
	isHOD:any;
	projectManagerName:any;
	isTeamCreated:any;
	rejectReason:any;       // RMG project reject reason


	//Project from Po
	name:any;
	status:any;
	clientState:any;
	department:any;
	projectManager:any;

	//Team
	teamName:any;
	teamMemberList:any;
}