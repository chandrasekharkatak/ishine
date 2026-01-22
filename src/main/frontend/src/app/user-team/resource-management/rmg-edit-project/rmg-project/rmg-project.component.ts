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
import { MatCheckboxChange } from '@angular/material/checkbox';

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
  @ViewChild("migrate_team") migrateTeamTemplateRef: TemplateRef<any>;
  @ViewChild("delete_team") deleteTeamTemplateRef: TemplateRef<any>;

  alertMessageModalRef: NgbModalRef;
  migrateTeamModalRef: NgbModalRef;
  deleteTeamModalRef: NgbModalRef;

  currentUser: User;
  userMapping: any = {};

  employeeObj: Employee = new Employee();
  newteamMember: TeamMember = new TeamMember();
  // migrateTeamObj:LiftAndShift();

  fixedCostTypes = ['fixed cost'];
  employeeRoles: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];

  projectList: any[] = [];
  migrationTeamIds: any[] = [];

  modalMessage: string = '';
  projectType: string = '';
  teamMigrationTargetProjectId: any;
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

  openMigrateTeamModal(po: PoDetails) {
    this.getActiveProjectList();
    this.migrateTeamModalRef = this.modalService?.open(this.migrateTeamTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeMigrateTeamModal() {
    if (this.migrateTeamModalRef) {
      this.migrateTeamModalRef?.close();
    }
  }

  openDeleteTeamModal(po: PoDetails) {
    this.deleteTeamModalRef = this.modalService?.open(this.deleteTeamTemplateRef, { modalDialogClass: 'modal-lg' });
  }

  closeDeleteTeamModal() {
    if (this.deleteTeamModalRef) {
      this.deleteTeamModalRef?.close();
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

  setMembersSelection(members: RmgTeamMember[], isSelected: boolean): void {
    members.forEach(m => (m.isMemberSelected = isSelected));
  }

  onPoCheckboxChange(po: PoDetails): void {
    po.isPoSelected = !po.isPoSelected;
    po.teamList.forEach(team => {
      team.isTeamSelected = po.isPoSelected;
      team.rmgResourceRequirementList.forEach(role => {
        role.isRequirementSelected = po.isPoSelected;
        this.setMembersSelection(role.rmgCurrentTeamMemberList, po.isPoSelected);
      });
    });

    this.updatePoActionButton(po);
  }

  onTeamCheckboxChange(po: PoDetails, team: RmgTeam): void {
    team.isTeamSelected = !team.isTeamSelected;
    team.rmgResourceRequirementList.forEach(role => {
      role.isRequirementSelected = team.isTeamSelected;
      this.setMembersSelection(role.rmgCurrentTeamMemberList, team.isTeamSelected);
    });

    this.updatePoSelection(po);
    this.updatePoActionButton(po);
  }

  onRoleCheckboxChange(po: PoDetails, team: RmgTeam, role: RmgResourceRequirement): void {
    role.isRequirementSelected = !role.isRequirementSelected;
    this.setMembersSelection(role.rmgCurrentTeamMemberList, role.isRequirementSelected);

    this.updateTeamSelection(team);
    this.updatePoSelection(po);
    this.updatePoActionButton(po);
  }

  onMemberCheckboxChange(po: PoDetails, team: RmgTeam, role: RmgResourceRequirement): void {
    this.updateRoleSelection(role);
    this.updateTeamSelection(team);
    this.updatePoSelection(po);
    this.updatePoActionButton(po);
  }

  updateRoleSelection(role: RmgResourceRequirement): void {
    role.isRequirementSelected =
      role.rmgCurrentTeamMemberList.length > 0 &&
      role.rmgCurrentTeamMemberList.every(member => member.isMemberSelected);
  }

  updateTeamSelection(team: RmgTeam): void {
    team.isTeamSelected =
      team.rmgResourceRequirementList.length > 0 &&
      team.rmgResourceRequirementList.every(role => role.isRequirementSelected);
  }

  updatePoSelection(po: PoDetails): void {
    po.isPoSelected =
      po.teamList.length > 0 &&
      po.teamList.every(team => team.isTeamSelected);
  }

  updatePoActionButton(po: PoDetails): void {
    po.isAnyMemberSelected = po.teamList.some(team =>
      team.rmgResourceRequirementList.some(role =>
        role.rmgCurrentTeamMemberList.some(
          member => member.isMemberSelected
        )
      )
    );
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

  onAllTeamSelectCheckboxChange(event: MatCheckboxChange, po: PoDetails): void {
    const flag: boolean = event.checked;
    po.selectedTeamIds = [];
    po.isPoSelected = flag;

    if (this.isValidList(po.teamList)) {
      for (let team of po.teamList) {
        team.isTeamSelected = flag;
        if (flag) {
          po.selectedTeamIds.push(team.teamId);
        }
      }
    }
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
  onEdit(member: RmgTeamMember): void {
    console.log('Edit', member);
  }

  onDelete(member: RmgTeamMember): void {
    console.log('Delete', member);
  }

  addNewTeamMember(requirement: RmgResourceRequirement, po: PoDetails) {

  }

  migrateTeam() {
    // this.liftAndShiftObj.currentUserEmpId = this.currentUser.empId;
    // this.liftAndShiftObj.teamIds = this.selectedTeamsDetails.map(team => team.teamId);
    // this.resourceManagementService.liftAndShiftTeams(this.liftAndShiftObj).pipe(first()).subscribe((response: any) => {
    //   if (response.serviceStatus == "Success") {
    //     this.openAlertModWithReload(this.modalRefWithReloadTemp, response.serviceResponse);
    //   } else {
    //     this.openAlertMod(this.alertTemplateWithoutReload, response.serviceResponse);
    //   }
    // });
  }
  //  Team Members Method & APIs End

  getActiveProjectList() {
    this.projectList = [];
    this.resourceManagementService.getActiveProjectList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.projectList = response.serviceResponse;
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }

}