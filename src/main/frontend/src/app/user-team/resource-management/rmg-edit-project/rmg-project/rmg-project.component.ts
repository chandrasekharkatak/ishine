import { ViewportScroller } from '@angular/common';
import { Component, ElementRef, EventEmitter, Input, OnInit, Renderer2, TemplateRef, ViewChild, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { AppComponent } from 'src/app/app.component';
import { Project } from 'src/app/models/project';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { FilterStateService } from 'src/app/services/filter-state.service';
import { LoaderService } from 'src/app/services/loader.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { environment } from 'src/environments/environment';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { Team } from 'src/app/models/team';
import { TeamMember } from 'src/app/models/teamMember';
import { RmgProject } from 'src/app/models/rmgProject';
import { finalize, first, map, startWith, catchError } from 'rxjs/operators';
import { PoDetails } from 'src/app/models/poDetails';
import { RmgTeam } from 'src/app/models/rmgTeam';
import { RmgResourceRequirement } from 'src/app/models/rmgResourceRequirement';
import { RmgTeamMember } from 'src/app/models/rmgTeamMember';

@Component({
  standalone: false,
  selector: 'app-rmg-project',
  templateUrl: './rmg-project.component.html',
  styleUrl: './rmg-project.component.css'
})

export class RmgProjectComponent implements OnInit {

  @Input() rmgProjectObj: RmgProject = new RmgProject();
  @Input() managerList: any[] = [];
  @Input() overheadList: any[] = [];
  @Input() departmentsList: any[] = [];
  @Input() spocList: any[] = [];
  @Output() closeProjectConfiguration = new EventEmitter<any>();

  @ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
  @ViewChild('employee_details') employeeDetailsTemplateRef: TemplateRef<any>;
  @ViewChild('employee_project_timesheet_summary') employeeProjectTimesheetSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('expired_tnm_projects_summary') expiredTNMProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('fixed_cost_projects_summary') fixedCostProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('total_projects_summary') totalProjectsSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('team_project_status_summary') teamProjectStatusSummaryTemplateRef: TemplateRef<any>;
  @ViewChild('project_details') projectDetailsTemplateRef: TemplateRef<any>;

  alertMessageModalRef: NgbModalRef;
  employeeDetailsModalRef: NgbModalRef;
  employeeProjectTimesheetSummaryModalRef: NgbModalRef;
  expiredTNMProjectsSummaryModalRef: NgbModalRef;
  fixedCostProjectsSummaryModalRef: NgbModalRef;
  totalProjectsSummaryModalRef: NgbModalRef;
  projectDetailsModalRef: NgbModalRef;
  teamProjectStatusSummaryModalRef: NgbModalRef;

  currentUser: User;
  userMapping: any = {};

  employeeObj: Employee = new Employee();
  newteamMember: TeamMember = new TeamMember();

  fixedCostTypes = ['fixed cost'];
  employeeRoles: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];

  modalMessage: string = '';
  projectType: string = '';

  projectConfigStepperIndex: number = 1;

  loadingRequirements: boolean = true;


  constructor(
    private filterStateService: FilterStateService,
    public validationService: ValidationService,
    private employeeService: EmployeeService,
    private modalService: NgbModal,
    private teamService: TeamService,
    private resourceManagementService: ResourceManagementService,
    private authenticationService: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private employee360Service: Employee360Service,
    private sanitizer: DomSanitizer,
    private loaderService: LoaderService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.projectConfigStepperIndex = 0; // Stepper Default to Project Information
    this.setProjectType();
    this.initialiseNewTeamObj();
  }

  callCloseProjectConfiguration() {
    this.closeProjectConfiguration.emit();
  }

  // Modals Start

  openAlertMessageModal(modalMessage: any) {
    this.modalMessage = modalMessage;
    this.alertMessageModalRef = this.modalService?.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef?.close();
    }
  }


  // Modals End

  // Helpers Start

  isValidList(list: any) {
    return this.validationService.validateNullUndefinedEmptyList(list);
  }

  isValidString(string: any) {
    return this.validationService.validateNullUndefinedEmptyStringTrim(string);
  }

  setProjectType(): void {
    if (this.isValidString(this.rmgProjectObj.poProjectType)) {
      this.projectType = this.rmgProjectObj.poProjectType;
    } else if (this.isValidString(this.rmgProjectObj.internalProjectType)) {
      this.projectType = this.rmgProjectObj.internalProjectType;
    } else {
      this.projectType = 'NA';
    }
  }

  initialiseNewTeamObj() {
    if (this.rmgProjectObj && this.isValidList(this.rmgProjectObj?.poDetailsList)) {
      for (let po of this.rmgProjectObj?.poDetailsList) {
        po.newTeamObj = new RmgTeam();
      }
    }
  }

  setTeamDepartmentNames(teamList: RmgTeam[]) {
    if (this.isValidList(teamList)) {
      for (let team of teamList) {
        if (this.isValidList(team.deptIds)) {
          const departmentNames = this.departmentsList
            .filter(x => team.deptIds?.includes(x.deptId))
            .map(x => x.name).join(', ');

          team.departmentNames = departmentNames
        }
      }
    }
  }

  createCurrentAndOldResourceList(teamList: RmgTeam[]) {
    if (this.isValidList(teamList)) {
      for (let team of teamList) {
        if (this.isValidList(team.rmgResourceRequirementList)) {
          for (let rmgReq of team.rmgResourceRequirementList) {
            rmgReq.newRmgTeamMember = new RmgTeamMember();
            this.setRequirementResourceType(rmgReq, true);
            rmgReq.rmgCurrentTeamMemberList = rmgReq.rmgTeamMemberList.filter(teamMember => {
              return teamMember.isMemberActive != 0
            });
            rmgReq.rmgOldTeamMemberList = rmgReq.rmgTeamMemberList.filter(teamMember => {
              return teamMember.isMemberActive == 0
            });
          }
        }
      }
    }
  }

  setRequirementResourceType(requirement: RmgResourceRequirement, isCurrentResource: boolean) {
    requirement.requirementType = isCurrentResource ? 'Current Resource' : 'Old Resource';
  }
  // Helpers End

  // Steppers Method Start

  onProjectConfigStepChange(event: any) {
    if (event) {
      this.projectConfigStepperIndex = event?.selectedIndex;
      console.log(event);
    }
  }

  // Steppers Method End


  // Team Method & APIs Start
  checkNewTeamName() {

  }

  addNewTeam(po: any) {

  }

  getTeamListByPoId(po: PoDetails) {
    this.teamService.getAllTeamsAndRoleWiseMembersByPoId(po?.poId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        po.teamList = response.serviceResponse || [];
        this.setTeamDepartmentNames(po.teamList);
        this.createCurrentAndOldResourceList(po.teamList);
      }
    });
  }
  // Team Method & APIs End

  // PO List Method & APIs Start
  onPoExpand(po: PoDetails) {
    if (!this.isValidList(po?.teamList)) {
      this.getTeamListByPoId(po);
    }
  }

  // PO List Method & APIs End

  //  Team Members Method & APIs Start

  toggleAll(event: Event, teamMembers: RmgTeamMember[]): void {
    const checked = (event.target as HTMLInputElement).checked;
    teamMembers?.forEach(m => m.selected = checked);
  }

  isAllSelected(teamMembers: RmgTeamMember[]): boolean {
    return teamMembers?.length > 0 && teamMembers?.every(m => m.selected);
  }

  onRowSelect(member: RmgTeamMember): void {
    console.log('Selected members:', member);
  }

  getSelectedMembers(teamMembers: RmgTeamMember[]): RmgTeamMember[] {
    return teamMembers?.filter(m => m.selected);
  }

  onEdit(member: RmgTeamMember): void {
    console.log('Edit', member);
  }

  onDelete(member: RmgTeamMember): void {
    console.log('Delete', member);
  }

  addNewTeamMember(requirement: RmgResourceRequirement, po: PoDetails) {

  }
  //  Team Members Method & APIs End

}