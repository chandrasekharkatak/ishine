export class getEmployeeTimesheetAsCalenderByProjectId{
	
	projectId : any;
	month : any;
	year : any;
	fromDate?: string;
	toDate?: string;
	empId : any;
	poNo?: string | string[] | null;
	poProjectId?: number | null;
	// billableType : any;
	billableType: string[];
	allEmp: boolean;
	status:any;
	page:any;
	size:any;
	sortDirection:any;
	sortBy:any;
	filters:any;
	projectActive :any;
	employeeActive:any;
	clientSideFilter?:string;
	deptId?:string;
	isEmployeeRepeated: boolean = false;
	multiPOs: any;
	
}
