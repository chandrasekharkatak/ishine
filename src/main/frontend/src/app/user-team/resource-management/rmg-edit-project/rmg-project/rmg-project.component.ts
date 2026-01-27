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
import { DefaultProjectUpdate } from 'src/app/models/defaultProjectUpdate';
import { SetDefaultProjectObj } from 'src/app/models/setDefaultProjectObj';
import { MigrateTeams } from 'src/app/models/migrateTeam';

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
  @ViewChild("role_list") roleListTemplateRef: TemplateRef<any>;
  @ViewChild("remove_members_confirmation") removeMembersConfirmationTemplateRef: TemplateRef<any>;
  @ViewChild("mark_default_project_completion") markDefaultProjectCompletionTemplateRef: TemplateRef<any>;
  @ViewChild("mapping_other_project_as_default") mappingToOtherProjectAsDefaultTemplateRef: TemplateRef<any>;
  @ViewChild("employee_existing_project_details") employeeExistingProjectDetailsTemplateRef: TemplateRef<any>;
  @ViewChild("delete_employee_from_existing_project") deleteEmployeeFromExistingProjectTemplateRef: TemplateRef<any>;


  alertMessageModalRef: NgbModalRef;
  migrateTeamModalRef: NgbModalRef;
  deleteTeamModalRef: NgbModalRef;
  roleListModalRef: NgbModalRef;
  removeMembersConfirmationModalRef: NgbModalRef;
  markDefaultProjectCompletionModalRef: NgbModalRef;
  mappingToOtherProjectAsDefaultModalRef: NgbModalRef;
  employeeExistingProjectDetailsModalRef: NgbModalRef;
  deleteEmployeeFromExistingProjectModalRef: NgbModalRef;

  currentUser: User;
  userMapping: any = {};

  employeeObj: Employee = new Employee();
  currentTeam: RmgTeam = new RmgTeam();
  currentPo: PoDetails = new PoDetails();
  defaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
  deleteEmployeeExistingProjectMappingObj: RmgTeamMember;
  tempRequirement: any;
  // migrateTeamObj:LiftAndShift();

  fixedCostTypes = ['fixed cost'];
  employeeRoles: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  projectTypes: any[] = ['Bench', 'Other'];

  projectList: any[] = [];
  projectsBench: any[] = [];
  projectsOther: any[] = [];
  employeeListFilteredByDept: any[] = [];
  employeeExistingProjectDetails: any[] = [];
  resourceRequirementList: RmgResourceRequirement[] = [];
  mappingToOtherProjectAsDefaultList: RmgTeamMember[] = [];
  markDefaultProjectCompletionList: RmgTeamMember[] = [];

  projectIdPoListMap = new Map<Number, PoDetails[]>();
  poIdTeamListMap = new Map<Number, RmgTeam[]>();
  teamIdResourceReqListMap = new Map<Number, RmgResourceRequirement[]>();

  alertMessage: string = '';
  projectType: string = '';
  employeeProjectEndDateType: 'PO' | 'Custom' = 'Custom';

  newPoRequirementMappingId: any;
  employeeExistingProjectDetailsPage: number = 1;
  employeeExistingProjectBillableType: any;

  projectConfigStepperIndex: number = 1;
  loadingRequirements: boolean = true;
  isMarkDefaultProjectCompletionBulk: boolean = true;
  // isAddNewTeamMemberDisabled: boolean = false;

  mappingToOtherProjectId: any;
  membersEndDate: any;
  employeeProjectEndDate: any;


  // Team Migration
  teamMigrationObj: MigrateTeams = new MigrateTeams();
  teamMigrationPoDetailsList: any[] = [];
  teamMigrationTeamList: any[] = [];
  teamMigrationResourceRequirementList: any[] = [];

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
    this.getActiveProjectList();
    this.setProjectType();
    this.initialiseNewTeamObj();
  }

  // Modals Start
  openAlertMessageModal(modalMessage: any) {
    this.alertMessage = modalMessage;
    this.alertMessageModalRef = this.modalService?.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeAlertMessageModal() {
    if (this.alertMessageModalRef) {
      this.alertMessageModalRef?.close();
    }
  }

  openMigrateTeamModal(po: PoDetails) {
    this.teamMigrationObj.migrationTeamIds = po?.teamList?.map(team => {
      if (team.isTeamSelected) {
        return team.teamId;
      }
    });
    this.migrateTeamModalRef = this.modalService?.open(this.migrateTeamTemplateRef, { modalDialogClass: 'modal-md' });
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

  openRoleListModal() {
    this.roleListModalRef = this.modalService?.open(this.roleListTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
  }

  closeRoleListModal() {
    if (this.roleListModalRef) {
      this.roleListModalRef?.close();
    }
  }

  openRemoveMembersModal() {
    this.removeMembersConfirmationModalRef = this.modalService?.open(this.removeMembersConfirmationTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
  }

  closeRemoveMembersModal() {
    if (this.removeMembersConfirmationModalRef) {
      this.removeMembersConfirmationModalRef?.close();
    }
  }

  openMarkDefaultProjectCompletionModal() {
    this.markDefaultProjectCompletionModalRef = this.modalService?.open(this.markDefaultProjectCompletionTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
  }

  closeMarkDefaultProjectCompletionModal() {
    if (this.markDefaultProjectCompletionModalRef) {
      this.markDefaultProjectCompletionModalRef?.close();
    }
  }

  openMappingToOtherProjectAsDefaultModal() {
    this.mappingToOtherProjectAsDefaultModalRef = this.modalService?.open(this.mappingToOtherProjectAsDefaultTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
  }

  closeMappingToOtherProjectAsDefaultModal() {
    if (this.mappingToOtherProjectAsDefaultModalRef) {
      this.mappingToOtherProjectAsDefaultModalRef?.close();
    }
  }

  openEmployeeExistingProjectDetailsModal() {
    this.employeeExistingProjectDetailsModalRef = this.modalService?.open(this.employeeExistingProjectDetailsTemplateRef, { modalDialogClass: 'modal-lg' });
  }

  closeEmployeeExistingProjectDetailsModal() {
    if (this.employeeExistingProjectDetailsModalRef) {
      this.employeeExistingProjectDetailsModalRef?.close();
    }
  }

  openDeleteEmployeeFromExistingProjectModal(employee: any) {
    if (this.employeeProjectEndDateType !== 'Custom') {
      this.employeeProjectEndDate = employee.endDate;
    }
    this.deleteEmployeeExistingProjectMappingObj = employee;
    this.deleteEmployeeFromExistingProjectModalRef = this.modalService?.open(this.deleteEmployeeFromExistingProjectTemplateRef, { modalDialogClass: 'modal-sm' });
  }

  closeDeleteEmployeeFromExistingProjectModal() {
    if (this.deleteEmployeeFromExistingProjectModalRef) {
      this.deleteEmployeeFromExistingProjectModalRef?.close();
    }
  }
  // Modals End

  // Validation Methods End
  isValidList(list: any) {
    return this.validationService.validateNullUndefinedEmptyList(list);
  }

  isValidString(string: any) {
    return this.validationService.validateNullUndefinedEmptyStringTrim(string);
  }

  isValidNumber(value: any): boolean {
    return typeof value === 'number' && !isNaN(value);
  }

  validateRmgProjectObj(template: TemplateRef<any>) {
    if (!this.isValidList(this.rmgProjectObj.projectManagerIds)) {
      this.openAlertMessageModal('Please Select atleast one Project manager!!');
      return;
    }
    if (!this.isValidList(this.rmgProjectObj?.poDetailsList)) {
      this.openAlertMessageModal(`No PO's found, kindly contact admin`);
      return;
    }
    if (!this.containsAtleastOneTeam()) {
      this.openAlertMessageModal('Please add atleast one Team!!');
      return;
    }

    let obj: { message: string, flag: boolean };
    for (let index = 0; index < this.rmgProjectObj?.poDetailsList.length; index++) {
      const po = this.rmgProjectObj?.poDetailsList[index];
      obj = this.validatePo(po, index + 1);
      if (!obj.flag) {
        break;
      }
    }
    if (!obj.flag) {
      this.openAlertMessageModal(obj.message);
      return;
    }
  }

  validateRemovingMembers(po: PoDetails, team: RmgTeam, requirement: RmgResourceRequirement) {
    let selectedMembers = requirement?.rmgCurrentTeamMemberList.filter(member => member.isMemberSelected);
    if (!this.isValidList(selectedMembers)) {
      this.openAlertMessageModal("Please select atleast one Team Member to remove!!");
    }
    // const selectedEmpIds = this.selectedTeamEntries.map(item => item.empId);

    // having no other active projects and this is default project
    let noOtherActiveAndCurrentIsDefaultProjectEmpIds: number[] = selectedMembers
      .filter(member =>
        Array.isArray(member.otherActiveProjectIds) &&
        member.otherActiveProjectIds.length === 0 &&
        member.defaultProject === true
      )
      // .filter(member => selectedEmpIds.includes(member)
      .map(member => member.empId);

    // having other active projects and this is default project
    const otherActiveAndCurrentIsDefaultProjectEmpIds: number[] = selectedMembers
      .filter(member =>
        Array.isArray(member.otherActiveProjectIds) &&
        member.otherActiveProjectIds.length > 0 &&
        member.defaultProject === true
      )
      // .filter(member => selectedEmpIds.includes(member)
      .map(member => member.empId);

    this.markDefaultProjectCompletionList = selectedMembers.filter(member => noOtherActiveAndCurrentIsDefaultProjectEmpIds.includes(member.empId));
    this.mappingToOtherProjectAsDefaultList = selectedMembers.filter(member => otherActiveAndCurrentIsDefaultProjectEmpIds.includes(member.empId));
    if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
      this.openMarkDefaultProjectCompletionModal();
      this.openMappingToOtherProjectAsDefaultModal();
    } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length === 0) {
      this.openMarkDefaultProjectCompletionModal();
    } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length === 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
      this.openMappingToOtherProjectAsDefaultModal();
    } else {
      this.openRemoveMembersModal();
    }
  }

  containsAtleastOneTeam() {
    let containsAtleastOneTeam = false;
    if (this.isValidList(this.rmgProjectObj?.poDetailsList)) {
      containsAtleastOneTeam = this.rmgProjectObj?.poDetailsList.some(po => this.isValidList(po.teamList));
    }
    return containsAtleastOneTeam;
  }

  validatePo(po: PoDetails, index?: any) {
    let message = '';
    let flag = true;
    if (this.isValidList(po.teamList)) {
      for (let teamIdx = 0; teamIdx < po.teamList.length; teamIdx++) {
        let team = po.teamList[teamIdx];
        if (!this.isValidString(team.teamName)) {
          message = 'PO is invalid';
          flag = false;
        }
      }
    }
    return { message, flag };
  }

  validateTeamName(po: PoDetails, team: RmgTeam) {
    let teamName = team.teamName;
    if (!this.isValidString(teamName)) {
      this.openAlertMessageModal("Kindly Provide a valid Team Name!!");
      return false;
    }
    if (this.isValidList(po.teamList)) {
      for (let t of po.teamList) {
        if (this.isValidString(t.teamName) && t?.teamId !== team?.teamId && t.teamName === teamName) {
          this.openAlertMessageModal("Team Name already exists!!");
          return false;
        }
      }
    }
    // else {
    // API Check will be Impl Later
    // }
    return true;
  }
  // Validation Methods End

  // Helpers Start
  callCloseProjectConfiguration() {
    this.closeProjectConfiguration.emit();
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

  createCurrentAndOldResourceList(rmgResourceRequirementList: RmgResourceRequirement[]) {
    if (this.isValidList(rmgResourceRequirementList)) {
      for (let rmgReq of rmgResourceRequirementList) {
        rmgReq.newRmgTeamMember = new RmgTeamMember();
        rmgReq.newRmgTeamMember.isNotSaved = true;

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

  setRequirementResourceType(requirement: RmgResourceRequirement, isCurrentResource: boolean) {
    requirement.requirementType = isCurrentResource ? 'Current Resource' : 'Old Resource';
  }

  setRequirementResourceTypeForTeam(team: RmgTeam, isCurrentResource: boolean) {
    team.rmgResourceRequirementList.forEach(requirement => {
      requirement.requirementType = isCurrentResource ? 'Current Resource' : 'Old Resource';
    });
  }

  getTodaysDate() {
    const today = new Date();
    return today.toISOString().split('T')[0];
  }

  handleEmployeeProjectDetailsPageChange(event: any) {
    this.employeeExistingProjectDetailsPage = event;
  }

  mapEmployeeProjectDates(projects: any[]): any[] {
    return projects.map(project => ({
      ...project,
      startDate: project.startDate ? new Date(project.startDate) : null,
      updatedOn: project.updatedOn ? new Date(project.updatedOn) : null
    }));
  }

  toggleMergeIntoExistingTeam() {
    if (!this.teamMigrationObj.mergeTeam) {
      this.teamMigrationObj.targetTeamId = null;
      this.teamMigrationTeamList = [];
      return;
    }
    this.getActiveTeamDetailsByPoIdForTeamMigration();
  }
  // Helpers End

  // Checkbox Helper Methods Start
  setMembersSelection(members: RmgTeamMember[], isSelected: boolean): void {
    members.forEach(m => {
      if (!m.isNotSaved) {
        m.isMemberSelected = isSelected;
      }
    });
  }

  onPoCheckboxChange(po: PoDetails): void {
    po.isPoSelected = !po.isPoSelected;
    po.teamList.forEach(team => {
      team.isTeamSelected = po.isPoSelected;
    });
    this.updatePoActionButton(po);
  }

  onTeamCheckboxChange(po: PoDetails, team: RmgTeam): void {
    team.isTeamSelected = !team.isTeamSelected;
    this.updatePoSelection(po);
    this.updatePoActionButton(po);
  }

  onRoleCheckboxChange(po: PoDetails, team: RmgTeam, role: RmgResourceRequirement): void {
    role.isRequirementSelected = !role.isRequirementSelected;
    this.setMembersSelection(role.rmgCurrentTeamMemberList, role.isRequirementSelected);
    this.updateRemoveTeamMemberButton(role);
  }

  onMemberCheckboxChange(po: PoDetails, team: RmgTeam, role: RmgResourceRequirement): void {
    this.updateRoleSelection(role);
    this.updateRemoveTeamMemberButton(role);
  }

  updateRoleSelection(resourceReq: RmgResourceRequirement): void {
    resourceReq.isRequirementSelected =
      resourceReq.rmgCurrentTeamMemberList.length > 0 &&
      resourceReq.rmgCurrentTeamMemberList.filter(member => !member.isNotSaved)
        .every(member => member.isMemberSelected);
  }

  updatePoSelection(po: PoDetails): void {
    po.isPoSelected = po.teamList.length > 0 && po.teamList.every(team => team.isTeamSelected);
  }

  updatePoActionButton(po: PoDetails): void {
    po.isAnyTeamSelected = po.teamList.some(team => team.isTeamSelected);
  }

  updateRemoveTeamMemberButton(resourceReq: RmgResourceRequirement): void {
    resourceReq.isAnyMemberSelected = resourceReq.rmgCurrentTeamMemberList.some(member => member.isMemberSelected);
  }
  // Checkbox Helper Methods End


  // Steppers Method Start
  onProjectConfigStepChange(event: any) {
    if (event) {
      this.projectConfigStepperIndex = event?.selectedIndex;
      console.log(event);
    }
  }
  // Steppers Method End


  // Team Method & APIs Start
  getAllTeamsByPoId(po: PoDetails) {
    this.teamService.getAllTeamsByPoId(po?.poId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        po.teamList = response.serviceResponse || [];
        this.setTeamDepartmentNames(po.teamList);
      }
    });
  }

  addNewTeam(po: PoDetails) {
    const team = po.newTeamObj;
    const isTeamNameValid = this.validateTeamName(po, team);
    if (!isTeamNameValid) {
      return;
    }
    if (!this.isValidList(team.deptIds)) {
      this.openAlertMessageModal("Kindly Select atleast one department!!");
      return;
    }
    po.teamList.push(team);
    po.newTeamObj = new RmgTeam();
  }

  migrateTeam() {
    if (!this.teamMigrationObj.targetProjectId || !this.isValidNumber(this.teamMigrationObj.targetProjectId)) {
      this.openAlertMessageModal("Kindly Select target Project!!");
      return;
    }
    if (!this.teamMigrationObj.targetPoId || !this.isValidNumber(this.teamMigrationObj.targetPoId)) {
      this.openAlertMessageModal("Kindly Select PO!!");
      return;
    }
    if (this.teamMigrationObj.mergeTeam && (!this.teamMigrationObj.targetTeamId || !this.isValidNumber(this.teamMigrationObj.targetTeamId))) {
      this.openAlertMessageModal("Kindly Select an existing Team in which to merge the Selected Team(s)!!");
      return;
    }

    this.teamMigrationObj.sourceProjectId = this.rmgProjectObj.projectId;
    this.teamMigrationObj.currentUserEmpId = this.currentUser.empId;
    this.teamService.migrateTeam(this.teamMigrationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong!!');
      }
    });
  }

  async getTeamDetailsByTeamId(team: RmgTeam, po: PoDetails) {
    this.currentPo = po;
    this.currentTeam = team;
    this.employeeListFilteredByDept = [];
    await this.getResourceRequirementByPoId(po);

    if (!team.teamId) {
      this.openRoleListModal();
      return;
    }
    this.teamService.getTeamDetailsByTeamId(team?.teamId, po?.projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        team.rmgResourceRequirementList = response.serviceResponse || [];

        // Filter out role where already any resource was mapped previously
        const poRequirementMappingId = team.rmgResourceRequirementList.map(req => req.poRequirementMappingId);
        this.resourceRequirementList = this.resourceRequirementList.filter(req => !poRequirementMappingId.includes(req.poRequirementMappingId));

        this.employeeListFilteredByDept = this.spocList.filter(emp => team.deptIds?.includes(emp.deptId));
        this.createCurrentAndOldResourceList(team.rmgResourceRequirementList);
        this.setRequirementResourceTypeForTeam(team, true);
        this.openRoleListModal();
      } else {
        this.openAlertMessageModal(response.serviceResponse || 'Something went wrong!!');
      }
    });
  }

  getActiveTeamDetailsByPoIdForEmployee(member: RmgTeamMember) {
    member.teamList = [];
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
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }

  getActiveTeamDetailsByPoId() {
    this.defaultProjectObj.teamList = [];
    if (this.poIdTeamListMap.has(this.defaultProjectObj.poId)) {
      this.defaultProjectObj.teamList = this.poIdTeamListMap.get(this.defaultProjectObj.poId);
      return;
    }

    this.teamService.getActiveTeamDetailsByPoId(this.defaultProjectObj.poId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const teamList = response.serviceResponse || [];
        this.defaultProjectObj.teamList = teamList;
        this.poIdTeamListMap.set(this.defaultProjectObj.poId, teamList);
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }

  getActiveTeamDetailsByPoIdForTeamMigration() {
    this.teamMigrationTeamList = [];
    this.teamService.getActiveTeamDetailsByPoId(this.teamMigrationObj.targetPoId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamMigrationTeamList = response.serviceResponse || [];
        if (!this.isValidList(this.teamMigrationTeamList)) {
          this.openAlertMessageModal("No Teams found in this PO, Kindly select another PO!!");
          return;
        }
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }

  // Team Method & APIs End

  // PO List Method & APIs Start
  onPoExpand(po: PoDetails) {
    if (!this.isValidList(po?.teamList)) {
      this.getAllTeamsByPoId(po);
    }
  }

  getActivePoDetailsByProjectIdForEmployee(member: RmgTeamMember) {
    member.poDetailsList = [];
    if (this.projectIdPoListMap.has(member.projectId)) {
      member.poDetailsList = this.projectIdPoListMap.get(member.projectId);
      return;
    }

    this.resourceManagementService.getActivePoDetailsByProjectId(member.projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const poDetailsList = response.serviceResponse || [];
        member.poDetailsList = poDetailsList;
        this.projectIdPoListMap.set(member.projectId, poDetailsList);
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }

  getActivePoDetailsByProjectId() {
    this.defaultProjectObj.poDetailsList = [];
    if (this.projectIdPoListMap.has(this.defaultProjectObj.projectId)) {
      this.defaultProjectObj.poDetailsList = this.projectIdPoListMap.get(this.defaultProjectObj.projectId);
      return;
    }

    this.resourceManagementService.getActivePoDetailsByProjectId(this.defaultProjectObj.projectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const poDetailsList = response.serviceResponse || [];
        this.defaultProjectObj.poDetailsList = poDetailsList;
        this.projectIdPoListMap.set(this.defaultProjectObj.projectId, poDetailsList);
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }

  getActivePoDetailsByProjectIdForTeamMigration() {
    this.teamMigrationPoDetailsList = [];
    this.resourceManagementService.getActivePoDetailsByProjectId(this.teamMigrationObj.targetProjectId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.teamMigrationPoDetailsList = response.serviceResponse || [];
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }
  // PO List Method & APIs End

  //  Team Members Method & APIs Start
  addNewTeamMemberToCurrentResourceList(rmgReq: RmgResourceRequirement, team: RmgTeam, po: PoDetails) {
    if (!this.isValidList(rmgReq.rmgCurrentTeamMemberList)) {
      rmgReq.rmgCurrentTeamMemberList = [];
    }

    rmgReq.newRmgTeamMember.memberName = this.employeeListFilteredByDept
      ?.find(emp => emp?.empId === rmgReq.newRmgTeamMember?.empId)?.name;
    rmgReq.newRmgTeamMember.teamId = team.teamId;
    rmgReq.newRmgTeamMember.poId = po.poId;

    this.employeeListFilteredByDept = this.employeeListFilteredByDept.filter(emp => emp.empId !== rmgReq.newRmgTeamMember?.empId);

    rmgReq.rmgCurrentTeamMemberList.push(rmgReq.newRmgTeamMember);
    rmgReq.newRmgTeamMember = new RmgTeamMember();
    this.updateAddTeamMemberButton(rmgReq);
  }

  removeTeamMemberFromCurrentResourceList(po: PoDetails, team: RmgTeam, rmgReq: RmgResourceRequirement, rmgTeamMember: RmgTeamMember) {
    if (!this.isValidList(rmgReq.rmgCurrentTeamMemberList)) {
      rmgReq.rmgCurrentTeamMemberList = [];
    }

    rmgReq.rmgCurrentTeamMemberList = rmgReq.rmgCurrentTeamMemberList.filter(member => member.empId !== rmgTeamMember.empId);
    this.employeeListFilteredByDept = this.spocList.filter(emp => team.deptIds?.includes(emp.deptId));
    this.updateAddTeamMemberButton(rmgReq);
  }

  addNewTeamMembers(po: PoDetails, team: RmgTeam, requirement: RmgResourceRequirement) {

  }

  updateExistingTeamMembers(po: PoDetails, team: RmgTeam, requirement: RmgResourceRequirement) {

  }

  removeTeamMembersFromPO() {

  }

  getAddNewTeamMemberButtonTitle(requirement: RmgResourceRequirement, newRmgTeamMember: any): string {
    if (!newRmgTeamMember.empId) {
      return 'Please Select a Resource.';
    }
    if (!newRmgTeamMember.employeeRoles?.length) {
      return 'Please Select a Role before adding the Resource.';
    }
    if (this.employeeExistingProjectDetails.length > 0 && this.rmgProjectObj?.poProjectType?.toLowerCase() === "tnm") {
      return 'The selected resource is already allocated to an existing project and is not eligible for assignment to a TNM project. Please remove the resource from the existing project and try again.';
    }
    return 'Add New Team Member.';
  }

  isAddNewTeamMemberButtonDisabled(requirement: RmgResourceRequirement, newRmgTeamMember: any): boolean {
    if (!newRmgTeamMember.empId) {
      return true;
    }
    if (!newRmgTeamMember.employeeRoles?.length) {
      return true;
    }
    if (this.employeeExistingProjectDetails.length > 0 && this.rmgProjectObj?.poProjectType?.toLowerCase() === "tnm") {
      return true;
    }
    return false;
  }

  updateAddTeamMemberButton(resourceReq: RmgResourceRequirement): void {
    resourceReq.isAnyNewMemberAdded = resourceReq.rmgCurrentTeamMemberList.some(member => member.isNotSaved);
  }

  // Team Members Method & APIs End

  // Resource Requirements Method & APIs Start
  async getResourceRequirementByPoId(po: PoDetails) {
    this.resourceRequirementList = [];
    const response: any = await this.resourceManagementService.getResourceRequirementByPoId(po?.poId).pipe(first()).toPromise();
    if (response.serviceStatus === "Success") {
      this.resourceRequirementList = response.serviceResponse || [];
      for (let req of this.resourceRequirementList) {
        req.displayValue = 'Role - ' + req.role + ' | Dept - ' + req.department +
          ' | Exp - ' + req.experience + ' yrs | Required Resource Count - ' + req.count;
      }
    } else {
      this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
    }
  }

  addResourceRequirementToTeam(team: RmgTeam) {
    if (!this.isValidList(team.rmgResourceRequirementList)) {
      team.rmgResourceRequirementList = [];
    }

    // When role is changed add only those role requirement which already not exist
    const poRequirementMappingId = team.rmgResourceRequirementList.map(req => req.poRequirementMappingId);
    if (!poRequirementMappingId.includes(this.newPoRequirementMappingId)) {
      const requirement = this.resourceRequirementList.find(req => req.poRequirementMappingId === this.newPoRequirementMappingId);
      team.rmgResourceRequirementList.push(requirement);
      this.createCurrentAndOldResourceList(team.rmgResourceRequirementList);
      this.setRequirementResourceTypeForTeam(team, true);
      this.currentTeam.rmgResourceRequirementList = team.rmgResourceRequirementList;
    }
  }

  getResourceRequirementByTeamIdForEmployee(member: RmgTeamMember) {
    member.resourceRequirementList = [];
    if (this.teamIdResourceReqListMap.has(member.teamId)) {
      member.resourceRequirementList = this.teamIdResourceReqListMap.get(member.teamId);
      return;
    }

    this.resourceManagementService.getResourceRequirementByTeamId(member.teamId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const resourceRequirementList = response.serviceResponse || [];
        for (let req of resourceRequirementList) {
          req.displayValue = 'Role - ' + req.role + ' | Dept - ' + req.department +
            ' | Exp - ' + req.experience + ' yrs | Required Resource Count - ' + req.count;
        }
        member.resourceRequirementList = resourceRequirementList;
        this.teamIdResourceReqListMap.set(member.teamId, resourceRequirementList);
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }

  getResourceRequirementByTeamId() {
    this.defaultProjectObj.resourceRequirementList = [];
    if (this.teamIdResourceReqListMap.has(this.defaultProjectObj.teamId)) {
      this.defaultProjectObj.resourceRequirementList = this.teamIdResourceReqListMap.get(this.defaultProjectObj.teamId);
      return;
    }

    this.resourceManagementService.getResourceRequirementByTeamId(this.defaultProjectObj.teamId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const resourceRequirementList = response.serviceResponse || [];
        this.defaultProjectObj.resourceRequirementList = resourceRequirementList;
        this.teamIdResourceReqListMap.set(this.defaultProjectObj.teamId, resourceRequirementList);
      } else {
        this.openAlertMessageModal(response.serviceRespone || "Something went wrong!!");
      }
    });
  }
  // Resource Requirements Method & APIs End

  // Projects Method & APIs Start
  getActiveProjectList() {
    this.projectList = [];
    this.resourceManagementService.getActiveProjectList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.projectList = response.serviceResponse;
        this.projectsBench = this.projectList.filter(project => project.internalProjectType === 'Bench');
        this.projectsOther = this.projectList.filter(project => project.internalProjectType !== 'Bench');
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }

  updateMappingToOtherProjectAsDefault() {
    let defaultProjectUpdate: DefaultProjectUpdate = new DefaultProjectUpdate();
    if (!this.mappingToOtherProjectId) {
      this.openAlertMessageModal("Please Select Default Project");
      return;
    }
    defaultProjectUpdate.empIds = this.mappingToOtherProjectAsDefaultList.map(employee => employee.empId);
    defaultProjectUpdate.projectId = this.mappingToOtherProjectId;
    defaultProjectUpdate.createdBy = this.currentUser.empId;
    this.membersEndDate = this.getTodaysDate();

    this.resourceManagementService.setDefaultProjectUpdateBillable(defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    });
  }

  updateDefaultProjectCompletion(emp: any) {

  }

  updateDefaultProjectCompletionBulk() {

  }

  getEmployeeExistingProjectDetailsByEmpId(requirement: RmgResourceRequirement, empId: any) {
    this.tempRequirement = requirement;
    this.employeeExistingProjectDetails = [];
    this.employeeExistingProjectDetailsPage = 1;

    this.projectService.getEmployeeExistingProjectDetailsByEmpId(empId).pipe(first()).subscribe((response: any) => {
      if (response?.serviceStatus !== "Success") {
        this.openAlertMessageModal(response?.serviceRespone || "Something went wrong!!");
        return;
      }

      this.employeeExistingProjectDetails = this.mapEmployeeProjectDates(response.serviceResponse || []);

      if (!this.isValidList(this.employeeExistingProjectDetails)) {
        requirement.newRmgTeamMember.billableType = "NewMember";
        return;
      }
      const billableType = this.employeeExistingProjectDetails[0]?.billableType;
      this.employeeExistingProjectBillableType = billableType;
      this.openEmployeeExistingProjectDetailsModal();

      if (billableType === "TNM") {
        this.openAlertMessageModal("This Employee is already mapped to TNM project. Cannot be added to another Project or Team!!");
      } else {
        requirement.newRmgTeamMember.billableType = "NewMember";
      }
    });
  }

  deleteEmployeeProjectResourceMapping() {
    this.deleteEmployeeExistingProjectMappingObj.isCustomDate = this.employeeProjectEndDateType === 'Custom';
    this.deleteEmployeeExistingProjectMappingObj.rescEndDate = new Date(this.employeeProjectEndDate);
    this.deleteEmployeeExistingProjectMappingObj.updatedBy = this.currentUser.empId;
    this.deleteEmployeeExistingProjectMappingObj.rescRemovedBy = this.currentUser.empId;
    console.log("Delete Employee Project Resource Mapping : ", this.deleteEmployeeExistingProjectMappingObj)
    this.projectService.updateEmployeeProjectMappingAsInActive(this.deleteEmployeeExistingProjectMappingObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMessageModal(response.serviceResponse);
        this.getEmployeeExistingProjectDetailsByEmpId(this.tempRequirement, this.deleteEmployeeExistingProjectMappingObj.empId);
      } else {
        this.openAlertMessageModal(response.serviceResponse);
      }
    })
  }
  // Projects Method & APIs End

}