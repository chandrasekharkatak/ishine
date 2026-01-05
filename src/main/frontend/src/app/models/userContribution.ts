export class UserContribution {
    userContributionId: any;
	empId: any;
	response: any;
	status: any;
	assignTo: any;
	projectId: any;
	userDefinedProjectName: any;
	createdOn: any;
	updatedOn: any;
	onlyText: any;
	title: any;
	teamMembers: any;
	parentContribution: any
	tags: any[] = [];
	processType:any;
	remark:any;
	parentContributionName:any;
	attachments: (File | {name: string, documentId: number, isExisting: boolean})[];
	userDocument:any[] = [];
	responseRemarkList:any[] = [];
	reviewType:any;
}