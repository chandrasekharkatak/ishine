import { Component, EventEmitter, Input, OnInit, TemplateRef, ViewChild, Output } from '@angular/core';
import { SafeResourceUrl } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Project } from 'src/app/models/project';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { ValidationService } from 'src/app/services/validation.service';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { RmgProject } from 'src/app/models/rmgProject';
import { first } from 'rxjs/operators';
import { PoDetails } from 'src/app/models/poDetails';
import { RmgTeam } from 'src/app/models/rmgTeam';
import { RmgResourceRequirement } from 'src/app/models/rmgResourceRequirement';
import { RmgTeamMember } from 'src/app/models/rmgTeamMember';
import { SetDefaultProjectObj } from 'src/app/models/setDefaultProjectObj';
import { MigrateTeams } from 'src/app/models/migrateTeam';
import { ViewImageComponent } from 'src/app/user-team/view-image/view-image.component';
import { MatDialog } from '@angular/material/dialog';
import { Status } from 'src/app/enum/status';
import { FCProjectMilestone } from 'src/app/models/fcProjectMileStone';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { ToastService } from 'src/app/services/toast.service';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { firstValueFrom } from 'rxjs';
import { EmployeeOtherActiveProject } from 'src/app/models/employeeOtherActiveProject';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';

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
	@Input() isProjectPreview: boolean = true;
	@Input() isAllProjects: boolean = false;
	@Output() closeProjectConfiguration = new EventEmitter<any>();

	@ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
	@ViewChild("migrate_team") migrateTeamTemplateRef: TemplateRef<any>;
	@ViewChild("delete_team") deleteTeamTemplateRef: TemplateRef<any>;
	@ViewChild("team_details") teamDetailsTemplateRef: TemplateRef<any>;
	@ViewChild("remove_members_confirmation") removeMembersConfirmationTemplateRef: TemplateRef<any>;
	@ViewChild("mark_default_project_completion") markDefaultProjectCompletionTemplateRef: TemplateRef<any>;
	@ViewChild("mapping_other_project_as_default") mappingToOtherProjectAsDefaultTemplateRef: TemplateRef<any>;
	@ViewChild("employee_existing_project_details") employeeExistingProjectDetailsTemplateRef: TemplateRef<any>;
	@ViewChild("delete_employee_from_existing_project") deleteEmployeeFromExistingProjectTemplateRef: TemplateRef<any>;
	@ViewChild("delete_team_confirmation") deleteTeamConfirmationTemplateRef: TemplateRef<any>;
	@ViewChild("add_new_member") addNewMemberTemplateRef: TemplateRef<any>;
	@ViewChild("update_project_start_date_error") updateProjectStartDateErrorTemplateRef: TemplateRef<any>;
	@ViewChild("update_project_start_date_confirmation") updateProjectStartDateConfirmationTemplateRef: TemplateRef<any>;
	@ViewChild("extend_team_member_end_date") extendTeamMemberEndDateTemplateRef: TemplateRef<any>;
	@ViewChild("update_project_milestone") updateProjectMilestoneTemplateRef: TemplateRef<any>;
	@ViewChild("project_milestone_document") projectMilestoneDocumentTemplateRef: TemplateRef<any>;
	@ViewChild("mark_complete_fc_project") markCompleteFCProjectTemplateRef: TemplateRef<any>;

	alertMessageModalRef: NgbModalRef;
	migrateTeamModalRef: NgbModalRef;
	deleteTeamModalRef: NgbModalRef;
	teamDetailsModalRef: NgbModalRef;
	removeMembersConfirmationModalRef: NgbModalRef;
	markDefaultProjectCompletionModalRef: NgbModalRef;
	mappingToOtherProjectAsDefaultModalRef: NgbModalRef;
	employeeExistingProjectDetailsModalRef: NgbModalRef;
	deleteEmployeeFromExistingProjectModalRef: NgbModalRef;
	deleteTeamConfirmationModalRef: NgbModalRef;
	addNewMemberModalRef: NgbModalRef;
	updateProjectStartDateErrorModalRef: NgbModalRef;
	updateProjectStartDateConfirmationModalRef: NgbModalRef;
	extendTeamMemberEndDateModalRef: NgbModalRef;
	updateProjectMilestoneModalRef: NgbModalRef;
	projectMilestoneDocumentModalRef: NgbModalRef;
	markCompleteFCProjectModalRef: NgbModalRef;

	currentUser: User;
	userMapping: any = {};
	projectConfigStepperIndex: number = 1;
	alertMessage: string = '';
	projectType: string = '';
	employeeProjectEndDateType: 'PO' | 'Custom' = 'Custom';

	// Objects
	currentTeam: RmgTeam = new RmgTeam();
	defaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
	teamMembersMigrationObj: MigrateTeams = new MigrateTeams();
	deleteEmployeeExistingProjectMappingObj: RmgTeamMember;
	deleteTeamsPo: PoDetails = new PoDetails();

	// Arrays
	fixedCostTypes = ['fixed cost'];
	allBillableProjectTypes = ['tnm', 'fixed cost', 'monitoring'];
	allNonBillableProjectTypes = ['internalrndproducts', 'bench', 'internal'];
	employeeRoles: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
	projectTypes: any[] = ['Bench', 'Other'];

	projectList: any[] = [];
	projectsBench: any[] = [];
	projectsOther: any[] = [];
	employeeListFilteredByDept: any[] = [];
	employeeExistingProjectDetails: any[] = [];
	teamMigrationPoDetailsList: any[] = [];
	teamMigrationTeamList: any[] = [];
	teamMigrationResourceRequirementList: any[] = [];
	selectedMembersEmpId: any[] = [];
	poDetailsList: PoDetails[] = [];
	currentRmgProjectResourceRequirementList: RmgResourceRequirement[] = [];
	allRmgProjectResourceRequirementList: RmgResourceRequirement[] = [];
	resourceRequirementList: RmgResourceRequirement[] = [];
	selectedRemoveMembers: RmgTeamMember[] = [];
	mappingToOtherProjectAsDefaultList: RmgTeamMember[] = [];
	markDefaultProjectCompletionList: RmgTeamMember[] = [];
	teamMigrationTeamMembersList: RmgTeamMember[] = [];

	// Maps for caching
	projectIdPoListMap = new Map<number, PoDetails[]>();
	poIdTeamListMap = new Map<number, RmgTeam[]>();
	teamIdResourceReqListMap = new Map<number, RmgResourceRequirement[]>();

	// Flags 
	isHOD: boolean = false;
	isInternalProject: boolean = false;
	isMarkDefaultProjectCompletionBulk: boolean = true;
	isBulkTeamMemberMigration: boolean = true;
	isUnsavedMemberUpdate: boolean = false;

	// Dates
	membersEndDate: any;
	employeeProjectEndDate: any;
	projectNewStartDate: any
	teamEndDate: any;

	// FC Project Milestone 
	projectMilestonepage = 1;
	file: File | null = null;
	selectedFilePreviewUrl: string | null = null;
	milestoneDocumentUrl: SafeResourceUrl | null = null;
	fcProjectMilestoneList: FCProjectMilestone[] = [];
	statusList = [Status.NOT_STARTED, Status.IN_PROGRESS, Status.ON_HOLD, Status.COMPLETED];
	projectMilestone: FCProjectMilestone = new FCProjectMilestone();

	// Client Side Pagination
	// Old Team Member
	isOldTeamMemberSearchEnabled: boolean = false;
	oldTeamMemberPage = 1;
	oldTeamMemberPageSize = 10;
	oldTeamMemberSortColumn: string = '';
	oldTeamMemberSortColumnType: string = '';
	oldTeamMemberSortDirection: string = 'asc';
	oldTeamMemberFilters: any = {};
	oldTeamMemberSearchOnEnter: boolean = true;
	rmgOldTeamMemberColumnList: any[] = ['employementId', 'memberName', 'poNo', 'displayRequirement', 'employeeRole', 'memberDepartment', 'blank', 'blank', 'blank', 'blank'];
	rmgOldTeamMemberColumnListForInternal = ['employementId', 'memberName', 'employeeRole', 'memberDepartment', 'blank', 'blank', 'blank', 'blank'];

	// Current Team Member
	isCurrentTeamMemberSearchEnabled: boolean = false;
	currentTeamMemberPage = 1;
	currentTeamMemberPageSize = 10;
	currentTeamMemberSortColumn: string = '';
	currentTeamMemberSortColumnType: string = '';
	currentTeamMemberSortDirection: string = 'asc';
	currentTeamMemberFilters: any = {};
	currentTeamMemberSearchOnEnter: boolean = true;
	rmgCurrentTeamMemberColumnList: any[] = ['blank', 'blank', 'employementId', 'memberName', 'poNo', 'displayRequirement', 'blank', 'blank', 'blank', 'blank'];
	rmgCurrentTeamMemberColumnListForInternal: any[] = ['blank', 'blank', 'employementId', 'memberName', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank'];

	// Migrate Team Member
	isMigrateTeamMemberSearchEnabled: boolean = false;
	migrateTeamMemberPage = 1;
	migrateTeamMemberPageSize = 10;
	migrateTeamMemberSortColumn: string = '';
	migrateTeamMemberSortColumnType: string = '';
	migrateTeamMemberSortDirection: string = 'asc';
	migrateTeamMemberFilters: any = {};
	migrateTeamMemberSearchOnEnter: boolean = true;
	migrateTeamMemberColumnListBulk: any[] = ['blank', 'employementId', 'memberName', 'teamName', 'employeeRole', 'poNo', 'displayRequirement'];

	// Migrate Individual Team Member
	isMigrateIndividualTeamMemberSearchEnabled: boolean = false;
	migrateIndividualTeamMemberPage = 1;
	migrateIndividualTeamMemberPageSize = 10;
	migrateIndividualTeamMemberSortColumn: string = '';
	migrateIndividualTeamMemberSortColumnType: string = '';
	migrateIndividualTeamMemberSortDirection: string = 'asc';
	migrateIndividualTeamMemberFilters: any = {};
	migrateIndividualTeamMemberSearchOnEnter: boolean = true;
	migrateIndividualTeamMemberColumnList: any[] = ['employementId', 'memberName', 'teamName', 'employeeRole', 'poNo', 'displayRequirement', 'blank', 'blank', 'blank'];

	//  Change Employee Default Project Mapping
	isChangeEmployeeDefaultProjectMappingSearchEnabled: boolean = false;
	changeEmployeeDefaultProjectMappingPage = 1;
	changeEmployeeDefaultProjectMappingPageSize = 10;
	changeEmployeeDefaultProjectMappingSortColumn: string = '';
	changeEmployeeDefaultProjectMappingSortColumnType: string = '';
	changeEmployeeDefaultProjectMappingSortDirection: string = 'asc';
	changeEmployeeDefaultProjectMappingFilters: any = {};
	changeEmployeeDefaultProjectMappingSearchOnEnter: boolean = true;
	changeEmployeeDefaultProjectMappingColumnList: any[] = ['employementId', 'memberName', 'blank', 'blank', 'blank', 'blank', 'blank', 'blank'];

	//  Employee Existing Project Details
	isEmployeeExistingProjectDetailsSearchEnabled: boolean = false;
	employeeExistingProjectDetailsPage = 1;
	employeeExistingProjectDetailsPageSize = 10;
	employeeExistingProjectDetailsSortColumn: string = '';
	employeeExistingProjectDetailsSortColumnType: string = '';
	employeeExistingProjectDetailsSortDirection: string = 'asc';
	employeeExistingProjectDetailsFilters: any = {};
	employeeExistingProjectDetailsSearchOnEnter: boolean = true;
	employeeExistingProjectDetailsColumnList: any[] = ['projectName', 'teamName', 'clientName', 'billableType', 'startDate', 'endDate', 'blank']

	constructor(
		public validationService: ValidationService,
		private readonly toastService: ToastService,
		private readonly modalService: NgbModal,
		private readonly teamService: TeamService,
		private readonly resourceManagementService: ResourceManagementService,
		private readonly authenticationService: AuthenticationService,
		private readonly projectService: ProjectService,
		private readonly dialog: MatDialog,
	) {
		this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
	}

	ngOnInit(): void {
		this.isHOD = ['HOD', 'SuperAdmin', 'Superadmin', 'Super Admin'].includes(this.currentUser.employeeRole);
		this.isInternalProject = this.rmgProjectObj.internalProjectType != undefined && this.rmgProjectObj.internalProjectType != null && ['internal', 'internalrndproducts', 'bench'].includes(this.rmgProjectObj.internalProjectType?.trim()?.toLowerCase());
		this.projectConfigStepperIndex = this.isProjectPreview ? 2 : 0; // Stepper Default to Project Information
		this.getActiveProjectList();
		this.setProjectType();
		this.initialiseNewTeamObj();
		if (!this.isInternalProject) {
			this.getPoDetailsByProjectId();
		}
	}

	// Modals Start
	openAlertMessageModal(modalMessage: any) {
		this.alertMessage = modalMessage;
		if (this.alertMessageModalRef) {
			this.closeAlertMessageModal();
		}
		this.alertMessageModalRef = this.modalService?.open(this.alertMessageTemplateRef, { modalDialogClass: 'modal-sm' });
	}

	closeAlertMessageModal() {
		if (this.alertMessageModalRef) {
			this.alertMessageModalRef?.close();
		}
	}

	openMigrateTeamModal() {
		this.teamMembersMigrationObj = new MigrateTeams();
		this.teamMembersMigrationObj.migrationTeamIds = this.rmgProjectObj?.teamDetailsList?.map(team => {
			if (team.isTeamSelected) {
				return team.teamId;
			}
		});
		if (this.projectType === 'TNM') {
			this.getActiveTeamMembersByTeamIdsAndProjectId();
		} else {
			this.migrateTeamModalRef = this.modalService?.open(this.migrateTeamTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
		}
	}

	closeMigrateTeamModal() {
		if (this.migrateTeamModalRef) {
			this.migrateTeamModalRef?.close();
		}
	}

	openDeleteTeamModal() {
		this.deleteTeamModalRef = this.modalService?.open(this.deleteTeamTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
	}

	closeDeleteTeamModal() {
		if (this.deleteTeamModalRef) {
			this.deleteTeamModalRef?.close();
		}
	}

	openTeamDetailsModal() {
		if (this.teamDetailsModalRef) {
			this.teamDetailsModalRef?.close();
		}
		this.teamDetailsModalRef = this.modalService?.open(this.teamDetailsTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
	}

	closeTeamDetailsModal() {
		this.selectedMembersEmpId = [];
		if (this.teamDetailsModalRef) {
			this.teamDetailsModalRef?.close();
		}
	}

	openRemoveMembersModal() {
		this.membersEndDate = null;
		this.removeMembersConfirmationModalRef = this.modalService?.open(this.removeMembersConfirmationTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
	}

	closeRemoveMembersModal() {
		if (this.removeMembersConfirmationModalRef) {
			this.removeMembersConfirmationModalRef?.close();
		}
	}

	openMarkDefaultProjectCompletionModal() {
		this.defaultProjectObj = new SetDefaultProjectObj();
		this.defaultProjectObj.projectType = 'Bench'
		this.getActiveProjectList();
		this.markDefaultProjectCompletionModalRef = this.modalService?.open(this.markDefaultProjectCompletionTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
	}

	closeMarkDefaultProjectCompletionModal() {
		if (this.markDefaultProjectCompletionModalRef) {
			this.markDefaultProjectCompletionModalRef?.close();
		}
	}

	openMappingToOtherProjectAsDefaultModal() {
		this.mappingToOtherProjectAsDefaultModalRef = this.modalService?.open(this.mappingToOtherProjectAsDefaultTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
	}

	closeMappingToOtherProjectAsDefaultModal() {
		if (this.isValidList(this.mappingToOtherProjectAsDefaultList)) {
			this.mappingToOtherProjectAsDefaultList.forEach(member => {
				member.defaultProject = member.dbDefaultProject;
			});
		}
		if (this.mappingToOtherProjectAsDefaultModalRef) {
			this.mappingToOtherProjectAsDefaultModalRef?.close();
		}
	}

	openEmployeeExistingProjectDetailsModal() {
		this.employeeExistingProjectDetailsModalRef = this.modalService?.open(this.employeeExistingProjectDetailsTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
	}

	closeEmployeeExistingProjectDetailsModal() {
		if (this.employeeExistingProjectDetailsModalRef) {
			this.employeeExistingProjectDetailsModalRef?.close();
		}
	}

	openDeleteEmployeeFromExistingProjectModal(employee: any) {
		this.employeeProjectEndDate = null;
		if (this.employeeProjectEndDateType !== 'Custom') {
			this.employeeProjectEndDate = employee.endDate;
		}
		this.deleteEmployeeExistingProjectMappingObj = employee;
		this.deleteEmployeeFromExistingProjectModalRef = this.modalService?.open(this.deleteEmployeeFromExistingProjectTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
	}

	closeDeleteEmployeeFromExistingProjectModal() {
		if (this.deleteEmployeeFromExistingProjectModalRef) {
			this.deleteEmployeeFromExistingProjectModalRef?.close();
		}
	}

	openDeleteTeamConfirmationModal() {
		this.deleteTeamConfirmationModalRef = this.modalService?.open(this.deleteTeamConfirmationTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
	}

	closeDeleteTeamConfirmationModal() {
		if (this.deleteTeamConfirmationModalRef) {
			this.deleteTeamConfirmationModalRef?.close();
		}
	}

	openUpdateProjectMilestoneModal(milestone: any) {
		this.projectMilestone = JSON.parse(JSON.stringify(milestone));
		this.updateProjectMilestoneModalRef = this.modalService.open(this.updateProjectMilestoneTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
	}

	closeUpdateProjectMilestoneModal() {
		if (this.updateProjectMilestoneModalRef) {
			this.updateProjectMilestoneModalRef?.close();
		}
	}

	openProjectMilestoneDocumentModal(milestoneId: any) {
		this.projectMilestoneDocumentModalRef = this.modalService.open(this.projectMilestoneDocumentTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
	}

	closeProjectMilestoneDocumentsModal() {
		if (this.projectMilestoneDocumentModalRef) {
			this.projectMilestoneDocumentModalRef?.close();
		}
	}

	openMarkAsCompleteFCProjectModal() {
		this.markCompleteFCProjectModalRef = this.modalService.open(this.markCompleteFCProjectTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
	}

	closeMarkAsCompleteFCProjectModal() {
		if (this.markCompleteFCProjectModalRef) {
			this.markCompleteFCProjectModalRef?.close();
		}
	}

	openAddNewMemberModal() {
		this.isUnsavedMemberUpdate = false;
		this.currentTeam.newRmgTeamMember = new RmgTeamMember();
		this.addNewMemberModalRef = this.modalService.open(this.addNewMemberTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
	}

	closeAddNewMemberModal() {
		this.currentTeam.newRmgTeamMember = new RmgTeamMember();
		if (this.addNewMemberModalRef) {
			this.addNewMemberModalRef?.close();
		}
	}

	openEditEmployee(member: RmgTeamMember) {
		this.isUnsavedMemberUpdate = true;
		this.currentTeam.newRmgTeamMember = member;
		this.addNewMemberModalRef = this.modalService.open(this.addNewMemberTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
	}

	openUpdateProjectStartDateErrorModal() {
		this.updateProjectStartDateErrorModalRef = this.modalService.open(this.updateProjectStartDateErrorTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
	}

	closeUpdateProjectStartDateErrorModal() {
		if (this.updateProjectStartDateErrorModalRef) {
			this.updateProjectStartDateErrorModalRef?.close();
		}
	}

	openUpdateProjectStartDateConfirmationModal() {
		this.projectNewStartDate = null;
		this.updateProjectStartDateConfirmationModalRef = this.modalService.open(this.updateProjectStartDateConfirmationTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
	}

	closeUpdateProjectStartDateConfirmationModal() {
		if (this.updateProjectStartDateConfirmationModalRef) {
			this.updateProjectStartDateConfirmationModalRef?.close();
		}
	}

	openExtendTeamMemberEndDateModal() {
		this.extendTeamMemberEndDateModalRef = this.modalService.open(this.extendTeamMemberEndDateTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
	}

	closeExtendTeamMemberEndDateModal() {
		if (this.extendTeamMemberEndDateModalRef) {
			this.extendTeamMemberEndDateModalRef?.close();
		}
	}
	// Modals End

	// Validation Methods Starts
	isValidList(list: any) {
		return this.validationService.validateNullUndefinedEmptyList(list);
	}

	isValidString(string: any) {
		return this.validationService.validateNullUndefinedEmptyStringTrim(string);
	}

	isValidNumber(value: any): boolean {
		return typeof value === 'number' && !Number.isNaN(value);
	}

	validateTeamName(team: RmgTeam) {
		let teamName = team.teamName?.trim()?.toLowerCase();
		if (!this.isValidString(teamName)) {
			this.openAlertMessageModal("Kindly Provide a valid Team Name!!");
			return false;
		}
		if (this.isValidList(this.rmgProjectObj?.teamDetailsList)) {
			for (let t of this.rmgProjectObj.teamDetailsList) {
				if (this.isValidString(t.teamName) && t?.teamId !== team?.teamId && t.teamName?.trim()?.toLowerCase() === teamName) {
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

	validateDeleteTeams() {
		this.markDefaultProjectCompletionList = [];
		this.mappingToOtherProjectAsDefaultList = [];

		let poObj = new PoDetails();
		poObj.projectId = this.rmgProjectObj.projectId;
		poObj.selectedTeamIds = this.rmgProjectObj?.teamDetailsList.filter(team => team.isTeamSelected).map(team => team.teamId);
		poObj.activeEtmFlag = false;

		this.deleteTeamsPo = poObj;
		this.deleteTeamsPo.teamList = this.rmgProjectObj?.teamDetailsList;
		this.teamService.getTeamDetailsByTeamIdsAndProjectId(poObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {

				const rmgResourceRequirementList: any[] = response.serviceResponse || [];
				let teamMembers: RmgTeamMember[] = [];

				for (let req of rmgResourceRequirementList) {
					if (this.isValidList(req.rmgTeamMemberList)) {
						for (let member of req.rmgTeamMemberList) {
							if (member.isMemberActive != 0) {
								teamMembers.push(member);
							}
						}
					}
				}

				// having no other active projects and this is default project
				let noOtherActiveAndCurrentIsDefaultProjectEmpIds: number[] = teamMembers
					.filter(member =>
						Array.isArray(member.otherActiveProjectIds) &&
						member.otherActiveProjectIds.length === 0 &&
						member.defaultProject === true
					)
					.map(member => member.empId);

				// having other active projects and this is default project
				const otherActiveAndCurrentIsDefaultProjectEmpIds: number[] = teamMembers
					.filter(member =>
						Array.isArray(member.otherActiveProjectIds) &&
						member.otherActiveProjectIds.length > 0 &&
						member.defaultProject === true
					)
					.map(member => member.empId);

				this.markDefaultProjectCompletionList = teamMembers.filter(member => noOtherActiveAndCurrentIsDefaultProjectEmpIds.includes(member.empId));
				this.mappingToOtherProjectAsDefaultList = teamMembers.filter(member => otherActiveAndCurrentIsDefaultProjectEmpIds.includes(member.empId));

				this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);

				if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
					this.openMarkDefaultProjectCompletionModal();
					this.openMappingToOtherProjectAsDefaultModal();
				} else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length === 0) {
					this.openMarkDefaultProjectCompletionModal();
				} else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length === 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
					this.openMappingToOtherProjectAsDefaultModal();
				} else {
					this.openDeleteTeamConfirmationModal();
				}

			} else {
				this.toastService.error(response.serviceResponse || 'Something went wrong!!');
			}
		});
	}

	validateRemoveMembers(rmgCurrentTeamMemberList: RmgTeamMember[]) {
		let selectedMembers = rmgCurrentTeamMemberList.filter(member => member.isMemberSelected);
		if (!this.isValidList(selectedMembers)) {
			this.openAlertMessageModal("Please select atleast one Team Member to remove!!");
		}

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

		this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);
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

	requiresDocument(status: string): boolean {
		return status?.trim() === Status.COMPLETED || status?.trim() === Status.ON_HOLD;
	}

	validateProjectMilestone() {
		if (!this.projectMilestone.startDate) {
			this.openAlertMessageModal('Start date is required for milestone');
			return false;
		}
		if (!this.projectMilestone.endDate) {
			this.openAlertMessageModal('End date is required for milestone');
			return false;
		}
		if (this.projectMilestone.startDate > this.projectMilestone.endDate) {
			this.openAlertMessageModal('End date must be after start date for milestone');
			return false;
		}
		if (!this.projectMilestone.status || this.projectMilestone.status.trim().length === 0) {
			this.openAlertMessageModal('Status is required for milestone');
			return false;
		}
		if (this.requiresDocument(this.projectMilestone.status) && !this.file) {
			this.openAlertMessageModal('Please upload a document when completing/holding a milestone');
			return false;
		}
		if (this.rmgProjectObj.projectStatus === "Completed") {
			this.openAlertMessageModal('Project is already completed. You cannot update the milestone.');
			return false;
		}
		return true;
	}
	// ValidationMethods End

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
		this.rmgProjectObj.newTeamObj = new RmgTeam();
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

	createCurrentAndOldResourceList(rmgTeam: RmgTeam) {
		rmgTeam.newRmgTeamMember = new RmgTeamMember();
		rmgTeam.newRmgTeamMember.isNotSaved = true;
		rmgTeam.rmgCurrentTeamMemberList = rmgTeam?.rmgTeamMemberList?.filter(teamMember => {
			return teamMember.isMemberActive != 0
		}) || [];

		// Removing all the members from the list that are in the current team 
		let empIdList = rmgTeam?.rmgCurrentTeamMemberList?.map(emp => emp.empId) || [];
		this.employeeListFilteredByDept = this.employeeListFilteredByDept?.filter(emp => !empIdList.includes(emp?.empId));

		rmgTeam.rmgOldTeamMemberList = rmgTeam?.rmgTeamMemberList?.filter(teamMember => {
			return teamMember.isMemberActive == 0
		}) || [];
	}

	setRequirementResourceTypeForTeam(team: RmgTeam, isCurrentResource: boolean) {
		team.requirementType = isCurrentResource ? 'Current Resource' : 'Old Resource';
	}

	getTodaysDate() {
		const today = new Date();
		return today.toISOString().split('T')[0];
	}

	handleEmployeeProjectDetailsPageChange(event: any) {
		this.employeeExistingProjectDetailsPage = event;
	}

	mapEmployeeProjectDates(projects: any[]): any[] {
		return this.isValidList(projects) ? projects?.map(project => ({
			...project,
			startDate: project.startDate ? this.normalizeDate(project.startDate) : null,
			updatedOn: project.updatedOn ? this.normalizeDate(project.updatedOn) : null
		})) : [];
	}

	addNewObject(team: RmgTeam) {
		team.newRmgTeamMember = new RmgTeamMember();
		if (!this.isValidList(team.rmgTeamMemberList) && this.projectType !== 'TNM') {
			team.rmgTeamMemberList = [];
		}
	}

	mapProjectListToEmployees(mappingToOtherProjectAsDefaultList: any[]) {
		if (this.isValidList(mappingToOtherProjectAsDefaultList) && this.isValidList(this.projectList)) {
			for (let emp of mappingToOtherProjectAsDefaultList) {
				emp.selectedProject = new EmployeeOtherActiveProject();
				emp.projectList = this.projectList.filter(p => emp?.otherActiveProjectIds?.includes(p.projectId));
			}
		}
	}

	handleProjectMilestonePageChange(event) {
		this.projectMilestonepage = event;
	}

	getFileType(filename: string): string {
		const extension = filename.split('.').pop()?.toLowerCase();
		switch (extension) {
			case 'png': return 'image/png';
			case 'jpg':
			case 'jpeg': return 'image/jpeg';
			case 'pdf': return 'application/pdf';
			default: return 'application/octet-stream';
		}
	}

	clearSelectedFile(fileInput: HTMLInputElement) {
		this.file = null;
		fileInput.value = '';
	}

	onFileSelected(event: any): void {
		const file: File = event.target.files[0];
		this.file = null;
		this.selectedFilePreviewUrl = null;
		if (!file) {
			return;
		}

		if (file) {
			const allowedTypes = ['image/jpeg', 'image/png'];
			if (!allowedTypes.includes(file.type)) {
				alert('Invalid file type. Please upload only PDF, JPG, JPEG, or PNG files.');
				event.target.value = '';
				this.file = null;
				return;
			}

			const reader = new FileReader();
			reader.onload = () => {
				this.selectedFilePreviewUrl = reader.result as string;
			};
			reader.readAsDataURL(file);
			this.file = file;
		}
	}

	previewSelectedFile(): void {
		if (!this.file || !this.selectedFilePreviewUrl) {
			this.openAlertMessageModal('Please select a file to preview!!');
			return;
		}

		this.dialog.open(ViewImageComponent, {
			width: '80%',
			data: {
				imageUrl: this.selectedFilePreviewUrl,
				fileName: this.file.name
			}
		});
	}

	onDefaultCheckBoxChanged(event: MatCheckboxChange, member: any) {
		if (!event.checked && member.dbDefaultProject && this.isValidList(member.otherActiveProjectIds)) {
			this.mappingToOtherProjectAsDefaultList = [];
			this.mappingToOtherProjectAsDefaultList.push(member);
			this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);
			this.openMappingToOtherProjectAsDefaultModal();
		}
	}

	trackByTeamId(index: number, team: any) {
		return team.teamId ?? team.tempId ?? index;
	}

	normalizeDate(dateInput: any): Date | null {
		if (!dateInput) {
			return null;
		}

		const date = new Date(dateInput);
		if (isNaN(date.getTime())) {
			return null;
		}

		return new Date(date.getFullYear(), date.getMonth(), date.getDate());
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

	onAllTeamsCheckboxChange(): void {
		this.rmgProjectObj.isAllTeamsSelected = !this.rmgProjectObj.isAllTeamsSelected;
		this.rmgProjectObj.teamDetailsList.forEach(team => {
			team.isTeamSelected = this.rmgProjectObj.isAllTeamsSelected;
		});
		this.updateTeamActionButton();
	}

	onTeamCheckboxChange(team: RmgTeam): void {
		team.isTeamSelected = !team.isTeamSelected;
		this.updateTeamActionButton();
		this.updateAllTeamSelection();
	}

	updateAllTeamSelection(): void {
		this.rmgProjectObj.isAllTeamsSelected = this.rmgProjectObj.teamDetailsList.length > 0 && this.rmgProjectObj.teamDetailsList.filter(team => !team.isNotSaved).every(team => team.isTeamSelected);
	}

	updateTeamActionButton(): void {
		this.rmgProjectObj.isAnyTeamSelected = this.rmgProjectObj.teamDetailsList.some(team => team.isTeamSelected);
	}

	updateTeamSelection(team: RmgTeam): void {
		team.isAllMemberSelected =
			team.rmgCurrentTeamMemberList.length > 0 &&
			team.rmgCurrentTeamMemberList.filter(member => !member.isNotSaved)
				.every(member => member.isMemberSelected);
	}

	onMemberCheckboxChange(team: RmgTeam, event: any, empId: any): void {
		if (!this.isValidList(this.selectedMembersEmpId)) {
			this.selectedMembersEmpId = [];
		}
		if (event?.checked) {
			this.selectedMembersEmpId.push(empId);
		} else {
			this.selectedMembersEmpId = this.selectedMembersEmpId.filter(id => id !== empId);
		}

		this.updateTeamSelection(team);
		this.updateRemoveTeamMemberButton(team);
	}

	onAllTeamTeamMemberCheckboxChange(team: RmgTeam, event: any, teamMembers: RmgTeamMember[]): void {
		team.isAllMemberSelected = !team.isAllMemberSelected;
		if (event?.checked) {
			this.selectedMembersEmpId = teamMembers?.map(member => member.empId);
		} else {
			this.selectedMembersEmpId = [];
		}
		this.setMembersSelection(team.rmgCurrentTeamMemberList, team.isAllMemberSelected);
		this.updateRemoveTeamMemberButton(team);
	}

	updateRemoveTeamMemberButton(team: RmgTeam): void {
		team.isAnyMemberSelected = team.rmgCurrentTeamMemberList.some(member => member.isMemberSelected);
	}

	onMemberMigrationMemberCheckboxChange(member: RmgTeamMember) {
		member.isMemberSelected = !member.isMemberSelected
		this.onMemberMigrationAllMemberCheckboxChange();
	}

	onMemberMigrationAllMemberCheckboxChange() {
		this.teamMembersMigrationObj.isAllMemberSelected =
			this.teamMigrationTeamMembersList.length > 0 &&
			this.teamMigrationTeamMembersList.every(member => member.isMemberSelected);
	}

	updateAllMemberTeamMigrationCheckBox() {
		this.teamMembersMigrationObj.isAllMemberSelected = !this.teamMembersMigrationObj.isAllMemberSelected;
		this.teamMigrationTeamMembersList.forEach(m => {
			m.isMemberSelected = this.teamMembersMigrationObj.isAllMemberSelected;
		});
	}
	// Checkbox Helper Methods End

	// Table Pagination & Searching & Sorting Methods Start 
	toggleOldTeamMemberSearch() {
		this.oldTeamMemberPage = 0;
		this.isOldTeamMemberSearchEnabled = !this.isOldTeamMemberSearchEnabled;
		if (!this.isOldTeamMemberSearchEnabled) {
			this.oldTeamMemberFilters = {};
		}
	}

	searchOldTeamMember(searchData: any) {
		this.oldTeamMemberPage = 0;
		this.oldTeamMemberFilters = searchData;
	}

	oldTeamMemberPageChange(event: any) {
		this.oldTeamMemberPage = event.pageIndex + 1;
		this.oldTeamMemberPageSize = event.pageSize;
	}

	sortOldTeamMemberData(sort: Sort) {
		if (sort.active) {
			let sortParams: any[] = sort.active?.split("|");
			this.oldTeamMemberSortColumn = sortParams[0];
			this.oldTeamMemberSortColumnType = sortParams[0];
			this.oldTeamMemberSortDirection = sort.direction;
		}
	}

	toggleCurrentTeamMemberSearch() {
		this.currentTeamMemberPage = 0;
		this.isCurrentTeamMemberSearchEnabled = !this.isCurrentTeamMemberSearchEnabled;
		if (!this.isCurrentTeamMemberSearchEnabled) {
			this.currentTeamMemberFilters = {};
		}
	}

	searchCurrentTeamMember(searchData: any) {
		this.currentTeamMemberPage = 0;
		this.currentTeamMemberFilters = searchData;
	}

	currentTeamMemberPageChange(event: any) {
		this.currentTeamMemberPage = event.pageIndex + 1;
		this.currentTeamMemberPageSize = event.pageSize;
	}

	sortCurrentTeamMemberData(sort: Sort) {
		if (sort.active) {
			let sortParams: any[] = sort.active?.split("|");
			this.currentTeamMemberSortColumn = sortParams[0];
			this.currentTeamMemberSortColumnType = sortParams[0];
			this.currentTeamMemberSortDirection = sort.direction;
		}
	}

	toggleMigrateTeamMemberSearch() {
		this.migrateTeamMemberPage = 0;
		this.isMigrateTeamMemberSearchEnabled = !this.isMigrateTeamMemberSearchEnabled;
		if (!this.isMigrateTeamMemberSearchEnabled) {
			this.migrateTeamMemberFilters = {};
		}
	}

	searchMigrateTeamMember(searchData: any) {
		this.migrateTeamMemberPage = 0;
		this.migrateTeamMemberFilters = searchData;
	}

	migrateTeamMemberPageChange(event: any) {
		this.migrateTeamMemberPage = event.pageIndex + 1;
		this.migrateTeamMemberPageSize = event.pageSize;
	}

	sortMigrateTeamMemberData(sort: Sort) {
		if (sort.active) {
			let sortParams: any[] = sort.active?.split("|");
			this.migrateTeamMemberSortColumn = sortParams[0];
			this.migrateTeamMemberSortColumnType = sortParams[0];
			this.migrateTeamMemberSortDirection = sort.direction;
		}
	}

	toggleMigrateIndividualTeamMemberSearch() {
		this.migrateIndividualTeamMemberPage = 0;
		this.isMigrateIndividualTeamMemberSearchEnabled = !this.isMigrateIndividualTeamMemberSearchEnabled;
		if (!this.isMigrateIndividualTeamMemberSearchEnabled) {
			this.migrateIndividualTeamMemberFilters = {};
		}
	}

	searchMigrateIndividualTeamMember(searchData: any) {
		this.migrateIndividualTeamMemberPage = 0;
		this.migrateIndividualTeamMemberFilters = searchData;
	}

	migrateIndividualTeamMemberPageChange(event: any) {
		this.migrateIndividualTeamMemberPage = event.pageIndex + 1;
		this.migrateIndividualTeamMemberPageSize = event.pageSize;
	}

	sortMigrateIndividualTeamMemberData(sort: Sort) {
		if (sort.active) {
			let sortParams: any[] = sort.active?.split("|");
			this.migrateIndividualTeamMemberSortColumn = sortParams[0];
			this.migrateIndividualTeamMemberSortColumnType = sortParams[0];
			this.migrateIndividualTeamMemberSortDirection = sort.direction;
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

	toggleEmployeeExistingProjectDetailsSearch() {
		this.employeeExistingProjectDetailsPage = 0;
		this.isEmployeeExistingProjectDetailsSearchEnabled = !this.isEmployeeExistingProjectDetailsSearchEnabled;
		if (!this.isEmployeeExistingProjectDetailsSearchEnabled) {
			this.employeeExistingProjectDetailsFilters = {};
		}
	}

	searchEmployeeExistingProjectDetails(searchData: any) {
		this.employeeExistingProjectDetailsPage = 0;
		this.employeeExistingProjectDetailsFilters = searchData;
	}

	employeeExistingProjectDetailsPageChange(event: any) {
		this.employeeExistingProjectDetailsPage = event.pageIndex + 1;
		this.employeeExistingProjectDetailsPageSize = event.pageSize;
	}

	sortEmployeeExistingProjectDetailsData(sort: Sort) {
		if (sort.active) {
			let sortParams: any[] = sort.active?.split("|");
			this.employeeExistingProjectDetailsSortColumn = sortParams[0];
			this.employeeExistingProjectDetailsSortColumnType = sortParams[0];
			this.employeeExistingProjectDetailsSortDirection = sort.direction;
		}
	}
	// Table Pagination & Searching & Sorting Methods End 

	// Steppers Method Start
	onProjectConfigStepChange(event: any) {
		if (event) {
			this.projectConfigStepperIndex = event?.selectedIndex;
		}
		if (this.projectConfigStepperIndex === 1) {
			this.getAllTeamsByProjectId();
			if (!this.isInternalProject) {
				this.getResourceRequirementDetailsByProjectId(true);
			}
		}
	}
	// Steppers Method End

	// Team Method & APIs Start
	getAllTeamsByProjectId() {
		this.rmgProjectObj.teamDetailsList = [];
		this.getResourceRequirementCountByProjectId();
		this.teamService.getActiveTeamDetailsByProjectId(this.rmgProjectObj?.projectId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.rmgProjectObj.teamDetailsList = response.serviceResponse || [];
				this.setTeamDepartmentNames(this.rmgProjectObj?.teamDetailsList);
				this.updateAddTeamButton();
				this.updateTeamActionButton();
			}
		});
	}

	async getTeamDetailsByTeamId(team: RmgTeam) {
		team.rmgTeamMemberList = [];
		team.isAllMemberSelected = false;
		team.isAnyNewMemberAdded = false;
		this.currentTeam = team;
		this.employeeListFilteredByDept = [];
		this.projectIdPoListMap = new Map<number, PoDetails[]>();
		this.poIdTeamListMap = new Map<number, RmgTeam[]>();
		this.teamIdResourceReqListMap = new Map<number, RmgResourceRequirement[]>();

		if (!team.teamId) {
			this.openTeamDetailsModal();
			return;
		}

		if (!this.isInternalProject) {
			this.getPoDetailsByProjectId();
			this.getResourceRequirementDetailsByProjectId(false);
		}
		this.getResourceRequirementCountByProjectId();

		try {
			const response: any = await firstValueFrom(this.teamService.getTeamDetailsByTeamId(team?.teamId, this.rmgProjectObj?.projectId));

			if (response.serviceStatus === "Success") {
				team.rmgTeamMemberList = response.serviceResponse || [];
				this.addNewObject(team);
				this.employeeListFilteredByDept = this.spocList?.filter(emp =>
					team.deptIds?.includes(emp.deptId)
				);

				this.createCurrentAndOldResourceList(team);
				this.setRequirementResourceTypeForTeam(team, true);
				this.openTeamDetailsModal();
				team?.rmgCurrentTeamMemberList?.forEach(member => {
					if (this.selectedMembersEmpId?.includes(member?.empId)) {
						member.isMemberSelected = true;
					}
				});

				this.updateTeamSelection(team);
				this.updateRemoveTeamMemberButton(team);
			} else {
				this.openAlertMessageModal(response.serviceResponse || 'Something went wrong!!');
			}
		} catch (error) {
			console.error(error);
			this.openAlertMessageModal('Something went wrong!!');
		}
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

	getActiveTeamDetailsByPoId() {
		this.defaultProjectObj.teamList = [];
		this.defaultProjectObj.employeeRoles = [];
		this.defaultProjectObj.poRequirementMappingId = null;
		this.defaultProjectObj.resourceRequirementList = [];
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
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getActiveTeamDetailsByProjectId() {
		this.defaultProjectObj.teamList = [];
		this.teamService.getActiveTeamDetailsByProjectId(this.defaultProjectObj.projectId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				const teamList = response.serviceResponse || [];
				this.defaultProjectObj.teamList = teamList;
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
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

	getActiveTeamDetailsByPoIdForTeamMigration() {
		this.teamMigrationTeamList = [];
		this.teamService.getActiveTeamDetailsByPoId(this.teamMembersMigrationObj.targetPoId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.teamMigrationTeamList = response.serviceResponse || [];
				if (!this.isValidList(this.teamMigrationTeamList)) {
					this.openAlertMessageModal("No Teams found in this PO, Kindly select another PO!!");
					return;
				}
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	async getTeamDetailsByTeamIdForUpdationDefaultProject() {
		let team = this.currentTeam;
		this.employeeListFilteredByDept = [];

		if (!team.teamId) {
			this.openTeamDetailsModal();
			return;
		}

		this.getPoDetailsByProjectId();
		this.getResourceRequirementCountByProjectId();

		try {
			const response: any = await firstValueFrom(this.teamService.getTeamDetailsByTeamId(team?.teamId, this.rmgProjectObj?.projectId));
			if (response.serviceStatus === "Success") {
				team.rmgTeamMemberList = response.serviceResponse || [];

				this.addNewObject(team);
				this.employeeListFilteredByDept = this.spocList?.filter(emp => team.deptIds?.includes(emp.deptId));
				this.createCurrentAndOldResourceList(team);
				this.setRequirementResourceTypeForTeam(team, true);
				this.openTeamDetailsModal();

				team?.rmgCurrentTeamMemberList?.map(member => {
					if (this.selectedMembersEmpId?.includes(member?.empId)) {
						member.isMemberSelected = true;
					}
				});

				this.updateTeamSelection(team);
				this.updateRemoveTeamMemberButton(team);
				this.validateRemoveMembers(team?.rmgCurrentTeamMemberList);
			} else {
				this.openAlertMessageModal(response.serviceResponse || 'Something went wrong!!');
			}
		} catch (error) {
			console.error(error);
			this.openAlertMessageModal('Something went wrong!!');
		}
	}

	addNewTeam() {
		const team = this.rmgProjectObj?.newTeamObj;
		const isTeamNameValid = this.validateTeamName(team);
		if (!isTeamNameValid) {
			return;
		}
		if (!this.isValidList(team.deptIds)) {
			this.openAlertMessageModal("Kindly Select atleast one department!!");
			return;
		}
		team.isNotSaved = true;
		this.rmgProjectObj?.teamDetailsList.push(team);
		this.rmgProjectObj.newTeamObj = new RmgTeam();
		this.updateAddTeamButton();
	}

	addOrUpdateTeamDetails(isUpdate: boolean) {
		let teamList = isUpdate ? this.rmgProjectObj?.teamDetailsList : this.rmgProjectObj?.teamDetailsList.filter(team => team.isNotSaved);;
		if (!this.isValidList(teamList)) {
			this.openAlertMessageModal('No Teams found in this PO!!');
			return;
		}

		for (let index = 0; index < teamList?.length; index++) {
			let team = teamList[index];
			if (!this.isValidString(team.teamName)) {
				this.openAlertMessageModal(`Team Name cannot be null for Team ${index + 1}!!`);
				return;
			}
			for (let t of this.rmgProjectObj.teamDetailsList) {
				if (this.isValidString(t.teamName) && t?.teamId !== team?.teamId && t.teamName?.trim()?.toLowerCase() === team.teamName?.trim()?.toLowerCase()) {
					this.openAlertMessageModal(`Team names must be unique. Duplicate team name found: '${team.teamName}'!!`);
					return false;
				}
			}
			if (!this.isValidList(team.deptIds)) {
				this.openAlertMessageModal(`Atleast select one Department for Team - '${team.teamName}' !!`);
				return;
			}
		}

		let updateTeamPoDetails = new PoDetails();
		updateTeamPoDetails.teamList = teamList
		updateTeamPoDetails.updatedBy = this.currentUser.empId;
		updateTeamPoDetails.isHod = this.isHOD;
		updateTeamPoDetails.isupdate = isUpdate;
		updateTeamPoDetails.projectId = this.rmgProjectObj.projectId;
		updateTeamPoDetails.projectType = this.projectType;
		this.teamService.addOrUpdateTeamDetails(updateTeamPoDetails).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.getAllTeamsByProjectId();
				this.toastService.success(response.serviceResponse);
			} else {
				this.toastService.error(response.serviceResponse);
			}
		});
	}

	updateAddTeamButton(): void {
		this.rmgProjectObj.isAnyNewTeamAdded = this.rmgProjectObj.teamDetailsList.some(team => team.isNotSaved);
	}

	removeTeamFromTeamList(team: RmgTeam) {
		if (!this.isValidList(this.rmgProjectObj?.teamDetailsList)) {
			this.rmgProjectObj.teamDetailsList = [];
		}
		this.rmgProjectObj.teamDetailsList = this.rmgProjectObj?.teamDetailsList.filter(t => t !== team);
		this.updateAddTeamButton();
	}

	deleteSelectedTeams() {
		if (!this.teamEndDate || this.teamEndDate == undefined || this.teamEndDate == null) {
			this.openAlertMessageModal("Please provide End date!!");
			return;
		}
		this.deleteTeamsPo.teamList = this.deleteTeamsPo?.teamList?.filter(team => team.isTeamSelected)
			.map(team => {
				team.endDate = this.normalizeDate(this.teamEndDate);
				team.updatedBy = this.currentUser.empId;
				return team;
			});
		this.teamService.deleteSelectedTeams(this.deleteTeamsPo).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.getAllTeamsByProjectId();
				this.toastService.success(response.serviceResponse);
			} else {
				this.toastService.error(response.serviceResponse);
			}
			this.closeDeleteTeamConfirmationModal();
		});
	}

	migrateTeam() {
		if (!this.teamMembersMigrationObj.targetProjectId || !this.isValidNumber(this.teamMembersMigrationObj.targetProjectId)) {
			this.openAlertMessageModal("Kindly Select target Project!!");
			return;
		}
		if (!this.teamMembersMigrationObj.targetPoId || !this.isValidNumber(this.teamMembersMigrationObj.targetPoId)) {
			this.openAlertMessageModal("Kindly Select PO!!");
			return;
		}
		if (this.teamMembersMigrationObj.mergeTeam && (!this.teamMembersMigrationObj.targetTeamId || !this.isValidNumber(this.teamMembersMigrationObj.targetTeamId))) {
			this.openAlertMessageModal("Kindly Select an existing Team in which to merge the Selected Team(s)!!");
			return;
		}

		this.teamMembersMigrationObj.sourceProjectId = this.rmgProjectObj.projectId;
		this.teamMembersMigrationObj.currentUserEmpId = this.currentUser.empId;
		this.teamService.migrateTeam(this.teamMembersMigrationObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.toastService.success(response.serviceResponse);
				this.closeMigrateTeamModal();
				this.getAllTeamsByProjectId();
			} else {
				this.toastService.error(response.serviceResponse || 'Something went wrong!!');
			}
		});
	}
	// Team Method & APIs End

	// PO List Method & APIs Start
	getPoDetailsByProjectId() {
		this.poDetailsList = [];
		this.resourceManagementService.getActivePoDetailsByProjectId(this.rmgProjectObj.projectId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.poDetailsList = response.serviceResponse || [];
				this.poDetailsList.forEach(po => {
					const poStartDate = this.normalizeDate(po.poStartDate);
					const poEndDate = this.normalizeDate(po.poEndDate);
					if (poStartDate && poStartDate != undefined && poStartDate != null && poEndDate && poEndDate != undefined && poEndDate != null) {
						po.poTitle = po.poNo + ' | PO Start Date - ' + poStartDate + ' | PO End Date - ' + poEndDate;
					} else {
						po.poTitle = po.poNo;
					}
				});
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong, unable to fetch PO Details List!!");
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
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
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
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getActivePoDetailsByProjectIdForTeamMigration() {
		this.teamMigrationPoDetailsList = [];
		this.teamMigrationTeamMembersList?.forEach(emp => { emp.poDetailsList = [] });
		this.resourceManagementService.getActivePoDetailsByProjectId(this.teamMembersMigrationObj.targetProjectId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.teamMigrationPoDetailsList = response.serviceResponse || [];
				this.teamMigrationTeamMembersList?.forEach(emp => {
					emp.poDetailsList = this.teamMigrationPoDetailsList;
				});
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
	}
	// PO List Method & APIs End

	//  Team Members Method & APIs Start
	getActiveTeamMembersByTeamIdsAndProjectId() {
		this.teamMigrationTeamMembersList = [];
		let poObj = new PoDetails();
		poObj.activeEtmFlag = true;
		poObj.projectId = this.rmgProjectObj.projectId;
		poObj.selectedTeamIds = this.rmgProjectObj?.teamDetailsList.filter(team => team.isTeamSelected).map(team => team.teamId);
		this.teamService.getTeamDetailsByTeamIdsAndProjectId(poObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.teamMigrationTeamMembersList = response.serviceResponse || [];
				this.migrateTeamModalRef = this.modalService?.open(this.migrateTeamTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	addNewTeamMemberToCurrentResourceList() {
		if (!this.isInternalProject) {
			if (!this.currentTeam.newRmgTeamMember.poId || !this.isValidNumber(this.currentTeam.newRmgTeamMember.poId)) {
				this.openAlertMessageModal("Kindly Select a PO!!");
				return;
			}

			const selectedPo = this.poDetailsList?.find(po => po?.poId === this.currentTeam.newRmgTeamMember?.poId);
			if (!selectedPo || selectedPo == undefined || selectedPo == null) {
				this.openAlertMessageModal("Selected PO not found in the List!!");
				return;
			}

			if (this.projectType === 'TNM' && (!this.currentTeam.newRmgTeamMember.roleId || !this.isValidNumber(this.currentTeam.newRmgTeamMember.roleId))) {
				this.openAlertMessageModal("Kindly Select a Requirement Role!!");
				return;
			}
		}

		if (!this.currentTeam.newRmgTeamMember.startDate || this.currentTeam.newRmgTeamMember.startDate == undefined || this.currentTeam.newRmgTeamMember.startDate == null) {
			this.openAlertMessageModal("Kindly Provide Start Date!!");
			return;
		}

		const projectStartDate = this.normalizeDate(this.rmgProjectObj.startDate);
		const memberStartDate = this.normalizeDate(this.currentTeam.newRmgTeamMember.startDate);
		if (memberStartDate < projectStartDate) {
			this.openUpdateProjectStartDateErrorModal();
			return;
		}

		if (!this.isValidList(this.currentTeam.rmgCurrentTeamMemberList)) {
			this.currentTeam.rmgCurrentTeamMemberList = [];
		}

		this.currentTeam.newRmgTeamMember.isNotSaved = true;
		this.currentTeam.newRmgTeamMember.teamId = this.currentTeam.teamId;
		this.currentTeam.newRmgTeamMember.memberName = this.employeeListFilteredByDept?.find(emp => emp?.empId === this.currentTeam.newRmgTeamMember?.empId)?.name;
		this.currentTeam.newRmgTeamMember.poNo = this.poDetailsList?.find(po => po?.poId === this.currentTeam.newRmgTeamMember?.poId)?.poNo;
		this.currentTeam.newRmgTeamMember.poRequirementMappingId = this.resourceRequirementList?.find(req => req?.roleId === this.currentTeam.newRmgTeamMember?.roleId)?.poRequirementMappingId;
		this.currentTeam.newRmgTeamMember.displayRequirement = this.resourceRequirementList?.find(req => req?.poRequirementMappingId === this.currentTeam.newRmgTeamMember?.poRequirementMappingId)?.displayRequirement;
		this.employeeListFilteredByDept = this.employeeListFilteredByDept.filter(emp => emp.empId !== this.currentTeam.newRmgTeamMember?.empId);
		this.currentTeam.rmgCurrentTeamMemberList.push(this.currentTeam.newRmgTeamMember);
		this.currentTeam.rmgCurrentTeamMemberList = [...this.currentTeam.rmgCurrentTeamMemberList];
		this.currentTeam.newRmgTeamMember = new RmgTeamMember();
		this.updateAddTeamMemberButton(this.currentTeam);
		this.closeAddNewMemberModal();
	}

	addOrUpdateTeamMembers(team: RmgTeam, isUpdate: boolean) {
		if (!this.isValidList(team.rmgCurrentTeamMemberList)) {
			this.openAlertMessageModal(isUpdate ? "No Team Members to update in current Resource!!" : "No New Members added in current Resource!!");
			return;
		}

		for (let i = 0; i < team?.rmgCurrentTeamMemberList?.length; i++) {
			let member = team?.rmgCurrentTeamMemberList[i];
			if (!member?.empId || member.empId == undefined || member.empId == null) {
				this.openAlertMessageModal(`Select an Employee for Member # ${i + 1}`);
				return;
			}
			if (!this.isValidString(member?.memberName)) {
				this.openAlertMessageModal(`Member Name cannot be null for Member # ${i + 1}`);
				return;
			}
			if (!this.isValidList(member?.employeeRoles)) {
				this.openAlertMessageModal(`Select atleast one Employee Role for Member # ${i + 1}`);
				return;
			}
			if (!member.startDate || member.startDate == undefined || member.startDate == null) {
				this.openAlertMessageModal(`Kindly Provide Start Date for Member # ${i + 1}`);
				return;
			}

			if (member.endDate && member.endDate != undefined && member.endDate != null && member.startDate > member.endDate) {
				this.openAlertMessageModal(`End Date cannot be less than Start Date for Member # ${i + 1}`);
				return;
			}

			team.rmgCurrentTeamMemberList[i].startDate = member.startDate ? moment(member.startDate).format('YYYY-MM-DDTHH:mm:ss') : null;
			team.rmgCurrentTeamMemberList[i].endDate = member.endDate ? moment(member.endDate).format('YYYY-MM-DDTHH:mm:ss') : null;
			team.rmgCurrentTeamMemberList[i].isShadow = team?.rmgCurrentTeamMemberList[i].isShadow != null && team?.rmgCurrentTeamMemberList[i].isShadow ? 1 : 0;
		}

		team.projectId = this.rmgProjectObj.projectId;
		team.updatedBy = this.currentUser.empId;
		team.isupdate = isUpdate;
		team.clientName = this.rmgProjectObj?.clientName;
		team.rmgTeamMemberList = team.rmgCurrentTeamMemberList;
		team.projectType = this.projectType;
		this.teamService.addOrUpdateTeamMembers(team).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.getTeamDetailsByTeamId(team);
				this.toastService.success(response.serviceResponse);
			} else {
				this.toastService.error(response.serviceResponse);
			}
		});
	}

	removeTeamMemberFromCurrentResourceList(team: RmgTeam, rmgTeamMember: RmgTeamMember) {
		if (!this.isValidList(team.rmgCurrentTeamMemberList)) {
			team.rmgCurrentTeamMemberList = [];
		}

		team.rmgCurrentTeamMemberList = team.rmgCurrentTeamMemberList.filter(member => member.empId !== rmgTeamMember.empId);
		this.employeeListFilteredByDept = this.spocList.filter(emp => team.deptIds?.includes(emp.deptId));
		this.updateAddTeamMemberButton(team);
	}

	removeTeamMembersFromProject() {
		if (!this.isValidList(this.selectedRemoveMembers)) {
			this.openAlertMessageModal("Kindly Select atleast one member to Remove!!");
			return;
		}

		if (!this.membersEndDate || this.membersEndDate == undefined || this.membersEndDate == null) {
			this.openAlertMessageModal("Please provide End date!!");
			return;
		}

		const teamMembersEndDate = this.normalizeDate(this.membersEndDate);

		for (let member of this.selectedRemoveMembers) {
			const memberStartDate = this.normalizeDate(member.startDate);
			if (memberStartDate > teamMembersEndDate) {
				this.openAlertMessageModal("Member End date cannot be less then Member Start date!!");
				return;
			}
		}

		let rmgTeam = new RmgTeam();
		rmgTeam.projectId = this.rmgProjectObj.projectId;
		rmgTeam.teamId = this.currentTeam?.teamId;
		rmgTeam.updatedBy = this.currentUser?.empId;
		rmgTeam.clientName = this.rmgProjectObj?.clientName;
		rmgTeam.rmgTeamMemberList = this.selectedRemoveMembers;
		rmgTeam.projectType = this.projectType;
		rmgTeam.isCustomEndDate = false; //this.customEndDate;
		rmgTeam.endDate = this.normalizeDate(this.membersEndDate);

		this.teamService.removeTeamMembersFromProject(rmgTeam).pipe(first()).subscribe(async (response: any) => {
			if (response.serviceStatus == "Success") {
				this.toastService.success(response.serviceResponse);
				await this.getTeamDetailsByTeamId(this.currentTeam);
			} else {
				this.toastService.error(response.serviceResponse);
			}
		});
	}

	updateTeamMembersEndDate(currentTeam: RmgTeam) {

	}

	migrateTeamMembers(employee: any) {

		if (!employee.targetProjectId || !this.isValidNumber(employee.targetProjectId)) {
			this.openAlertMessageModal("Kindly Select target Project!!");
			return;
		}
		if (!employee.targetPoId || !this.isValidNumber(employee.targetPoId)) {
			this.openAlertMessageModal("Kindly Select PO!!");
			return;
		}
		let tempTeamMembersMigrationObj = new MigrateTeams();
		tempTeamMembersMigrationObj.sourceProjectId = this.rmgProjectObj.projectId;
		tempTeamMembersMigrationObj.currentUserEmpId = this.currentUser.empId;
		tempTeamMembersMigrationObj.empIds = employee.empId ? [employee.empId] : [];
		this.teamService.migrateTeamMembers(tempTeamMembersMigrationObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.toastService.success(response.serviceResponse);
				this.closeMigrateTeamModal();
				this.openMigrateTeamModal();
			} else {
				this.toastService.error(response.serviceResponse || 'Something went wrong!!');
			}
		});
	}

	migrateTeamMembersBulk() {

		let bulkEmpIds = this.teamMigrationTeamMembersList?.map(employee => {
			if (employee.isMemberSelected) {
				return employee.empId;
			};
		})
		if (!this.isValidList(bulkEmpIds)) {
			this.openAlertMessageModal("Kindly Select atleast one member to migrate!!");
			return;
		}
		if (!this.teamMembersMigrationObj.targetProjectId || !this.isValidNumber(this.teamMembersMigrationObj.targetProjectId)) {
			this.openAlertMessageModal("Kindly Select target Project!!");
			return;
		}
		if (!this.teamMembersMigrationObj.targetPoId || !this.isValidNumber(this.teamMembersMigrationObj.targetPoId)) {
			this.openAlertMessageModal("Kindly Select PO!!");
			return;
		}

		this.teamMembersMigrationObj.sourceProjectId = this.rmgProjectObj.projectId;
		this.teamMembersMigrationObj.currentUserEmpId = this.currentUser.empId;
		this.teamMembersMigrationObj.empIds = bulkEmpIds ? bulkEmpIds : [];
		this.teamService.migrateTeamMembers(this.teamMembersMigrationObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.toastService.success(response.serviceResponse);
				this.closeMigrateTeamModal();
				this.openMigrateTeamModal();
			} else {
				this.toastService.error(response.serviceResponse || 'Something went wrong!!');
			}
		});
	}

	getAddNewTeamMemberButtonTitle(newRmgTeamMember: any): string {
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

	isAddNewTeamMemberButtonDisabled(newRmgTeamMember: RmgTeamMember): boolean {
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

	updateAddTeamMemberButton(team: RmgTeam): void {
		team.isAnyNewMemberAdded = team.rmgCurrentTeamMemberList.some(member => member.isNotSaved);
	}

	onMemberStartDateChange(event: MatDatepickerInputEvent<Date>, member: RmgTeamMember) {
		const selectedDate = event.value;
		const projectStartDate = this.normalizeDate(this.rmgProjectObj.startDate);
		if (selectedDate < projectStartDate) {
			member.startDate = member.dbStartDate;
			this.openUpdateProjectStartDateErrorModal();
			return;
		}

		member.projectId = this.rmgProjectObj.projectId;
		this.teamService.validateEmployeeTimesheetFilledToChangeStartDate(member).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.openAlertMessageModal(response.serviceResponse);
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong, unable to validate timesheet filled count at the moment for the updated start date!!");
			}
		});
	}
	// Team Members Method & APIs End

	// Resource Requirements Method & APIs Start
	getResourceRequirementByPoId(poId: any) {
		this.resourceRequirementList = [];
		this.resourceManagementService.getResourceRequirementByPoId(poId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.resourceRequirementList = response.serviceResponse || [];
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getResourceRequirementByTeamIdForEmployee(member: RmgTeamMember) {
		member.poRequirementMappingId = null;
		member.resourceRequirementList = [];
		if (this.teamIdResourceReqListMap.has(member.teamId)) {
			member.resourceRequirementList = this.teamIdResourceReqListMap.get(member.teamId);
			return;
		}

		this.resourceManagementService.getResourceRequirementByTeamId(member.teamId).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				member.resourceRequirementList = response.serviceResponse || [];
				this.teamIdResourceReqListMap.set(member.teamId, member.resourceRequirementList);
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getResourceRequirementByTeamId() {
		this.defaultProjectObj.poRequirementMappingId = null;
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
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getResourceRequirementCountByProjectId() {
		this.resourceManagementService.getResourceRequirementCountByProjectId(this.rmgProjectObj?.projectId, this.projectType).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				const resp = response.serviceResponse;
				this.rmgProjectObj.totalRequirements = resp.totalRequirements || 0;
				this.rmgProjectObj.assignedApproved = resp.assignedApproved || 0;
				this.rmgProjectObj.assignedPending = resp.assignedPending || 0;
				this.rmgProjectObj.difference = resp.difference || 0;
			} else {
				this.rmgProjectObj.totalRequirements = 0;
				this.rmgProjectObj.assignedApproved = 0;
				this.rmgProjectObj.assignedPending = 0;
				this.rmgProjectObj.difference = 0;
			}
		});
	}

	getResourceRequirementDetailsByProjectId(currentActivePOs: boolean) {
		if (currentActivePOs) {
			this.currentRmgProjectResourceRequirementList = [];
		} else {
			this.allRmgProjectResourceRequirementList = [];
		}
		this.resourceManagementService.getResourceRequirementDetailsByProjectId(this.rmgProjectObj?.projectId, this.projectType, currentActivePOs).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				if (currentActivePOs) {
					this.currentRmgProjectResourceRequirementList = response.serviceResponse || [];
				} else {
					this.allRmgProjectResourceRequirementList = response.serviceResponse || [];
				}
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getResourceRequirementDetailsByProjectIdForTeamMemberMigration(projectId: any) {
		if (this.projectType !== 'TNM') {
			return
		}
		this.teamMigrationResourceRequirementList = [];
		this.resourceManagementService.getResourceRequirementDetailsByProjectId(projectId, this.projectType, false).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.teamMigrationResourceRequirementList = response.serviceResponse || [];
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	getResourceRequirementDetailsByProjectIdForTeamMemberMigrationAndForEmployee(employee: any, projectId: any) {
		if (this.projectType !== 'TNM') {
			return
		}
		employee.resourceRequirementList = [];
		this.resourceManagementService.getResourceRequirementDetailsByProjectId(projectId, this.projectType, false).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				employee.resourceRequirementList = response.serviceResponse || [];
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}
	// Resource Requirements Method & APIs End

	// Projects Method & APIs Start
	getProjectConfigurationDetailsByProjectId(project: any) {
		this.rmgProjectObj = null;
		this.resourceManagementService.getProjectConfigurationDetailsByProjectId(project?.projectId, this.isAllProjects).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.rmgProjectObj = response.serviceResponse;
				this.rmgProjectObj.state = this.isValidString(this.rmgProjectObj.state) ? this.rmgProjectObj.state : 'NA';
			} else {
				this.openAlertMessageModal(response.serviceResponse);
			}
		});
	}

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

	getEmployeeExistingProjectDetailsByEmpId(empId: any) {
		let requirement: any;
		this.employeeExistingProjectDetails = [];
		this.employeeExistingProjectDetailsPage = 1;

		this.projectService.getEmployeeExistingProjectDetailsByEmpId(empId).pipe(first()).subscribe((response: any) => {
			if (response?.serviceStatus !== "Success") {
				this.openAlertMessageModal(response?.serviceResponse || "Something went wrong!!");
				return;
			}

			this.employeeExistingProjectDetails = this.mapEmployeeProjectDates(response.serviceResponse || []);

			if (!this.isValidList(this.employeeExistingProjectDetails)) {
				requirement.newRmgTeamMember.billableType = "NewMember";
				return;
			}
			const billableType = this.employeeExistingProjectDetails[0]?.billableType;
			this.openEmployeeExistingProjectDetailsModal();
			if (billableType === "TNM") {
				this.openAlertMessageModal("This Employee is already mapped to TNM project. Cannot be added to another Project or Team!!");
			} else {
				requirement.newRmgTeamMember.billableType = "NewMember";
			}
		});
	}

	updateProjectStartDate() {
		if (!this.projectNewStartDate || this.projectNewStartDate == undefined || this.projectNewStartDate == null) {
			this.openAlertMessageModal("Kindly provide new Project Start Date!!");
			return;
		}
		let projectObj: Project = new Project();
		projectObj.projectId = this.rmgProjectObj.projectId;
		projectObj.startDate = moment(this.normalizeDate(this.projectNewStartDate)).format('YYYY-MM-DD');
		projectObj.updatedBy = this.currentUser.empId;

		this.projectService.updateProjectStartDate(projectObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.rmgProjectObj.startDate = this.normalizeDate(this.projectNewStartDate);
				this.closeUpdateProjectStartDateConfirmationModal();
				this.openAlertMessageModal(response.serviceResponse);
			} else {
				this.openAlertMessageModal(response.serviceResponse || "Something went wrong!");
			}
		});
	}

	async updateMappingToOtherProjectAsDefault(employee: RmgTeamMember) {
		if (!employee?.selectedProject || !employee?.selectedProject?.projectId) {
			this.openAlertMessageModal("Please Select Default Project");
			return;
		}
		employee.selectedProject.updatedBy = this.currentUser.empId;

		try {
			const response: any = await firstValueFrom(this.projectService.updateMappingToOtherProjectAsDefault(employee.selectedProject));
			if (response.serviceStatus == "Success") {
				this.closeMappingToOtherProjectAsDefaultModal();
				this.toastService.success(response.serviceResponse);
				await this.getTeamDetailsByTeamId(this.currentTeam);
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!");
			}
		} catch (error) {
			this.toastService.error("Something went wrong!");
		}
	}

	updateDefaultProjectCompletion(employee: RmgTeamMember, isBulk: boolean) {
		if (!this.isValidString(employee?.projectType) || !employee.projectId || !employee.teamId) {
			this.openAlertMessageModal('Project Type, Project, Team, and Employee Role must be selected!!');
			return;
		}
		if (!this.isValidList(employee.employeeRoles)) {
			this.openAlertMessageModal('Employee Role must be selected!!');
			return;
		}

		let tempRmgTeamMember = new RmgTeamMember();
		tempRmgTeamMember.empId = employee?.empId;
		tempRmgTeamMember.updatedBy = this.currentUser.empId;
		tempRmgTeamMember.projectId = employee.projectId;
		tempRmgTeamMember.teamId = employee.teamId;
		tempRmgTeamMember.poId = employee?.poId;
		tempRmgTeamMember.employeeRoles = employee.employeeRoles;
		tempRmgTeamMember.roleId = employee.roleId;
		tempRmgTeamMember.poRequirementMappingId = employee.poRequirementMappingId;
		tempRmgTeamMember.clientName = this.rmgProjectObj?.clientName;
		tempRmgTeamMember.selectedEmpIds = isBulk ? this.markDefaultProjectCompletionList?.map(member => member.empId) ?? [] : employee?.empId ? [employee.empId] : [];
		tempRmgTeamMember.projectType = this.projectType;

		this.teamService.updateDefaultProjectCompletion(tempRmgTeamMember).pipe(first()).subscribe(async (response: any) => {
			if (response.serviceStatus == "Success") {
				this.closeMarkDefaultProjectCompletionModal();
				await this.getTeamDetailsByTeamIdForUpdationDefaultProject();
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	updateDefaultProjectCompletionBulk() {
		if (!this.isValidString(this.defaultProjectObj?.projectType) || !this.defaultProjectObj.projectId || !this.defaultProjectObj.teamId) {
			this.openAlertMessageModal('Project Type, Project, Team, and Employee Role must be selected!!');
			return;
		}
		if (!this.isValidList(this.defaultProjectObj.employeeRoles)) {
			this.openAlertMessageModal('Employee Role must be selected!!');
			return;
		}

		let tempRmgTeamMember = new RmgTeamMember();
		tempRmgTeamMember.empId = this.defaultProjectObj?.empId;
		tempRmgTeamMember.updatedBy = this.currentUser.empId;
		tempRmgTeamMember.projectId = this.defaultProjectObj.projectId;
		tempRmgTeamMember.teamId = this.defaultProjectObj.teamId;
		tempRmgTeamMember.poId = this.defaultProjectObj?.poId;
		tempRmgTeamMember.employeeRoles = this.defaultProjectObj.employeeRoles;
		tempRmgTeamMember.roleId = this.defaultProjectObj.roleId;
		tempRmgTeamMember.poRequirementMappingId = this.defaultProjectObj.poRequirementMappingId;
		tempRmgTeamMember.clientName = this.rmgProjectObj?.clientName;
		tempRmgTeamMember.selectedEmpIds = this.markDefaultProjectCompletionList?.map(member => member.empId) || [];
		tempRmgTeamMember.projectType = this.projectType;

		this.teamService.updateDefaultProjectCompletion(tempRmgTeamMember).pipe(first()).subscribe(async (response: any) => {
			if (response.serviceStatus == "Success") {
				this.closeMarkDefaultProjectCompletionModal();
				this.closeTeamDetailsModal();
				this.getTeamDetailsByTeamIdForUpdationDefaultProject();
			} else {
				this.toastService.error(response.serviceResponse || "Something went wrong!!");
			}
		});
	}

	deleteEmployeeProjectResourceMapping() {
		if (!this.employeeProjectEndDate || this.employeeProjectEndDate == undefined || this.employeeProjectEndDate == null) {
			this.openAlertMessageModal("Please provide End date!!");
			return;
		}

		if (this.normalizeDate(this.deleteEmployeeExistingProjectMappingObj?.startDate) > this.normalizeDate(this.employeeProjectEndDate)) {
			this.openAlertMessageModal("Member End date cannot be less then Member Start date!!");
			return;
		}

		this.deleteEmployeeExistingProjectMappingObj.isCustomDate = this.employeeProjectEndDateType === 'Custom';
		this.deleteEmployeeExistingProjectMappingObj.rescEndDate = this.normalizeDate(this.employeeProjectEndDate);
		this.deleteEmployeeExistingProjectMappingObj.updatedBy = this.currentUser.empId;
		this.deleteEmployeeExistingProjectMappingObj.rescRemovedBy = this.currentUser.empId;
		this.projectService.updateEmployeeProjectMappingAsInActive(this.deleteEmployeeExistingProjectMappingObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus == "Success") {
				this.getEmployeeExistingProjectDetailsByEmpId(this.deleteEmployeeExistingProjectMappingObj.empId);
				this.toastService.success(response.serviceResponse);
			} else {
				this.toastService.error(response.serviceResponse);
			}
		});
		this.closeEmployeeExistingProjectDetailsModal();
		this.closeDeleteEmployeeFromExistingProjectModal();
	}

	saveProjectInformation() {
		if (!this.isValidList(this.rmgProjectObj?.departmentIds)) {
			this.openAlertMessageModal("Please Select atleast one Department!!");
			return;
		}
		if (!this.isValidList(this.rmgProjectObj?.projectManagerIds)) {
			this.openAlertMessageModal("Please Select atleast one Project Manager!!");
			return;
		}
		this.rmgProjectObj.updatedBy = this.currentUser.empId;
		this.projectService.saveProjectInformation(this.rmgProjectObj).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.openAlertMessageModal(response.serviceResponse);
				this.getProjectConfigurationDetailsByProjectId(this.rmgProjectObj);
			} else {
				this.openAlertMessageModal(response.serviceResponse);
			}
		});
	}

	getTeamOrPoBasedOnProjectTypeForEmployee(employee: RmgTeamMember) {
		employee.poId = null;
		employee.poDetailsList = [];
		employee.teamId = null;
		employee.teamList = [];
		employee.employeeRoles = [];
		employee.poRequirementMappingId = null;
		employee.resourceRequirementList = [];
		if (employee.projectType !== 'Bench') {
			this.getActivePoDetailsByProjectIdForEmployee(employee);
			return;
		}
		this.getActiveTeamDetailsByProjectIdForEmployee(employee);
	}

	getTeamOrPoBasedOnProjectType() {
		this.defaultProjectObj.poId = null;
		this.defaultProjectObj.poDetailsList = [];
		this.defaultProjectObj.teamId = null;
		this.defaultProjectObj.teamList = [];
		this.defaultProjectObj.employeeRoles = [];
		this.defaultProjectObj.poRequirementMappingId = null;
		this.defaultProjectObj.resourceRequirementList = [];
		if (this.defaultProjectObj.projectType !== 'Bench') {
			this.getActivePoDetailsByProjectId();
			return;
		}
		this.getActiveTeamDetailsByProjectId();
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
	// Projects Method & APIs End

	// FC Milestone Method & APIs Start
	getProjectMilestones() {
		this.fcProjectMilestoneList = [];
		let projectObjTemp = new Project();
		projectObjTemp.poProjectId = this.rmgProjectObj.projectId;
		this.projectService.getAllProjectFCLineItemListByProjectId(projectObjTemp).pipe(first()).subscribe({
			next: (response: any) => {
				if (response.serviceStatus === "Success") {
					this.fcProjectMilestoneList = response.serviceResponse;
				} else {
					console.error("Error fetching milestones:", response.serviceResponse);
					this.openAlertMessageModal(response.serviceResponse);
				}
			},
			error: (err) => {
				this.openAlertMessageModal("An unexpected error occurred while fetching milestones.");
			}
		});
	}

	viewFiles(mileStoneId: any) {
		this.projectService.getMilestoneById(mileStoneId).subscribe((res: any) => {
			if (res.documentContent && res.documentName) {
				const fileType = this.getFileType(res.documentName);
				const imageDataUrl = `data:${fileType};base64,${res.documentContent}`;
				console.log("image url" + imageDataUrl)
				this.dialog.open(ViewImageComponent, { width: '80%', data: { imageUrl: imageDataUrl, fileName: res.documentName } });
			} else {
				this.openAlertMessageModal("Image is not available!!")
			}
		},
			(error) => {
				this.openAlertMessageModal(error.error.message);
			}
		)
	}

	updateMilestoneChanges() {
		let isValid = this.validateProjectMilestone();
		if (!isValid) {
			return;
		}

		this.projectMilestone.updatedBy = this.currentUser.empId;
		this.projectMilestone.updatedOn = new Date();
		const formData = new FormData();
		formData.append('dto', new Blob([JSON.stringify(this.projectMilestone)], { type: 'application/json' }));
		if (this.file) {
			formData.append('file', this.file);
		}

		this.projectService.updateMilestoneById(formData).pipe(first()).subscribe({
			next: (response: any) => {
				if (response.serviceStatus === "Success") {
					this.file = null;
					const index = this.fcProjectMilestoneList.findIndex(m => m.id === this.projectMilestone.id);
					if (index > -1) {
						this.fcProjectMilestoneList[index] = { ...this.projectMilestone };
					}
					this.calculatePoStatus();
				} else {
					this.openAlertMessageModal(response.serviceResponse || "Failed to update milestone.");
				}
				this.getProjectMilestones();
			},
			error: (err) => {
				this.openAlertMessageModal("Error updating milestone: " + err.message);
			}
		});
		this.closeUpdateProjectMilestoneModal();
	}

	calculatePoStatus() {
		let notStarted = 0, completed = 0, hold = 0, inProgress = 0;
		let lineItemList = this.fcProjectMilestoneList.filter(milestone => milestone.lineItemId == this.projectMilestone.lineItemId);

		for (let m of lineItemList) {
			if (m.status == Status.IN_PROGRESS) {
				inProgress++;
			} else if (m.status == Status.COMPLETED) {
				completed++;
			} else if (m.status == Status.ON_HOLD) {
				hold++;
			} else if (m.status == Status.NOT_STARTED) {
				notStarted++;
			}
		}

		if (inProgress > 0) {
			this.projectMilestone.lineItemStatus = Status.IN_PROGRESS
		} else if (hold > 0) {
			this.projectMilestone.lineItemStatus = Status.ON_HOLD
		} else if (notStarted > 0 && notStarted < lineItemList?.length) {
			this.projectMilestone.lineItemStatus = Status.IN_PROGRESS
		} else if (completed > 0) {
			this.projectMilestone.lineItemStatus = Status.COMPLETED
		}

		const allCompleted =
			this.fcProjectMilestoneList.length > 0 &&
			this.fcProjectMilestoneList.every(m => m.status === Status.COMPLETED);

		if (allCompleted) {
			this.openMarkAsCompleteFCProjectModal();
		} else {

			this.updateMilestoneChanges();
			this.closeUpdateProjectMilestoneModal();
		}
	}

	markAsCompleteFCProject() {
		let project = new Project();
		project.id = this.rmgProjectObj.poProjectId;
		project.projectId = this.rmgProjectObj.projectId;
		project.projectName = this.rmgProjectObj.projectName;
		project.projectStatus = "Completed";
		project.status = this.rmgProjectObj.status;
		project.projectType = this.rmgProjectObj.projectType;
		project.poProjectType = this.rmgProjectObj.poProjectType;
		project.internalProjectType = this.rmgProjectObj.internalProjectType;
		project.clientState = this.rmgProjectObj.state;

		project.updatedBy = this.currentUser.empId;
		project.projectCompletionDate = new Date();
		project.deptName = '';
		this.resourceManagementService.completionDateOfProject(project).pipe(first()).subscribe((response: any) => {
			if (response.serviceStatus === "Success") {
				this.openAlertMessageModal("Since all milestones are completed, the project is marked as complete.");
			} else {
				this.openAlertMessageModal(response.serviceResponse);
			}
		},
			(error) => {
				this.openAlertMessageModal("Something went wrong while completing the project.");
			}
		);
	}
	// FC Milestone Method & APIs End
}