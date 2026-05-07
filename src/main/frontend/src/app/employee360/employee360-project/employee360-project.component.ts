import { Component, OnInit, TemplateRef, viewChild, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { saveAs } from 'file-saver';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { DefaultProjectUpdate } from 'src/app/models/defaultProjectUpdate';
import { EmployeeInformation } from 'src/app/models/employeeInformation';
import { GetProjectDetailsForBulkDefaultUpdate } from 'src/app/models/getProjectDetailsForBulkDefaultUpdate';
import { Project } from 'src/app/models/project';
import { SetDefaultProjectObj } from 'src/app/models/setDefaultProjectObj';
import { TeamMember } from 'src/app/models/teamMember';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { EncryptionService } from 'src/app/services/EncryptionService';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { ResourceManagementComponent } from 'src/app/user-team/resource-management/resource-management.component';
import * as XLSX from 'xlsx';
import { firstValueFrom } from 'rxjs';
import { RmgTeamMember } from 'src/app/models/rmgTeamMember';
import { TeamService } from 'src/app/services/team.service';
import { EmployeeProjectTimesheetDto } from 'src/app/models/employeeProjectTimesheetDto';
import { RmgProjectConfigComponent } from 'src/app/user-team/resource-management/rmg-project-config/rmg-project-config.component';
import { EmployeeProjectService } from 'src/app/services/employee-project.service';
import { AppModalService } from 'src/app/user-team/resource-management/app-modal.service';
import { GlobalRightDrawerService } from 'src/app/services/global-right-drawer.service';
import { ValidationService } from 'src/app/services/validation.service';
import { EmployeeOtherActiveProject } from 'src/app/models/employeeOtherActiveProject';
import { ToastService } from 'src/app/services/toast.service';
import { PoDetails } from 'src/app/models/poDetails';
import { RmgResourceRequirement } from 'src/app/models/rmgResourceRequirement';
import { RmgTeam } from 'src/app/models/rmgTeam';
import { MomentDateAdapter, MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';

export const MY_DATE_FORMATS = {
    parse: {
        dateInput: 'DD-MM-YYYY',
    },
    display: {
        dateInput: 'DD-MM-YYYY',
        monthYearLabel: 'MMM YYYY',
        dateA11yLabel: 'DD-MM-YYYY',
        monthYearA11yLabel: 'MMMM YYYY',
    },
};

@Component({
  standalone: false,
  selector: 'app-employee360-project',
  templateUrl: './employee360-project.component.html',
  styleUrls: ['./employee360-project.component.css'],
  providers: [
      { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_MOMENT_DATE_ADAPTER_OPTIONS] },
      { provide: MAT_DATE_FORMATS, useValue: MY_DATE_FORMATS },
  ]
})
export class Employee360ProjectComponent implements OnInit {

  @ViewChild("alert_message") alertTemplate: TemplateRef<any>;
  @ViewChild("update_project_start_date_error") updateProjectStartDateErrorTemplateRef: TemplateRef<any>;
  @ViewChild("update_project_start_date_confirmation") updateProjStartDateModal :TemplateRef<any>;
  @ViewChild(ResourceManagementComponent) resourceManagementComponent: ResourceManagementComponent;
  @ViewChild(RmgProjectConfigComponent) rmgProjectConfigComponent: RmgProjectConfigComponent;
  @ViewChild("remove_members_confirmation") removeMembersConfirmationTemplateRef!: TemplateRef<any>;
  @ViewChild("mark_default_project_completion") markDefaultProjectCompletionTemplateRef!: TemplateRef<any>;
  @ViewChild("mapping_other_project_as_default") mappingToOtherProjectAsDefaultTemplateRef!: TemplateRef<any>;

  modalRef: NgbModalRef;
  errModalRef: NgbModalRef;
  alertMessageModalRef: NgbModalRef;
  updateProjectStartDateErrorModalRef: NgbModalRef;
  updateProjectStartDateConfirmationModalRef: NgbModalRef;
  existingEmployeeProjectTimesheetInfoModalRef: NgbModalRef;
  projectGapMessageModalRef: NgbModalRef;
  removeMembersConfirmationModalRef!: NgbModalRef;
  markDefaultProjectCompletionModalRef!: NgbModalRef;
  mappingToOtherProjectAsDefaultModalRef!: NgbModalRef;

  isEditProject: boolean = false;
  isHideButton: boolean = false;

  //added by rahul
  flag: boolean = false;
  isProjectVisible: boolean = true;
  isProjectTeamVisible: boolean = false;
  isProjectTeamMemberVisible: boolean = false;
  projectListBulk: GetProjectDetailsForBulkDefaultUpdate = new GetProjectDetailsForBulkDefaultUpdate();
  employeeData: any;
  getBillableType: any;
  newMemberInProject: any;
  lastDate: any;
  startDate: any;
  endDate: any;
  page = 1;
  copyDepartment: any = [];
  currentBreadcrumbList: any[] = [];
  allProjectList: any[] = [];
  //Rahul Singh
  filterProjectByProjectId: any[] = [];
  filterTeamfromTeamId: any;
  //end
  filteredDeptList: any[] = [];
  allDeptList: any[] = [];
  allTeamList: any[] = [];
  previewTeamList: any[] = [];
  currentUser: User;
  filters: any = {};
  isSearchEnabled: boolean = false;
  projectColumns: any[] = ['blank', 'projectName', 'teamName', 'clientName', 'billableType', 'combinedProjectType','poNoSearch','startDateSearch', 'endDateSearch','rescRemovedByName' ,'projectStartDateSearch', 'projectEndDateSearch', 'status'];
  employeesColumns: any[] = ['blank', 'teamName', 'employeeName', 'billableType', 'startDate', 'employeeRole'];
  teamColumns: any[] = ['blank','employmentIdAcToET','name','teamName','teamLeadName']
  alertMessage: any;
  newteamMember: TeamMember = new TeamMember();
  projectNewStartDate: any
  projectObj: Project = new Project();
  projectEditObj: Project = new Project();
  selectedOtherProjectId: any;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  abbreviationError: string = '';

  projectType: string = '';
  employeeProjectEndDateType: 'PO' | 'Custom' = 'Custom';
  newProjectObj: Project = new Project();
  fixedCostTypes = ['fixed cost'];
  allBillableProjectTypes = ['tnm', 'fixed cost', 'monitoring'];
  allNonBillableProjectTypes = ['internalrndproducts', 'bench', 'internal'];
  employeeRoles: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  endDateUpdateAllowedRoles: any[] = ['hod', 'superadmin', 'super admin'];
  projectTypes: any[] = ['Bench', 'Other'];
  projectPage = 1;
  teamPage = 1;
  teamMemberPage = 1;
  defaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
  removePermanently: boolean = false;
  membersEndDate: any;
  membersEndDateType: 'PO' | 'Custom' = 'Custom';
  defaultProjectMappingActionType: 'DELETE_TEAM' | 'REMOVE_MEMBERS' | 'PROJECT_COMPLETION' | 'DEFAULT_REMOVE' = 'REMOVE_MEMBERS';
  projectList: any[] = [];
  projectsBench: any[] = [];
  projectsOther: any[] = [];
  selectedRemoveMembers: RmgTeamMember[] = [];
  mappingToOtherProjectAsDefaultList: RmgTeamMember[] = [];
  markDefaultProjectCompletionList: RmgTeamMember[] = [];
  projectIdPoListMap = new Map<number, PoDetails[]>();
  poIdTeamListMap = new Map<number, RmgTeam[]>();
  teamIdResourceReqListMap = new Map<number, RmgResourceRequirement[]>();

  //  Change Employee Default Project Mapping
  isChangeEmployeeDefaultProjectMappingSearchEnabled: boolean = false;
  changeEmployeeDefaultProjectMappingPage = 1;
  changeEmployeeDefaultProjectMappingPageSize = 10;
  changeEmployeeDefaultProjectMappingSortColumn: string = '';
  changeEmployeeDefaultProjectMappingSortColumnType: string = '';
  changeEmployeeDefaultProjectMappingSortDirection: string = 'asc';
  changeEmployeeDefaultProjectMappingFilters: any = {};
  changeEmployeeDefaultProjectMappingSearchOnEnter: boolean = true;
  changeEmployeeDefaultProjectMappingColumnList: any[] = ['employementId', 'memberName', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank'];

  // Mark Default Project Completion Individual
  isMarkDefaultProjectCompletionIndividualSearchEnabled: boolean = false;
  markDefaultProjectCompletionIndividualPage = 1;
  markDefaultProjectCompletionIndividualPageSize = 10;
  markDefaultProjectCompletionIndividualSortColumn: string = '';
  markDefaultProjectCompletionIndividualSortColumnType: string = '';
  markDefaultProjectCompletionIndividualSortDirection: string = 'asc';
  markDefaultProjectCompletionIndividualFilters: any = {};
  markDefaultProjectCompletionIndividualSearchOnEnter: boolean = true;
  markDefaultProjectCompletionIndividualColumnList: any[] = ['employementId', 'memberName', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank'];


  constructor(
    private breadcrumbService: BreadcrumbService,
    private projectService: ProjectService,
    private modalService: NgbModal,
    private router: Router,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private departmentService: DepartmentService,
    private resourceManagementService: ResourceManagementService,
    private employeeService: EmployeeService,
    private emp360Service: Employee360Service,
    private encryptionService: EncryptionService,
    private teamService: TeamService,
    private employeeProjectService : EmployeeProjectService,
    private appModalService : AppModalService,
    private drawerService: GlobalRightDrawerService,
    public validationService: ValidationService,
    private readonly toastService: ToastService,

  ) {

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    const navigation = this.router.getCurrentNavigation();
    this.employeeData = navigation?.extras.state?.['employeeData'];
  }

  ngOnInit(): void {
    console.log("Current User", this.currentUser)
    let encryptedEmployeeData = sessionStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
  const storedData = employeeData;
    const parsedData = storedData ? storedData : null;
    if (parsedData != null || parsedData != undefined) {
      this.employeeData = parsedData;
    } else {
      this.employeeData = history.state.data;
    }

    this.appModalService.rmgAction$.subscribe(action => {
      if (action.actionType === 'PROJECT_START_DATE_UPDATED') {
        this.cancelRequest();
        this.getExistingProjectsByUser();
      }
    });

    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Project");
    if (findbreadcrumbObject >= 0) {
      this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
      this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
    } else {
      let breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = "Project";
      breadcrumbObject.url = "/employee-360/project";
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }
    this.getExistingProjectsByUser();
    this.getActiveProjectList();
  }
  clearBreadcrumbs() {
    // this.breadcrumbService.setBreadcrumbSubject(null);
    window.location.reload()
  }
  //added by rahul singh
  backfromvisibility(type: any) {
    this.isProjectVisible = false;
    this.isProjectTeamVisible = false;
    this.isProjectTeamMemberVisible = false;
    this.getExistingProjectsByUser();
    if (type == "ProjectVisible" && this.flag == true) {
      this.isProjectVisible = true;
      this.flag = false;
      this.filterProjectByProjectId = [];

    }
    else if (type == "ProjectTeamVisible") {
      this.isProjectTeamVisible = true;
    }
    else if (type == "ProjectTeamMemberVisible" && this.flag == false) {
      this.isProjectVisible = true;
      this.isProjectTeamVisible = false;
    } else {
      this.isProjectTeamVisible = true;
      this.isProjectVisible = false;
    }

  }
    redirecttoProjectTeam(projectId: any) {
      this.isProjectVisible = false;
      this.isProjectTeamVisible = true;
      this.isProjectTeamMemberVisible = false;
      this.flag = true;
      this.page = 1;
      this.getTeamByProjectId(projectId);
      //this.filterProjects(projectId);
    }
    redirecttoTeam(id: any, projectId: any) {
      this.isProjectTeamMemberVisible = true;
      this.page = 1;
      this.isProjectVisible = false;
      this.isProjectTeamVisible = false;
      this.getTeamEmployeeByTeamId(id);
      //this.filterProjects(projectId);


  }

  projectteamInfo: Project = new Project();

  // getTeamByProjectId(projectId: any) {

  //   console.log("Clicked Project ID 👉", projectId);
  //   this.projectteamInfo.projectId = projectId;

  //   this.emp360Service.getTeamInfo(this.projectteamInfo.projectId).subscribe({
  //     next: (response: any) => {
  //       if (response.serviceStatus === "Success") {
  //         this.filterProjectByProjectId = response.serviceResponse;
  //          console.log("getTeamInfo ", this.filterProjectByProjectId);

  //         const groupedData = {};

  //         this.filterProjectByProjectId.forEach((member) => {
  //           const teamKey = member.teamId;

  //           if (!groupedData[teamKey]) {
  //             groupedData[teamKey] = {
  //               teamId: member.teamId,
  //               teamName: member.teamName,
  //               employees: [],
  //               projectId: member.projectId,
  //               projectName: member.projectName
  //             };
  //           }

  //           groupedData[teamKey].employees.push({
  //             empId: member.empId,
  //             employeeName: member.employeeName,
  //             employeeRole: member.employeeRole ? member.employeeRole.split(',').filter(role => role.trim() !== '').join(', ') : "",
  //             startDate: member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null,
  //             billableType: member.billableType,
  //             active: member.active,
  //             employeeTeamMapId:member.employeeTeamMapId,
  //             emp360: {}
  //           });

  //           groupedData[teamKey].employees = groupedData[teamKey].employees || [];
  //         });

  //         this.filterProjectByProjectId = Object.values(groupedData);

  //         this.filterProjectByProjectId.forEach((team) => {
  //           team.employees.forEach((employee) => {
  //             employee.emp360 = employee.empId;
  //           });
  //         });
  //         // console.log("Formatted Team Data: ", this.teamMemberList);
  //       } else {
  //         console.warn("Failed to fetch team info");
  //       }
  //     }
  //   });

  // }

  getTeamByProjectId(projectId: any) {
  this.emp360Service.getTeamInfo(projectId).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === "Success") {

        const projectData = response.serviceResponse;

        this.filterProjectByProjectId = projectData.teamDetails.map(team => ({
          teamId: team.teamId,
          teamName: team.teamName,
          spoc: team.spoc,
          projectId: projectData.projectId,
          projectName: projectData.projectName,
          employees: team.teamMemberDetails.map(member => ({
            empId: member.empId,
            employeeName: member.employeeName,
            employeeRole: member.employeeRole,
            billableType: member.billableType,
            startDate: member.startDate,
            active: member.active,
            employeeTeamMapId: member.employeeTeamMapId,
            emp360: member.empId
          }))
        }));

        console.log(" Final Data:", this.filterProjectByProjectId);

      }
    }
  });
}

  filterProjects(id) {
    this.getTeamByProjectId(id);

  }
  filterTeamMemberProjects(id) {
    this.filterProjectByProjectId = this.allProjectList.filter(project =>
      project.projectId == id
    );
  }

    refresh() {
    window.location.reload();
  }

flattenProjectData(data: any[]): any[] {
  const flatList: any[] = [];

  data.forEach(parent => {
    flatList.push(parent);

    parent.history?.forEach(child => {
      flatList.push(child);
    });
  });

  return flatList;
}

  exportToExcel(id: any): void {


    let exportToExcelTeamfile = id + ".xlsx";
    const table = document.getElementById('' + id); // Get table by ID
    if (!table) {
      console.error('Table not found');
      return;
    }

    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(table); // Convert table to worksheet
    
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Project Data');

    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });

    saveAs(data, exportToExcelTeamfile);

  }


  // added by Soumyakant
exportFullDataToExcel(): void {

  let exportData: any[] = [];
  this.allProjectList.forEach((project, index) => {

    exportData.push({
      'Sr No.': index + 1,
      'Project Name': project.projectName,
      'Team Name': project.teamName,
      'Client Name': project.clientName,
      'Billable Type': project.billableType,
      'Project Type': project.combinedProjectType,
      'PO No': project.poNo || '',
      'Start Date': project.startDate,
      'End Date': project.endDate,
      'Removed By': project.rescRemovedByName || '',
      'PO Start Date': project.projectStartDate,
      'PO End Date': project.projectEndDate,
      'Project Status': project.status,
      'Type': 'Parent'
    });


    (project.history || []).forEach(h => {
      exportData.push({
        'Sr No.': '',
        'Project Name': h.projectName,
        'Team Name': h.teamName,
        'Client Name': h.clientName,
        'Billable Type': h.billableType,
        'Project Type': h.combinedProjectType,
        'PO No': h.poNo || '',
        'Start Date': h.startDate,
        'End Date': h.endDate,
        'Removed By': h.rescRemovedByName || '',
        'PO Start Date': h.projectStartDate,
        'PO End Date': h.projectEndDate,
        'Project Status': h.status,
        'Type': 'History'
      });
    });

  });

  const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exportData);
  const workbook: XLSX.WorkBook = XLSX.utils.book_new();

  XLSX.utils.book_append_sheet(workbook, worksheet, 'Project Data');

  const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });

  saveAs(data, 'Full_Project_Data.xlsx');
}



  async getTeamEmployeeByTeamId(teamId: any) {
    try {
      const response: any = await this.projectService.getTeamMemberByTeamId(teamId).pipe(first()).toPromise();

      if (response.serviceStatus === 'Success') {
        this.filterTeamfromTeamId = response.serviceResponse;

        this.filterTeamfromTeamId.forEach((employee) => {
          employee.emp360 = employee.empId;
          employee.emp360teamLeadId = employee.teamLeadId;
        });
        // console.log('filterTeamfromTeamId = ',this.filterTeamfromTeamId);

      } else {
        // Handle failure case, if needed
        console.log('Service failed:', response);
      }
    } catch (error) {
      // Handle error case
      console.error('Error fetching team members:', error);
    }
  }
  //end

  showEditProjectForm(project: any) {
    this.isEditProject = true;
    this.isHideButton = true;

    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
  }



getProjectType(project: any): string {
  if (project.poProjectType !== null && project.poProjectType !== undefined && project.poProjectType !== '') {
    return project.poProjectType;
  } else if (project.internalProjectType !== null && project.internalProjectType !== undefined && project.internalProjectType !== '') {
    return project.internalProjectType;
  } else {
    return 'NA';
  }
}

// async getExistingProjectsByUser() {
//   let projectObj = new Project();
//   projectObj.empId = this.employeeData.empId;
//   projectObj.isAllProj = true;
//   this.allProjectList = [];
//   // getExistingProjectsAndTeamsByEmployee service impl
//   this.projectService.getExistingProjectsAndTeamsByEmployee(projectObj).pipe(first()).subscribe((response: any) => {
//     if (response.serviceStatus == "Success") {
//       this.allProjectList = response.serviceResponse;
//       // Add combined project type to each project in the list
//       this.allProjectList = this.allProjectList.map((project: any) => {
//         project.combinedProjectType = this.getProjectType(project);
//         return project;
//       });
//     }
//   });
// }

//Updated by Soumyakant
async getExistingProjectsByUser() {
  let projectObj = new Project();
  projectObj.empId = this.employeeData.empId;
  projectObj.isAllProj = true;
  this.allProjectList = [];

  this.projectService.getExistingProjectsAndTeamsByEmployee(projectObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      let rawData = response.serviceResponse || [];


     rawData.forEach((p: any) => {
  p.combinedProjectType = this.getProjectType(p);
  p.startDateSearch = this.formatDateForSearch(p.startDate);
  p.endDateSearch = this.formatDateForSearch(p.endDate);
  p.projectStartDateSearch = this.formatDateForSearch(p.projectStartDate);
  p.projectEndDateSearch = this.formatDateForSearch(p.projectEndDate);
});

      rawData.sort((a: any, b: any) => {
        const dateA = new Date(a.startDate).getTime();
        const dateB = new Date(b.startDate).getTime();
        return dateB - dateA;
      });

      const groupedMap = new Map();

      rawData.forEach((item: any) => {

        const key = `${item.projectName?.trim()}|${item.teamName?.trim()}`;
        item.isExpanded = false;

       if (!groupedMap.has(key)) {

  item.history = [];

  item.poNoSearch = item.poNo ? item.poNo.toString() : '';
   item.startDateSearch = item.startDateSearch || '';
   item.endDateSearch = item.endDateSearch || '';
   item.projectStartDateSearch = item.projectStartDateSearch || '';
   item.projectEndDateSearch = item.projectEndDateSearch || '';

  groupedMap.set(key, item);

} else {

  const parent = groupedMap.get(key);
  parent.history.push(item);

  if (item.poNo) {
    parent.poNoSearch += ' ' + item.poNo.toString();
     parent.startDateSearch += ' ' + (item.startDateSearch || '');
    parent.endDateSearch += ' ' + (item.endDateSearch || '');
    parent.projectStartDateSearch += ' ' + (item.projectStartDateSearch || '');
    parent.projectEndDateSearch += ' ' + (item.projectEndDateSearch || '');
  }
}
      });

      this.allProjectList = Array.from(groupedMap.values());
    }
  });
}

//added by Soumyakant
formatDateForSearch(date: any): string {
  if (!date) return '';

  const d = new Date(date);

  const ddmmyyyy = `${('0' + d.getDate()).slice(-2)}-${('0' + (d.getMonth()+1)).slice(-2)}-${d.getFullYear()}`;
  const yyyymmdd = `${d.getFullYear()}-${('0' + (d.getMonth()+1)).slice(-2)}-${('0' + d.getDate()).slice(-2)}`;

  return ddmmyyyy + ' ' + yyyymmdd;
}

formatToLocalDateTime(date: any): string {
  const d = new Date(date);

  const pad = (n: number) => n.toString().padStart(2, '0');

  return d.getFullYear() + '-' +
    pad(d.getMonth() + 1) + '-' +
    pad(d.getDate()) + 'T' +
    pad(d.getHours()) + ':' +
    pad(d.getMinutes()) + ':' +
    pad(d.getSeconds());
}


  deleteResourceModal1(template: TemplateRef<any>, projObj, member) {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    this.lastDate = `${yyyy}-${mm}-${dd}`;
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
    this.projectObj = projObj;
    this.projectObj.empId = member.empId;

  }



  deleteResourceFromProject(template: TemplateRef<any>) {
    this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.lastDate;
    projectObj.employeeTeamMapId = this.employeeTeamMapId;
    projectObj.rescRemovedBy = this.currentUser.empId;

    console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.lastDate = '';
        this.lastDate = '';
        this.getExistingProjectsByUser();
        this.getTeamByProjectId(this.projectObj.projectId);
        this.getTeamByProjectId(this.projectObj.projectId);
      }
    })
  }

  editProjectInfo(projectObj: any) {
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project Edit - " + projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: {} });

    this.resourceManagementComponent.showEditProjectForm(projectObj);



    this.resourceManagementComponent.showEditProjectForm(projectObj);


  }

  viewProjectInfo(projectObj: any) {
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project View - " + projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    this.router.navigate([breadcrumbObject.url], { queryParams: {} });
  }

  getAllDepartmentList(project: any) {
    // this.allDeptList = [];

    // this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
    //   if (response.serviceStatus == "Success") {
    //     this.allDeptList = response.serviceResponse;
    //     this.filteredDeptList = this.allDeptList.filter(x => project.department.includes(x.name));
    //     //console.log("allDeptList : ", this.allDeptList)
    //   } else {
    //     console.error(response.serviceResponse);
    //   }
    // });
  }

  getTeamListByProjectName(project: any) {
    //   // this.previewTeamList = [];
    //   //console.log(" project    ",project);

    //   this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
    //     if (response.serviceStatus == "Success") {
    //       this.projectObj.teamList = response.serviceResponse;
    //       //console.log(this.projectObj.teamList, " this.projectObj.teamList");
    //       this.projectObj.teamList.forEach((obj) => {
    //         obj.departmentList = obj.departmentList?.map(x => +x);
    //         console.log(" obj.departmentList     ",obj.departmentList);
    //         this.copyDepartment = obj.departmentList;

    //         if (obj.teamMemberList) {
    //           obj.teamMemberList.forEach((member) => {
    //             if (member) { // Check if member is not null
    //               // Format the startDate if it exists, otherwise set it to null
    //               member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
    //             }
    //           });
    //         } else {
    //           console.warn('teamMemberList is null or undefined');
    //         }
    //       });
    //       //console.log(this.projectObj.teamList, " this.projectObj.teamList");
    //       this.previewTeamList = this.projectObj.teamList;

    //       //console.log(" length of previewTeamList  ",this.previewTeamList.length);
    //       //Project Team List
    //       if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
    //         this.addInputTeamField();
    //       } else {
    //         //console.log(" find error in else part ")
    //         this.allTeamList = this.projectObj.teamList;
    //         // this.allTeamListCopy = this.projectObj.teamList;
    //         this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
    //       }

    //     } else {
    //       console.error(response.serviceResponse);

    //       //Project Team List
    //       if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
    //         this.addInputTeamField();
    //       } else {
    //         this.allTeamList = this.projectObj.teamList;
    //         this.allTeamListCopy = JSON.parse(JSON.stringify(this.projectObj.teamList));
    //       }
    //     }
    //   });
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }


  handlePageChange(event) {
    this.page = event;
  }

  // handlePageChange(event, type: string) {
  //   if (type === 'project') this.projectPage = event;
  //   if (type === 'team') this.teamPage = event;
  //   if (type === 'teamMember') this.teamMemberPage = event;
  // }

  cancelRequest() {
    this.modalRef?.close();
    if (this.alertMessageModalRef) {
			this.alertMessageModalRef?.close();
		}
  }

  editStartdateModal(template: TemplateRef<any>, projObj) {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    this.startDate = null;
    this.lastDate = `${yyyy}-${mm}-${dd}`;
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
    this.projectObj = projObj;
  }

  editEnddateModal(template: TemplateRef<any>, projObj) {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    this.endDate = null;
    this.lastDate = `${yyyy}-${mm}-${dd}`;
    // let projectObj = Object.assign({},this.projectObj); for copy object
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
    this.projectObj = projObj;
  }


  async editStartdate(template: TemplateRef<any>, edit_enddate_template: TemplateRef<any> ) {
    this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.startDate = this.formatToLocalDateTime(this.startDate);

    if (this.isBefore(this.projectObj.projectStartDate, projectObj.startDate)) {
      this.errModalRef = this.modalService.open(this.updateProjectStartDateErrorModalRef, { modalDialogClass: 'modal-md' });
      return;
    }

    const flag: boolean = await this.validateEmployeeProjectStartDate(this.projectObj.empId, this.projectObj.projectId, '', this.projectObj.teamId, edit_enddate_template);
    if (!flag) {
      return;
    }

    projectObj.updatedBy = this.currentUser.empId;
    this.projectService.updateProjectStartAndEndDate(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.startDate = '';
        this.getExistingProjectsByUser();
        this.getTeamByProjectId(this.projectObj.projectId);
        // this.getTeamByProjectId(this.projectObj.projectId);
      }
    })
  }

  isBefore(startDate,newStartDate){
    const startDate1=new Date(startDate).getTime()
    const newStartDate1=new Date(newStartDate).getTime()
    return newStartDate1<startDate1;

  }


  editEnddate(template: TemplateRef<any>) {

      const startDateStr = this.projectObj.startDate?.split('T')[0];
  const endDateStr = this.endDate;

  const startDate = new Date(startDateStr + 'T00:00:00');
  const endDate = new Date(endDateStr + 'T00:00:00');

  if (endDate < startDate) {
    this.openAlertMod(template, "End Date can't be set before Start Date");
    return;
  }
    this.cancelRequest();
    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.formatToLocalDateTime(this.endDate);
    projectObj.employeeTeamMapId = this.projectObj.employeeTeamMapId; //added for updating end-date


    console.log("team details ", projectObj)
    this.projectService.updateProjectStartAndEndDate(projectObj).pipe(first()).subscribe((response: any) => {

      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.endDate = '';
        this.getExistingProjectsByUser();
        this.getTeamByProjectId(this.projectObj.projectId);
      }
      else {
        if (response.serviceStatus === "Fail") {
          this.openAlertMod(template, "End Date can't be set before Start Date");
        }
      }
      this.endDate = ''; // for clearing the selected date in date picker
    });
  }

  isFutureDate(dateString: string | Date): boolean {
    if (!dateString) return false;
    const today = new Date();
    const inputDate = new Date(dateString);
    // Set time to 0:00:00 to compare only by date (optional)
    today.setHours(0, 0, 0, 0);
    inputDate.setHours(0, 0, 0, 0);
    return inputDate > today;
  }
  modalRef2:NgbModalRef;
  modalRef3:NgbModalRef;
  activeProjects: any;
  EmployessIds: any;
  employeeTeamMapId:any;
  setDefaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();

  deleteResourceModal(template: TemplateRef<any>, template1: TemplateRef<any>, template2: TemplateRef<any>, projObj, teamId) {
    console.log("test", projObj);
    const empIds: number[] = [projObj.empId];
    this.EmployessIds = empIds;
    this.projectteamInfo.projectId = projObj.projectId;

   console.log("test", projObj.empId);
    this.resourceManagementService.getTeamListByProjectName(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        console.log(this.projectObj, "projectofthisteam");

        this.projectObj.teamList.forEach((obj) => {
          obj.departmentList = obj.departmentList?.map(x => +x);
          this.copyDepartment = obj.departmentList;

          if (obj.teamMemberList) {
            obj.teamMemberList.forEach((member) => {
              if (member) { // Check if member is not null
                // Format the startDate if it exists, otherwise set it to null
                member.startDate = member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null;
                member.emp360 = member.empId;
                // member.shadowControl = new FormControl(member.shadow || null);
              }
            });
          } else {
            console.warn('teamMemberList is null or undefined');
          }
        });

        this.previewTeamList = this.projectObj.teamList;

        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {

        } else {
          this.allTeamList = this.projectObj.teamList;

          this.allTeamList = this.allTeamList.filter(team => team.teamId === teamId);


          const empIds: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
            const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];
const targetMember = members.find(member => member.empId === projObj.empId);

this.employeeTeamMapId = targetMember ? targetMember.employeeTeamMapId : null;
console.log("mapping ID",this.employeeTeamMapId);
            members.forEach(member => {
              if (
                Array.isArray(member.otherActiveProjects) &&
                member.otherActiveProjects.length === 0 &&
                member.isDefaultProject === 1
              ) {
                acc.push(member.empId);
              }
            });

            return acc;
          }, []);

          const empIdsHavingActiveProjects: number[] = this.allTeamList.reduce((acc: number[], team: any) => {
            const members = Array.isArray(team.teamMemberList) ? team.teamMemberList : [];

            members.forEach(member => {
              if (
                Array.isArray(member.otherActiveProjects) &&
                member.otherActiveProjects.length !== 0 &&
                member.isDefaultProject === 1
              ) {
                acc.push(member.empId);
              }
            });

            return acc;
          }, []);
          this.activeProjects = empIdsHavingActiveProjects;
          this.EmployessIds = empIds;
          this.EmployessIds = this.EmployessIds.filter(id => id === projObj.empId);
          this.activeProjects = this.activeProjects.filter(id => id === projObj.empId);
          console.log("test",this.activeProjects,this.EmployessIds);
          if (this.activeProjects.length !== 0) {
            this.setDefaultProjectObj.empIds = this.activeProjects;
            this.setDefaultProjectObj.projectId = projObj.projectId;
            this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
            this.modalRef2 = this.modalService.open(template1, { modalDialogClass: 'modal-xl' });

          } else if (this.EmployessIds.length !== 0) {
            this.getEmployeeInformationBulk(this.EmployessIds);
            this.modalRef3 = this.modalService.open(template, { modalDialogClass: 'modal-xl' });
          }
          else {
            this.modalRef = this.modalService.open(template2, { modalDialogClass: 'modal-sm' });
          }

        }

      } else {
        console.error(response.serviceResponse);

      }
    });

    this.projectObj = projObj;
    console.log("test",this.projectObj);

    this.lastDate = projObj.poEndDate
      ? moment(projObj.poEndDate).format('YYYY-MM-DD')
      : moment().format('YYYY-MM-DD');

    if (this.modalRef) this.modalRef?.close();
    if (this.modalRef2) this.modalRef2.close();


    this.getProjectDetailsForBulkDefaultUpdate();

  }

  getEmployeeInformationBulk(empIds) {
    this.resourceManagementService.getEmployeeInformationBulk(empIds).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeList = response.serviceResponse;
        console.error("Unable to fetch Employee List!", this.bulkEmployeeList);
      } else {
           if (this.EmployessIds.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      }
    });
  }


  bulkEmployeeList: EmployeeInformation[] = [];

  @ViewChild("alert_message_without_reload")
  alertTemplateWithoutReload: TemplateRef<any>;

  openAlertMod3(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  bulkEmployeeListActiveList: any[] = [];
  getEmployeeInformationForDefaultProject(setDefaultProjectObj: any) {

    this.resourceManagementService.getEmployeeInformationForDefaultProject(setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeListActiveList = response.serviceResponse;
        console.error("Unable to fetch Employee List!", this.bulkEmployeeListActiveList);
        // this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });

       } else {
            if (this.activeProjects.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
       }
    });
  }
  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  searchTerm: any
  searchTermTeam: any;
  resourceRequirementListBulk: any;
  defaultProjectUpdate: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredOtherProjectList: any[] = [];
  otherProjectList: any[] = [];
  filteredTeamsForDefaultBulk: any;
  isBulkDelete: boolean = false;
  isBulkUpdateMode: boolean = true;
  filteredProjectsForDefaultBulkBench: any;
  defaultProjectUpdateBulk: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredProjectsForDefaultBulkOther: any;
  bulkProjectType: any;
  benchProjectListBulk: any;
  otherProjectListBulk: any;

  teamListBulk: any;
  filterOtherProjects() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredOtherProjectList = this.otherProjectList.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }
  setDefaultProjectUpdateForActiveProject(details: any, projectId, template: TemplateRef<any>) {
    console.log("test id", details.empId, details);
    this.defaultProjectUpdate.empIds = [details.empId];
    this.defaultProjectUpdate.projectId = projectId;
    this.defaultProjectUpdate.createdBy = this.currentUser.empId;
     if(!this.defaultProjectUpdate.projectId){
      this.openAlertMod3(this.alertTemplateWithoutReload, "Please update Default Project");
      return;
    }
    this.resourceManagementService.setDefaultProjectUpdateBillable(this.defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        this.activeProjects = this.activeProjects.filter(id => id !== details.empId);
        if (this.activeProjects.length === 0) {
          this.modalRef2.close();
          this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
        }
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        console.error("Error setting the default project!");
      }
    });
  }

  getTeamListForSelectedProject() {
    let selectedProjectId = this.setDefaultProjectObj.projectId;

    if (!selectedProjectId || !this.bulkProjectType)
      this.teamListBulk = [];

    const projectList = this.bulkProjectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
    this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
  }
  filterProjectsForDefaultBulkBench() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }

  filterProjectsForDefaultBulkOther() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }

  filterTeamsForDefaultBulk() {
    const lowerSearch = this.searchTermTeam.toLowerCase();
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList.filter(team =>
      team.teamName.toLowerCase().includes(lowerSearch)
    );
  }



  setProjectMappingAndDefaultProjectBulk(setDefaultProjectObj, template: TemplateRef<any>) {

    setDefaultProjectObj.empId = this.EmployessIds;

     if (
      !setDefaultProjectObj.projectId ||
      !setDefaultProjectObj.teamId ||
      !setDefaultProjectObj.employeeRole
    ) {
         this.openAlertMod3(this.alertTemplateWithoutReload, 'Project, Team, and Employee Role must be selected.');
      // alert('Project, Team, and Employee Role must be selected.');
      return;
    }
    if (setDefaultProjectObj.teamId !== null) {
      this.resourceManagementService.setProjectMappingAndDefaultProject(setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.bulkEmployeeList = response.serviceResponse;
          this.EmployessIds = [];
          setDefaultProjectObj = [];
          // this.getEmployeeInformationBulk(this.EmployessIds);
          this.modalRef?.close();
          if (this.activeProjects.length === 0 && this.EmployessIds.length === 0) {
            this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
          } else {
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the Employees default Projects");
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

  }

  getTeamListForSelectedProject1(emp) {
    let selectedProjectId = emp.projectId;

    if (!selectedProjectId || !this.bulkProjectType)
      this.teamListBulk = [];

    const projectList = emp.projectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
    this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
  }


  setProjectMappingAndDefaultProject(template: TemplateRef<any>, emp) {
    console.log("empId", emp, emp.empId);
    this.setDefaultProjectObj.empId = [emp.empId];
    this.setDefaultProjectObj.createdBy = this.currentUser.empId;
    this.setDefaultProjectObj.projectId = emp.projectId;
    this.setDefaultProjectObj.teamId = emp.teamId;
    this.setDefaultProjectObj.employeeRole = emp.employeeRole;
    this.setDefaultProjectObj.resourceOverViewId = emp.resourceOverViewId;

      if (
      !this.setDefaultProjectObj.projectId ||
      !this.setDefaultProjectObj.teamId ||
      !this.setDefaultProjectObj.employeeRole
    ) {
         this.openAlertMod3(this.alertTemplateWithoutReload, 'Project, Team, and Employee Role must be selected.');
      // alert('Project, Team, and Employee Role must be selected.');
      return;
    }

    if (this.setDefaultProjectObj.teamId !== null) {
      this.resourceManagementService.setProjectMappingAndDefaultProject(this.setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.bulkEmployeeList = response.serviceResponse;
          this.EmployessIds = this.EmployessIds.filter(id => id !== emp.empId);
          if (this.EmployessIds.length === 0) {
            this.modalRef3.close();
            this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

  }


  getProjectDetailsForBulkDefaultUpdate() {
    this.resourceManagementService.getProjectDetailsForBulkDefaultUpdate().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectListBulk = response.serviceResponse;
        this.benchProjectListBulk = this.projectListBulk.benchProjectList;
        this.otherProjectListBulk = this.projectListBulk.otherProjectList;
        this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk;
        this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk;
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Project List");
        console.error("Unable to fetch Project List!");
      }
    });
  }

  updateProjectStartDate() {
      if (!this.projectNewStartDate || this.projectNewStartDate == undefined || this.projectNewStartDate == null) {
        this.openAlertMessageModal("Kindly provide new Project Start Date!!");
        return;
      }
      let projectObj: Project = new Project();
      projectObj.projectId = this.projectObj.projectId;
      projectObj.startDate = moment(this.normalizeDate(this.projectNewStartDate)).format('YYYY-MM-DD');
      projectObj.updatedBy = this.currentUser.empId;

      this.projectService.updateProjectStartDate(projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.projectObj.projectStartDate = this.normalizeDate(this.projectNewStartDate);
          this.closeUpdateProjectStartDateConfirmationModal();
          this.openAlertMessageModal(response.serviceResponse);
        } else {
          this.openAlertMessageModal(response.serviceResponse || "Something went wrong!");
        }
      });
    }

  normalizeDate(dateInput: any) {
    if (!dateInput) {
      return null;
    }
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) {
      return null;
    }
    return moment(dateInput).startOf('day').format('YYYY-MM-DDTHH:mm:ss');
    // return new Date(date.getFullYear(), date.getMonth(), date.getDate());
  }

  closeUpdateProjectStartDateConfirmationModal() {
		if (this.updateProjectStartDateConfirmationModalRef) {
			this.updateProjectStartDateConfirmationModalRef?.close();
		}
	}

  closeErrorModal() {
		if (this.errModalRef) {
			this.errModalRef?.close();
		}
    this.startDate = '';
	}

  openUpdateProjectStartDateConfirmationModal() {
		this.projectNewStartDate = null;
		this.updateProjectStartDateConfirmationModalRef = this.modalService.open(this.updateProjStartDateModal,
      { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
	}

  openAlertMessageModal(modalMessage: any) {
		this.alertMessage = modalMessage;
		if (this.alertMessageModalRef) {
			this.cancelRequest();
		}
		this.alertMessageModalRef = this.modalService?.open(this.alertTemplate, { modalDialogClass: 'modal-sm' });
	}

  async validateEmployeeProjectStartDate(empId: any, projectId: any, projectType: any, teamId:any, edit_enddate_template?: any): Promise<boolean> {
    this.projectObj.isEndDateVisible = false;
    this.projectObj.memberMaxEndDate = null;
    let rmgMember: RmgTeamMember = new RmgTeamMember();
    rmgMember.empId = empId;
    rmgMember.startDate = this.startDate;
    rmgMember.teamId = teamId;
    let projectData = {
      currentProjectId: projectId,
      projectIds: [projectId],
      projectType: projectType
    };
    const response = await this.employeeProjectService.validateEmployeeProjectStartDateChange(rmgMember, projectData);
    if (response?.type === 'NO_CONFLICT' || response?.type === 'PROJECT_GAP') {
      return true;
    } else if (response?.type === 'EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT' || response?.type === 'OVERLAPPING_ENTRIES_FOUND_IN_THIS_PROJECT') {
      this.projectObj.isEndDateVisible = true;
      this.projectObj.memberMaxEndDate = response?.data.memberMaxEndDate;
      this.editEnddateModal(edit_enddate_template, this.projectObj);
      this.openAlertMessageModal(response?.message || 'Start date overlaps with an existing mapping. Ensure the current assignment ends before the next start date!!');
      return false;
    } else {
      return false;
    }
  }

  async validateRemoveMembers(project: any) {
    this.newProjectObj = project;
    this.projectType = (this.newProjectObj.poProjectType != null && this.newProjectObj.poProjectType.trim() !== '')
      ? this.newProjectObj.poProjectType : this.newProjectObj.internalProjectType;

    try {
      const response: any = await firstValueFrom(this.teamService.getTeamMemberDetailsByEmpIdAndProjectId(project?.projectId, project.empId, project.employeeTeamMapId));
      if (response.serviceStatus === "Success") {
        const selectedMembers = response.serviceResponse || [];
        this.markDefaultProjectCompletionList = [];
        this.mappingToOtherProjectAsDefaultList = [];
        this.selectedRemoveMembers = selectedMembers;

        // having no other active projects and this is default project
        let noOtherActiveAndCurrentIsDefaultProjectEmpIds: number[] = selectedMembers
          .filter(member =>
            Array.isArray(member.otherActiveProjectIds) &&
            member.otherActiveProjectIds.length === 0 &&
            member.defaultProject === true
          )
          .map(member => member.empId);

        // having other active projects and this is default project
        const otherActiveAndCurrentIsDefaultProjectEmpIds: number[] = selectedMembers
          .filter(member =>
            Array.isArray(member.otherActiveProjectIds) &&
            member.otherActiveProjectIds.length > 0 &&
            member.defaultProject === true
          )
          .map(member => member.empId);

        this.markDefaultProjectCompletionList = selectedMembers.filter(member => noOtherActiveAndCurrentIsDefaultProjectEmpIds.includes(member.empId));
        this.mappingToOtherProjectAsDefaultList = selectedMembers.filter(member => otherActiveAndCurrentIsDefaultProjectEmpIds.includes(member.empId));

        this.resetDefaultProjectCompletion(this.markDefaultProjectCompletionList);
        this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);

        if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
          this.openMarkDefaultProjectCompletionModal('REMOVE_MEMBERS');
          this.openMappingToOtherProjectAsDefaultModal('REMOVE_MEMBERS');
        } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length === 0) {
          this.openMarkDefaultProjectCompletionModal('REMOVE_MEMBERS');
        } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length === 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
          this.openMappingToOtherProjectAsDefaultModal('REMOVE_MEMBERS');
        } else {
          this.openRemoveMembersModal();
        }
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong!!');
      }
    } catch (error) {
      this.openAlertMessageModal('Something went wrong!!');
    }
  }

  openRemoveMembersModal() {
    this.membersEndDate = null;
    this.removePermanently = false;
    this.membersEndDateType = 'Custom';
    this.removeMembersConfirmationModalRef = this.modalService?.open(this.removeMembersConfirmationTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
  }

  onMemberRemoveEndDateTypeChange(event: any) {
    const selectedValue = event?.value;
    if (selectedValue === 'PO') {
      const poEndDate = this.selectedRemoveMembers?.[0]?.poEndDate;
      this.membersEndDate = poEndDate ? moment(poEndDate).format('YYYY-MM-DD') : null;
    } else if (selectedValue === 'Custom') {
      this.membersEndDate = null;
    }
  }

  closeRemoveMembersModal() {
    if (this.removeMembersConfirmationModalRef) {
      this.removeMembersConfirmationModalRef?.close();
    }
  }

  async openMarkDefaultProjectCompletionModal(actionType: any) {
    this.defaultProjectMappingActionType = actionType;
    this.defaultProjectObj = new SetDefaultProjectObj();
    this.defaultProjectObj.projectType = 'Bench'
    await this.getActiveProjectList();
    if (this.markDefaultProjectCompletionModalRef) {
      this.markDefaultProjectCompletionModalRef?.close();
    }
    this.markDefaultProjectCompletionModalRef = this.modalService?.open(this.markDefaultProjectCompletionTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
  }

  closeMarkDefaultProjectCompletionModal() {
    if (this.markDefaultProjectCompletionModalRef) {
      this.markDefaultProjectCompletionModalRef?.close();
    }
  }

  openMappingToOtherProjectAsDefaultModal(actionType: any) {
    this.defaultProjectMappingActionType = actionType;
    this.closeMappingToOtherProjectAsDefaultModal();
    this.drawerService.open(this.mappingToOtherProjectAsDefaultTemplateRef);
  }

  closeMappingToOtherProjectAsDefaultModal() {
    if (this.isValidList(this.mappingToOtherProjectAsDefaultList)) {
      this.mappingToOtherProjectAsDefaultList.forEach(member => {
        member.defaultProject = member.dbDefaultProject;
      });
    }
    this.drawerService?.close();
  }

  mapProjectListToEmployees(mappingToOtherProjectAsDefaultList: any[]) {
    if (this.isValidList(mappingToOtherProjectAsDefaultList) && this.isValidList(this.projectList)) {
      for (let emp of mappingToOtherProjectAsDefaultList) {
        if (this.isValidList(emp.otherActiveProjects) && emp.otherActiveProjects?.length == 1) {
          emp.selectedProject = emp.otherActiveProjects[0] || new EmployeeOtherActiveProject();
        } else {
          emp.selectedProject = new EmployeeOtherActiveProject();
        }
        emp.projectList = this.projectList.filter(p => emp?.otherActiveProjectIds?.includes(p.projectId));
      }
    }
  }

  resetDefaultProjectCompletion(markDefaultProjectCompletionList: any[]) {
    if (this.isValidList(markDefaultProjectCompletionList)) {
      for (let emp of markDefaultProjectCompletionList) {
        emp.projectType = null;
        emp.projectId = null;
        emp.poId = null;
        emp.teamId = null;
        emp.employeeRoles = null;
        emp.roleId = null;
        emp.startDate = null;
      }
    }
  }

  isValidList(list: any) {
    return Array.isArray(list) && this.validationService.validateNullUndefinedEmptyList(list);
  }

  isValidString(string: any) {
    return this.validationService.validateNullUndefinedEmptyStringTrim(string);
  }

  async getActiveProjectList() {
    this.projectList = [];
    this.projectsBench = [];
    this.projectsOther = [];
    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getActiveProjectList());
      if (response.serviceStatus == "Success") {
        this.projectList = response.serviceResponse;
        this.projectsBench = this.projectList.filter(project => project.internalProjectType === 'Bench');
        this.projectsOther = this.projectList.filter(project => project.internalProjectType !== 'Bench');
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    } catch (error) {
      this.openAlertMessageModal("Something went wrong!");
    }
  }

  toggleChangeEmployeeDefaultProjectMappingSearch() {
    this.changeEmployeeDefaultProjectMappingPage = 0;
    this.isChangeEmployeeDefaultProjectMappingSearchEnabled = !this.isChangeEmployeeDefaultProjectMappingSearchEnabled;
    if (!this.isChangeEmployeeDefaultProjectMappingSearchEnabled) {
      this.changeEmployeeDefaultProjectMappingFilters = {};
    }
  }

  searchChangeEmployeeDefaultProjectMapping(searchData: any) {
    this.changeEmployeeDefaultProjectMappingPage = 0;
    this.changeEmployeeDefaultProjectMappingFilters = searchData;
  }

  changeEmployeeDefaultProjectMappingPageChange(event: any) {
    this.changeEmployeeDefaultProjectMappingPage = event.pageIndex + 1;
    this.changeEmployeeDefaultProjectMappingPageSize = event.pageSize;
  }

  sortChangeEmployeeDefaultProjectMappingData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.changeEmployeeDefaultProjectMappingSortColumn = sortParams[0];
      this.changeEmployeeDefaultProjectMappingSortColumnType = sortParams[0];
      this.changeEmployeeDefaultProjectMappingSortDirection = sort.direction;
    }
  }

  toggleMarkDefaultProjectCompletionIndividualSearch() {
    this.markDefaultProjectCompletionIndividualPage = 0;
    this.isMarkDefaultProjectCompletionIndividualSearchEnabled = !this.isMarkDefaultProjectCompletionIndividualSearchEnabled;
    if (!this.isMarkDefaultProjectCompletionIndividualSearchEnabled) {
      this.markDefaultProjectCompletionIndividualFilters = {};
    }
  }

  searchMarkDefaultProjectCompletionIndividual(searchData: any) {
    this.markDefaultProjectCompletionIndividualPage = 0;
    this.markDefaultProjectCompletionIndividualFilters = searchData;
  }

  markDefaultProjectCompletionIndividualPageChange(event: any) {
    this.markDefaultProjectCompletionIndividualPage = event.pageIndex + 1;
    this.markDefaultProjectCompletionIndividualPageSize = event.pageSize;
  }

  sortMarkDefaultProjectCompletionIndividualData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.markDefaultProjectCompletionIndividualSortColumn = sortParams[0];
      this.markDefaultProjectCompletionIndividualSortColumnType = sortParams[0];
      this.markDefaultProjectCompletionIndividualSortDirection = sort.direction;
    }
  }

  updateDefaultProjectCompletion(employee: RmgTeamMember, isBulk: boolean) {
    if (!this.isValidString(employee?.projectType)) {
      this.openAlertMessageModal('Project Type must be selected!!');
      return;
    }
    if (!employee.projectId) {
      this.openAlertMessageModal('Kindly Select a Project!!');
      return;
    }
    if (!employee.teamId) {
      this.openAlertMessageModal('Kindly Select a Team!!');
      return;
    }
    if (!this.isValidList(employee.employeeRoles)) {
      this.openAlertMessageModal('Employee Role must be selected!!');
      return;
    }
    if (!employee.startDate || employee.startDate == undefined || employee.startDate == null) {
      this.openAlertMessageModal("Kindly Provide Start Date!!");
      return;
    }


    let tempRmgTeamMember = new RmgTeamMember();
    tempRmgTeamMember.empId = employee?.empId;
    tempRmgTeamMember.updatedBy = this.currentUser.empId;
    tempRmgTeamMember.projectId = employee.projectId;
    tempRmgTeamMember.teamId = employee.teamId;
    tempRmgTeamMember.poId = employee?.poId;
    tempRmgTeamMember.employeeRoles = employee.employeeRoles;
    tempRmgTeamMember.empTeamDepartmentId = employee.empTeamDepartmentId;
    tempRmgTeamMember.roleId = employee.roleId;
    tempRmgTeamMember.poRequirementMappingId = employee.poRequirementMappingId;
    tempRmgTeamMember.clientName = this.newProjectObj?.clientName;
    tempRmgTeamMember.selectedEmpIds = isBulk ? this.markDefaultProjectCompletionList?.map(member => member.empId) ?? [] : employee?.empId ? [employee.empId] : [];
    tempRmgTeamMember.projectType = this.projectType;
    tempRmgTeamMember.startDate = this.normalizeDate(this.defaultProjectObj.startDate);

    this.teamService.updateDefaultProjectCompletion(tempRmgTeamMember).pipe(first()).subscribe(async (response: any) => {
      if (response.serviceStatus == "Success") {
        this.closeMarkDefaultProjectCompletionModal();
        this.toastService.success(response.serviceResponse);
      } else {
        this.toastService.error(response.serviceResponse || "Something went wrong!!");
      }
    });
  }

  async updateMappingToOtherProjectAsDefault(employee: RmgTeamMember) {
    if (!employee?.selectedProject || !employee?.selectedProject?.projectId) {
      this.openAlertMessageModal("Please Select Default Project");
      return;
    }

    let rmgMember: RmgTeamMember = new RmgTeamMember();
    rmgMember.empId = employee.empId;
    rmgMember.startDate = employee.selectedProject.startDate;
    rmgMember.teamId = employee.teamId;
    let projectData = {
      currentProjectId: employee?.selectedProject?.projectId,
      projectIds: [employee?.selectedProject?.projectId],
      projectType: this.projectType
    };
    const response = await this.employeeProjectService.validateEmployeeProjectStartDateChange(rmgMember, projectData);
    if (response?.type === 'EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT') {
      this.openAlertMessageModal('Start date overlaps with an existing mapping. Ensure the current assignment ends before the next start date!!');
      return false;
    } else if (response?.type === 'EMPLOYEE_DATE_OF_JOINING_LESS_THAN_MEMBER_START_DATE') {
        return false;
    }

    employee.selectedProject.updatedBy = this.currentUser.empId;
    try {
      const response: any = await firstValueFrom(this.projectService.updateMappingToOtherProjectAsDefault(employee.selectedProject));
      if (response.serviceStatus == "Success") {
        this.toastService.success(response.serviceResponse);
        this.closeMappingToOtherProjectAsDefaultModal();
        this.getExistingProjectsByUser();
        await this.validateRemoveMembers(this.newProjectObj);
      } else {
        this.toastService.error(response.serviceResponse || "Something went wrong!");
      }
    } catch (error) {
      this.toastService.error("Something went wrong!");
    }
  }

  removeTeamMembersFromProject() {
    if (!this.isValidList(this.selectedRemoveMembers)) {
      this.openAlertMessageModal("Kindly Select atleast one member to Remove!!");
      return;
    }

    if (!this.removePermanently && this.membersEndDateType === 'PO') {
      const poEndDate = this.selectedRemoveMembers?.[0]?.poEndDate;
      this.membersEndDate = poEndDate ? moment(poEndDate).format('YYYY-MM-DD') : null;
    }

    if (!this.removePermanently && (!this.membersEndDate || this.membersEndDate == undefined || this.membersEndDate == null)) {
      this.openAlertMessageModal("Please provide End date!!");
      return;
    }

    const teamMembersEndDate = this.normalizeDate(this.membersEndDate);

    for (let member of this.selectedRemoveMembers) {
      const memberStartDate = this.normalizeDate(member.startDate);
      if (!this.removePermanently && (memberStartDate > teamMembersEndDate)) {
        this.openAlertMessageModal(`Member End date cannot be less then Member Start date for ${member.employementId}!!`);
        return;
      }
      member.removePermanently = this.removePermanently;
    }

    let rmgTeam = new RmgTeam();
    rmgTeam.projectId = this.newProjectObj.projectId;
    rmgTeam.teamId = this.newProjectObj?.teamId;
    rmgTeam.updatedBy = this.currentUser?.empId;
    rmgTeam.clientName = this.newProjectObj?.clientName;
    rmgTeam.rmgTeamMemberList = this.selectedRemoveMembers;
    rmgTeam.projectType = this.projectType;
    rmgTeam.isCustomEndDate = false; //this.customEndDate;
    rmgTeam.endDate = this.normalizeDate(this.membersEndDate);

    this.teamService.removeTeamMembersFromProject(rmgTeam).pipe(first()).subscribe(async (response: any) => {
      if (response.serviceStatus == "Success") {
        this.toastService.success(response.serviceResponse);
        this.getExistingProjectsByUser();
      } else {
        this.toastService.error(response.serviceResponse);
      }
    });
    this.closeRemoveMembersModal();
  }

  changeProjectTypeForEmployee(employee: RmgTeamMember) {
    employee.projectId = null;
    employee.poId = null;
    employee.poDetailsList = [];
    employee.teamId = null;
    employee.teamList = [];
    employee.employeeRoles = [];
    employee.poRequirementMappingId = null;
    employee.resourceRequirementList = [];
  }

  changeProjectType() {
    this.defaultProjectObj.projectId = null;
    this.defaultProjectObj.poId = null;
    this.defaultProjectObj.poDetailsList = [];
    this.defaultProjectObj.teamId = null;
    this.defaultProjectObj.teamList = [];
    this.defaultProjectObj.employeeRoles = [];
    this.defaultProjectObj.poRequirementMappingId = null;
    this.defaultProjectObj.resourceRequirementList = [];
  }

  getTeamOrPoBasedOnProjectTypeForEmployee(employee: RmgTeamMember) {
    employee.poId = null;
    employee.poDetailsList = [];
    employee.teamId = null;
    employee.teamList = [];
    employee.employeeRoles = [];
    employee.poRequirementMappingId = null;
    employee.resourceRequirementList = [];
    employee.projectStartDate = this.projectList?.find(p => p.projectId === this.defaultProjectObj.projectId)?.startDate;
    employee.calculatedProjectType = this.projectList?.find(p => p.projectId === this.defaultProjectObj.projectId)?.projectType;
    if (employee.projectType !== 'Bench') {
      this.getActivePoDetailsByProjectIdForEmployee(employee);
    }
    this.getActiveTeamDetailsByProjectIdForEmployee(employee);
  }

  getActiveTeamDetailsByProjectIdForEmployee(member: RmgTeamMember) {
    member.teamList = [];
    this.teamService.getActiveTeamDetailsByProjectId(member.projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const teamList = response.serviceResponse || [];
        member.teamList = teamList;
      } else {
        this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
      }
    });
  }

  async getActivePoDetailsByProjectIdForEmployee(member: RmgTeamMember) {
    member.poDetailsList = [];
    if (this.projectIdPoListMap.has(member.projectId)) {
      member.poDetailsList = this.projectIdPoListMap.get(member.projectId);
      return;
    }
    const response = await this.employeeProjectService.getPoDetailsByProjectId(member.projectId);
    const poDetailsList = response?.data || [];
    member.poDetailsList = poDetailsList;
    this.projectIdPoListMap.set(member.projectId, poDetailsList);
  }

  async getResourceRequirementByPoIdForEmployeeDefaultMapping(member: RmgTeamMember) {
    if (this.allNonBillableProjectTypes.includes(member.projectType?.toLowerCase())) {
      return;
    }
    member.poRequirementMappingId = null;
    member.resourceRequirementList = [];
    const response = await this.employeeProjectService.getResourceRequirementByPoId(member.poId);
    member.resourceRequirementList = response.data;
    member.filteredActiveResourceRequirement = [...member.resourceRequirementList];
  }

  getActiveTeamDetailsByPoIdForEmployee(member: RmgTeamMember) {
    member.teamList = [];
    member.employeeRoles = [];
    member.poRequirementMappingId = null;
    member.resourceRequirementList = [];
    if (this.poIdTeamListMap.has(member.poId)) {
      member.teamList = this.poIdTeamListMap.get(member.poId);
      return;
    }

    this.teamService.getActiveTeamDetailsByPoId(member.poId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const teamList = response.serviceResponse || [];
        member.teamList = teamList;
        this.poIdTeamListMap.set(member.poId, teamList);
      } else {
        this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
      }
    });
  }

  async validateEmployeeProjectStartDateDefaultProjectMapping(employee: any, projectId: any, projectType: any, edit_enddate_template?: any): Promise<boolean> {
    let rmgMember: RmgTeamMember = new RmgTeamMember();
    rmgMember.empId = employee.empId;
    rmgMember.startDate = employee.selectedProject.startDate;
    rmgMember.teamId = employee.selectedProject.teamId;
    let projectData = {
      currentProjectId: projectId,
      projectIds: [projectId],
      projectType: projectType
    };
    const response = await this.employeeProjectService.validateEmployeeProjectStartDateChange(rmgMember, projectData);
    if (response?.type === 'NO_CONFLICT' || response?.type === 'PROJECT_GAP') {
      return true;
    } else if (response?.type === 'EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT' || response?.type === 'OVERLAPPING_ENTRIES_FOUND_IN_THIS_PROJECT') {
      // this.projectObj.isEndDateVisible = true;
      // this.projectObj.memberMaxEndDate = response?.data.memberMaxEndDate;
      // this.editEnddateModal(edit_enddate_template, this.projectObj);
      this.openAlertMessageModal('Start date overlaps with an existing mapping. Ensure the current assignment ends before the next start date!!');
      return false;
    } else {
      return false;
    }
  }

}
