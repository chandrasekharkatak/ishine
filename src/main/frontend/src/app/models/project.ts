import { ResourceRequirement } from "./resourceRequirement";

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
	isDraftProject:any;
	bulkSyncList: any[] = [];
	id:any;
	teamsApprovedList:any;
	//Project from Po
	name:any;
	status:any;
	clientState:any;
	department:any;
	projectManager:any;
	poProjectId:any;

	//Team
	teamName:any;
	teamMemberList:any;
	isActive:any;
	teamId:any;
	spoc:any;

	// As per RMG request
	resourceRequirements: ResourceRequirement[];
	oldresourceRequirements: ResourceRequirement[];


	endDate: any;
	isAllProj: any;

	isSynced: any;
	poNo: any;
	startDate:any;
	poStartDate:any;
	poEndDate:any;
	projectType:any;
	poProjectType:any;
	isMail:any;
	apmosysRM:any;
	clientRM:any;
    projectCompletionDate:any;
	projectStatus:any;
	projectManagers:any;
	projectViewId:any;
	projectOverheadId:any;
	projectOverheadName:any;
	internalProjectType:any;
	teamLeadId:any;
	teamLead:any;
	employeeTeamMapId:any;
	draftStatus:any;
  combinedProjectType: string;
  extendedDate: any; // Added for milestone extension
  page:any;
  size:any;
  isClientDashboard:any;
  rescRemovedBy:any;	
}