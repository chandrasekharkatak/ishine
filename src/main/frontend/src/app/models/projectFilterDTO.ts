export class ProjectFilterDTO{
    approvalStatus: any = null;
    completionStatus: any = null;
    currentUserEmpId:any;
    isAdmin:boolean = false;
    isHod:boolean = false;
    isOther:boolean = false;
    departmentsids: any[] = [];
    departments:any[] = [];
  days: any;
  expiredProjectFilter: any;
  type : any;
}