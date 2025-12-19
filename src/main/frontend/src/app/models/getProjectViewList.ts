export class getProjectViewList{



  projectId : any;
	month : any;
     month1:any;
	year : any;
	empId : any;
	// billableType : any;
	billableTypes: string[];
    // billableTypes: string[] = ['All', 'TNM', 'Fixed Cost', 'Shadow', 'InternalRNDProducts'];
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
    dataForExcel :boolean;
    isClientDashboard :boolean;
    columnFilter :any;
}