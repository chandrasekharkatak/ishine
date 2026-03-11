import { Component, EventEmitter, Input, OnInit, TemplateRef, ViewChild, Output, ViewContainerRef } from '@angular/core';
import { SafeResourceUrl } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Project } from 'src/app/models/project';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectService } from 'src/app/services/project.service';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { TeamService } from 'src/app/services/team.service';
import { ValidationService } from 'src/app/services/validation.service';
import { User } from 'src/app/models/user';
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
import { RmgMemberEndDate } from 'src/app/models/rmgMemberEndDate';
import { MatTabChangeEvent } from '@angular/material/tabs';
import { MatStepper } from '@angular/material/stepper';
import { GlobalRightDrawerService } from 'src/app/services/global-right-drawer.service';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { EmployeeProjectTimesheetDto } from 'src/app/models/employeeProjectTimesheetDto';
import { provideMomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { MomentDateAdapter, MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';

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
    selector: 'app-rmg-project-config',
    templateUrl: './rmg-project-config.component.html',
    styleUrl: './rmg-project-config.component.css',
    providers: [
        { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_MOMENT_DATE_ADAPTER_OPTIONS] },
        { provide: MAT_DATE_FORMATS, useValue: MY_DATE_FORMATS },
    ]
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
    @Output() closeProjectConfigurationModal = new EventEmitter<any>();
    @Output() openProjectCompletionDatePicker = new EventEmitter<any>();

    @ViewChild('project_config_stepper') projectConfigStepper!: MatStepper;
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
    @ViewChild("team_member_details_preview") teamMemberDetailsPreviewTemplateRef: TemplateRef<any>;
    @ViewChild("existing_employee_project_timesheet_info") existingEmployeeProjectTimesheetInfoTemplateRef: TemplateRef<any>;
    @ViewChild("project_gap_message") projectGapMessageTemplateRef: TemplateRef<any>;
    @ViewChild("shadow_resource_mapping") shadowResourceMappingTemplateRef: TemplateRef<any>;

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
    teamMemberDetailsPreviewModalRef: NgbModalRef;
    existingEmployeeProjectTimesheetInfoModalRef: NgbModalRef;
    projectGapMessageModalRef: NgbModalRef;
    shadowResourceMappingModalRef: NgbModalRef;

    currentUser: User;
    userMapping: any = {};
    projectConfigStepperIndex: number = 1;
    alertMessage: string = '';
    projectType: string = '';
    employeeProjectEndDateType: 'PO' | 'Custom' = 'Custom';
    membersEndDateType: 'PO' | 'Custom' = 'Custom';
    currentTab: 'Project' | 'Milestones' = 'Project';
    defaultProjectMappingActionType: 'DELETE_TEAM' | 'REMOVE_MEMBERS' | 'PROJECT_COMPLETION' = 'REMOVE_MEMBERS';
    todayTimestamp: any;
    employeeExistingProjectEmpName: any;
    employeeExistingProjectEmploymentId: any;
    projectConfigStepperErrorMessage: string = '';
    roleFilterActionLabel: 'Show Active PO Roles' | 'Show All PO Roles' = 'Show Active PO Roles';

    // Objects
    currentTeam: RmgTeam = new RmgTeam();
    defaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
    teamMembersMigrationObj: MigrateTeams = new MigrateTeams();
    deleteEmployeeExistingProjectMappingObj: RmgTeamMember;
    deleteTeamsPo: PoDetails = new PoDetails();
    shadowResourceMappingMember: RmgTeamMember = new RmgTeamMember();

    // Arrays
    fixedCostTypes = ['fixed cost'];
    allBillableProjectTypes = ['tnm', 'fixed cost', 'monitoring'];
    allNonBillableProjectTypes = ['internalrndproducts', 'bench', 'internal'];
    employeeRoles: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
    projectTypes: any[] = ['Bench', 'Other'];
    employeeDisplayArr: any[] = ['name', 'employmentId']

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
    filteredActiveResourceRequirement: RmgResourceRequirement[] = [];
    selectedRemoveMembers: RmgTeamMember[] = [];
    mappingToOtherProjectAsDefaultList: RmgTeamMember[] = [];
    markDefaultProjectCompletionList: RmgTeamMember[] = [];
    teamMigrationTeamMembersList: RmgTeamMember[] = [];
    cloneMemberMappingList: RmgTeamMember[] = [];
    existingEmployeeProjectTimesheetEntries: EmployeeProjectTimesheetDto[] = [];

    // Maps for caching
    projectIdPoListMap = new Map<number, PoDetails[]>();
    poIdTeamListMap = new Map<number, RmgTeam[]>();
    teamIdResourceReqListMap = new Map<number, RmgResourceRequirement[]>();

    // Flags 
    isHOD: boolean = false;
    isInternalProject: boolean = false;
    isMarkDefaultProjectCompletionBulk: boolean = true;
    isBulkTeamMemberMigration: boolean = true;
    isTeamDetailsForm: boolean = false;
    isProjectManagerValid: boolean = false;
    isProjectOverlapping: boolean = false;
    isOnboardingAsShadow: boolean = false;

    // Dates
    membersEndDate: any;
    employeeProjectEndDate: any;
    projectNewStartDate: any
    teamEndDate: any;
    gapStartDate: any;
    gapEndDate: any;
    shadowResourceMappingMemberEndDate: any;
    shadowResourceMappingMemberStartDate: any;

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
    rmgOldTeamMemberColumnList: any[] = ['blank', 'employementId', 'memberName', 'memberDepartment', 'poNo', 'displayRequirement', 'employeeRole', 'empTeamDepartmentName', 'blank', 'blank', 'blank'];
    rmgOldTeamMemberColumnListForInternal = ['blank', 'employementId', 'memberName', 'memberDepartment', 'employeeRole', 'empTeamDepartmentName', 'blank', 'blank', 'blank'];

    // Current Team Member
    isCurrentTeamMemberSearchEnabled: boolean = false;
    currentTeamMemberPage = 1;
    currentTeamMemberPageSize = 10;
    currentTeamMemberSortColumn: string = '';
    currentTeamMemberSortColumnType: string = '';
    currentTeamMemberSortDirection: string = 'asc';
    currentTeamMemberFilters: any = {};
    currentTeamMemberSearchOnEnter: boolean = true;
    rmgCurrentTeamMemberColumnList: any[] = ['blank', 'blank', 'blank', 'employementId', 'memberName', 'poNo', 'displayRequirement', 'blank', 'empTeamDepartmentName', 'blank', 'blank', 'blank', 'blank'];
    rmgCurrentTeamMemberColumnListForInternal: any[] = ['blank', 'blank', 'blank', 'employementId', 'memberName', 'blank', 'empTeamDepartmentName', 'blank', 'blank', 'blank', 'blank'];
    rmgCurrentTeamMemberColumnListPreview: any[] = ['blank', 'employementId', 'memberName', 'poNo', 'displayRequirement', 'blank', 'empTeamDepartmentName', 'blank', 'blank', 'blank', 'blank'];
    rmgCurrentTeamMemberColumnListForInternalPreview: any[] = ['blank', 'employementId', 'memberName', 'blank', 'empTeamDepartmentName', 'blank', 'blank', 'blank', 'blank'];


    // Migrate Team Member
    isMigrateTeamMemberSearchEnabled: boolean = false;
    migrateTeamMemberPage = 1;
    migrateTeamMemberPageSize = 10;
    migrateTeamMemberSortColumn: string = '';
    migrateTeamMemberSortColumnType: string = '';
    migrateTeamMemberSortDirection: string = 'asc';
    migrateTeamMemberFilters: any = {};
    migrateTeamMemberSearchOnEnter: boolean = true;
    migrateTeamMemberColumnListBulk: any[] = ['blank', 'employementId', 'memberName', 'teamName', 'employeeRole', 'empTeamDepartmentName', 'poNo', 'displayRequirement'];

    // Migrate Individual Team Member
    isMigrateIndividualTeamMemberSearchEnabled: boolean = false;
    migrateIndividualTeamMemberPage = 1;
    migrateIndividualTeamMemberPageSize = 10;
    migrateIndividualTeamMemberSortColumn: string = '';
    migrateIndividualTeamMemberSortColumnType: string = '';
    migrateIndividualTeamMemberSortDirection: string = 'asc';
    migrateIndividualTeamMemberFilters: any = {};
    migrateIndividualTeamMemberSearchOnEnter: boolean = true;
    migrateIndividualTeamMemberColumnList: any[] = ['employementId', 'memberName', 'teamName', 'employeeRole', 'poNo', 'displayRequirement', 'blank', 'blank', 'blank', 'blank'];

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

    // Mark Default Project Completion
    isMarkDefaultProjectCompletionSearchEnabled: boolean = false;
    markDefaultProjectCompletionPage = 1;
    markDefaultProjectCompletionPageSize = 10;
    markDefaultProjectCompletionSortColumn: string = '';
    markDefaultProjectCompletionSortColumnType: string = '';
    markDefaultProjectCompletionSortDirection: string = 'asc';
    markDefaultProjectCompletionFilters: any = {};
    markDefaultProjectCompletionSearchOnEnter: boolean = true;
    markDefaultProjectCompletionColumnListBulk: any[] = ['employementId', 'memberName', 'memberDepartment', 'jobRoleName', 'empTeamDepartmentName', 'billableType', 'prevExp', 'currentExp', 'totalExp'];

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
        public validationService: ValidationService,
        private readonly toastService: ToastService,
        private readonly modalService: NgbModal,
        private readonly teamService: TeamService,
        private readonly resourceManagementService: ResourceManagementService,
        private readonly authenticationService: AuthenticationService,
        private readonly projectService: ProjectService,
        private readonly dialog: MatDialog,
        private drawerService: GlobalRightDrawerService
    ) {
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

    ngOnInit(): void {
        this.isHOD = ['HOD', 'SuperAdmin', 'Superadmin', 'Super Admin'].includes(this.currentUser.employeeRole);
        this.isInternalProject = this.rmgProjectObj.internalProjectType != undefined && this.rmgProjectObj.internalProjectType != null && ['internal', 'internalrndproducts', 'bench'].includes(this.rmgProjectObj.internalProjectType?.trim()?.toLowerCase());
        this.projectConfigStepperIndex = this.isProjectPreview ? 2 : 0; // Stepper Default to Project Information
        this.validateProjectManagerIds();
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
        this.migrateTeamModalRef = this.modalService?.open(this.migrateTeamTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
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

    openRemoveMembersModal() {
        this.membersEndDate = null;
        this.removeMembersConfirmationModalRef = this.modalService?.open(this.removeMembersConfirmationTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
    }

    closeRemoveMembersModal() {
        if (this.removeMembersConfirmationModalRef) {
            this.removeMembersConfirmationModalRef?.close();
        }
    }

    openMarkDefaultProjectCompletionModal(actionType: any) {
        this.defaultProjectMappingActionType = actionType;
        this.defaultProjectObj = new SetDefaultProjectObj();
        this.defaultProjectObj.projectType = 'Bench'
        this.getActiveProjectList();
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
        // this.mappingToOtherProjectAsDefaultModalRef = this.modalService?.open(this.mappingToOtherProjectAsDefaultTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
    }

    closeMappingToOtherProjectAsDefaultModal() {
        if (this.isValidList(this.mappingToOtherProjectAsDefaultList)) {
            this.mappingToOtherProjectAsDefaultList.forEach(member => {
                member.defaultProject = member.dbDefaultProject;
            });
        }
        this.drawerService?.close();
        // if (this.mappingToOtherProjectAsDefaultModalRef) {
        //     this.mappingToOtherProjectAsDefaultModalRef?.close();
        // }
    }

    openEmployeeExistingProjectDetailsModal() {
        this.drawerService.open(this.employeeExistingProjectDetailsTemplateRef);
    }

    closeEmployeeExistingProjectDetailsModal() {
        this.drawerService?.close();
    }

    openDeleteEmployeeFromExistingProjectModal(employee: any) {
        this.employeeProjectEndDate = null;
        if (this.employeeProjectEndDateType !== 'Custom') {
            this.employeeProjectEndDate = employee.endDate;
        }
        this.deleteEmployeeExistingProjectMappingObj = employee;
        this.deleteEmployeeExistingProjectMappingObj.isInternalProject = this.deleteEmployeeExistingProjectMappingObj?.internalProjectType != undefined && this.deleteEmployeeExistingProjectMappingObj?.internalProjectType != null && ['internal', 'internalrndproducts', 'bench'].includes(this.deleteEmployeeExistingProjectMappingObj?.internalProjectType?.trim()?.toLowerCase());
        this.deleteEmployeeFromExistingProjectModalRef = this.modalService?.open(this.deleteEmployeeFromExistingProjectTemplateRef, { modalDialogClass: 'modal-sm', backdrop: 'static', keyboard: false });
    }

    closeDeleteEmployeeFromExistingProjectModal() {
        if (this.deleteEmployeeFromExistingProjectModalRef) {
            this.deleteEmployeeFromExistingProjectModalRef?.close();
        }
    }

    openDeleteTeamConfirmationModal(actionType: any) {
        this.defaultProjectMappingActionType = actionType;
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
        this.currentTeam.newRmgTeamMember = member;
        this.currentTeam.newRmgTeamMember.empId = member.empId;
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

    async openTeamMemberDetailsPreviewModal(team) {
        this.resetTeamDetailsForm();
        await this.getTeamDetailsByTeamId(team);
        this.teamMemberDetailsPreviewModalRef = this.modalService.open(this.teamMemberDetailsPreviewTemplateRef, { modalDialogClass: 'modal-lg no-modal-content', backdrop: 'static', keyboard: false });
    }

    closeTeamMemberDetailsPreviewModal() {
        if (this.teamMemberDetailsPreviewModalRef) {
            this.teamMemberDetailsPreviewModalRef?.close();
        }
    }

    openExistingEmployeeProjectTimesheetInfoModal() {
        this.existingEmployeeProjectTimesheetInfoModalRef = this.modalService.open(this.existingEmployeeProjectTimesheetInfoTemplateRef, { modalDialogClass: 'modal-lg', backdrop: 'static', keyboard: false });
    }

    closeExistingEmployeeProjectTimesheetInfoModal() {
        if (this.existingEmployeeProjectTimesheetInfoModalRef) {
            this.existingEmployeeProjectTimesheetInfoModalRef?.close();
        }
    }

    openProjectGapMessageModal() {
        this.projectGapMessageModalRef = this.modalService.open(this.projectGapMessageTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
    }

    closeProjectGapMessageModal() {
        if (this.projectGapMessageModalRef) {
            this.projectGapMessageModalRef?.close();
        }
    }

    openShadowResourceMappingModal(member: RmgTeamMember) {
        this.shadowResourceMappingMember = member;
        this.shadowResourceMappingMemberEndDate = null;
        this.shadowResourceMappingMemberStartDate = null
        this.shadowResourceMappingModalRef = this.modalService.open(this.shadowResourceMappingTemplateRef, { modalDialogClass: 'modal-md', backdrop: 'static', keyboard: false });
    }

    closeShadowResourceMappingModal() {
        this.shadowResourceMappingMember.isShadow = this.shadowResourceMappingMember.dbIsShadow;
        if (this.shadowResourceMappingModalRef) {
            this.shadowResourceMappingModalRef?.close();
        }
    }
    // Modals End

    // Validation Methods Starts
    isValidList(list: any) {
        return Array.isArray(list) && this.validationService.validateNullUndefinedEmptyList(list);
    }

    isValidString(string: any) {
        return this.validationService.validateNullUndefinedEmptyStringTrim(string);
    }

    isValidNumber(value: any): boolean {
        return typeof value === 'number' && !Number.isNaN(value);
    }

    validateTeamName(team: RmgTeam) {
        let teamName = team.teamName?.trim()?.toLowerCase()?.replace(/\s+/g, '') || '';
        if (!this.isValidString(teamName)) {
            this.openAlertMessageModal("Kindly Provide a valid Team Name!!");
            return false;
        }
        if (this.isValidList(this.rmgProjectObj?.teamDetailsList)) {
            for (let t of this.rmgProjectObj.teamDetailsList) {
                if (this.isValidString(t.teamName) && t?.teamId !== team?.teamId && t.teamName?.trim()?.toLowerCase()?.replace(/\s+/g, '') || '' === teamName) {
                    this.openAlertMessageModal("Team Name already exists!!");
                    return false;
                }
            }
        }
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
                let teamMembers: RmgTeamMember[] = response.serviceResponse || [];

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

                this.resetDefaultProjectCompletion(this.markDefaultProjectCompletionList);
                this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);

                if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
                    this.openMarkDefaultProjectCompletionModal('DELETE_TEAM');
                    this.openMappingToOtherProjectAsDefaultModal('DELETE_TEAM');
                } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length === 0) {
                    this.openMarkDefaultProjectCompletionModal('DELETE_TEAM');
                } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length === 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
                    this.openMappingToOtherProjectAsDefaultModal('DELETE_TEAM');
                } else {
                    this.openDeleteTeamConfirmationModal('DELETE_TEAM');
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

    validateProjectManagerIds(): void {
        const { projectManagerIds, dbProjectManagerIds } = this.rmgProjectObj || {};
        this.isProjectManagerValid = true;
        this.projectConfigStepperErrorMessage = '';
        if (!this.isValidList(projectManagerIds) || !projectManagerIds?.length) {
            return this.setProjectManagerError('Kindly assign at least one Project Manager before proceeding to the next step.');
        }
        if (!this.validationService.areArraysEqual(projectManagerIds, dbProjectManagerIds)) {
            return this.setProjectManagerError(
                'Kindly save the updated Project Manager information before proceeding to the next step.'
            );
        }
    }

    private setProjectManagerError(message: string): void {
        this.isProjectManagerValid = false;
        this.projectConfigStepperErrorMessage = message;
    }
    // Validation Methods End

    // Helpers Start
    // callCloseProjectConfiguration() {
    //     this.closeProjectConfiguration.emit();
    // }

    setProjectType(): void {
        if (this.isValidString(this.rmgProjectObj.poProjectType)) {
            this.projectType = this.rmgProjectObj.poProjectType;
        } else if (this.isValidString(this.rmgProjectObj.internalProjectType)) {
            this.projectType = this.rmgProjectObj.internalProjectType;
        } else {
            this.projectType = 'NA';
        }
    }

    getProjectType(project: any): string {
        if (this.isValidString(project?.poProjectType)) {
            return project.poProjectType;
        } else if (this.isValidString(project?.internalProjectType)) {
            return project.internalProjectType;
        } else {
            return 'NA';
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
            return teamMember.isMemberActive != 0 || moment(teamMember.startDate).format('YYYY-MM-DD') >= moment(new Date()).format('YYYY-MM-DD')
        }) || [];

        if (!this.isValidList(rmgTeam.rmgCurrentTeamMemberList)) {
            this.toggleAddNewMember(rmgTeam);
        }

        // Removing all the members from the list that are in the current team 
        let empIdList = rmgTeam?.rmgCurrentTeamMemberList?.map(emp => emp.empId) || [];
        this.employeeListFilteredByDept = this.employeeListFilteredByDept?.filter(emp => !empIdList.includes(emp?.empId));

        rmgTeam.rmgOldTeamMemberList = rmgTeam?.rmgTeamMemberList?.filter(teamMember => {
            return teamMember.isMemberActive == 0 && moment(teamMember.startDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD')
        }) || [];
    }

    setRequirementResourceTypeForTeam(team: RmgTeam, isCurrentResource: boolean) {
        team.requirementType = isCurrentResource ? 'Current Resource' : 'Old Resource';
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
        if (!this.isValidList(team.rmgTeamMemberList)) {
            team.rmgTeamMemberList = [];
        }
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

    onDefaultCheckBoxChanged(event: MatCheckboxChange, member: RmgTeamMember) {
        if (!event.checked && member.dbDefaultProject && this.isValidList(member.otherActiveProjectIds)) {
            this.mappingToOtherProjectAsDefaultList = [];
            this.mappingToOtherProjectAsDefaultList.push(member);
            this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);
            this.openMappingToOtherProjectAsDefaultModal('REMOVE_MEMBERS');
        }
    }

    onShadowCheckBoxChanged(event: MatCheckboxChange, member: RmgTeamMember) {
        this.isOnboardingAsShadow = event.checked
        this.openShadowResourceMappingModal(member);
    }

    trackByTeamId(index: number, team: any) {
        return team.teamId ?? team.tempId ?? index;
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

    getTodaysDate() {
        return moment().startOf('day').format('YYYY-MM-DDTHH:mm:ss');
    }

    onTabChange(event: MatTabChangeEvent) {
        if (event.index === 1) {
            this.currentTab = 'Milestones';
            this.getProjectMilestones();
        } else {
            this.currentTab = 'Project';
        }
    }

    get resourceStats() {
        return [
            { label: 'Requirements', value: this.rmgProjectObj?.totalRequirements ?? 0, class: 'primary', show: this.projectType === 'TNM' },
            { label: 'Assigned', value: (this.rmgProjectObj?.assignedApproved ?? 0) + (this.rmgProjectObj?.assignedPending ?? 0), class: 'assigned', show: true },
            { label: 'Approved', value: this.rmgProjectObj?.assignedApproved ?? 0, class: 'approved', show: true },
            { label: 'Pending', value: this.rmgProjectObj?.assignedPending ?? 0, class: 'pending', show: true },
            { label: 'Difference', value: this.rmgProjectObj?.difference ?? 0, class: 'danger', show: this.projectType === 'TNM' }
        ];
    }

    toggleAddNewTeam() {
        this.rmgProjectObj.addNewTeamToggle = !this.rmgProjectObj.addNewTeamToggle;
        if (this.rmgProjectObj.addNewTeamToggle) {
            this.rmgProjectObj.newTeamObj = new RmgTeam();
        }
    }

    toggleAddNewMember(currentTeam: RmgTeam) {
        currentTeam.addNewTeamMemberToggle = !currentTeam.addNewTeamMemberToggle;
        if (currentTeam.addNewTeamMemberToggle) {
            currentTeam.newRmgTeamMember = new RmgTeamMember();
        }
    }

    async showTeamDetails(team: RmgTeam) {
        this.isTeamDetailsForm = true;
        this.resetTeamDetailsForm();
        await this.getTeamDetailsByTeamId(team);
    }

    async hideTeamDetails() {
        this.isTeamDetailsForm = false;
        this.currentTab = 'Project';
        await this.getAllTeamsByProjectId();
        if (!this.isInternalProject) {
            this.getResourceRequirementDetailsByProjectId(false);
        }
        if (this.projectConfigStepperIndex != 2) {
            this.projectConfigStepperIndex = 1;
        }
    }

    onResourceTabChange(event: MatTabChangeEvent) {
        if (event.index === 1) {
            this.currentTeam.requirementType = 'Old Resource';
        } else {
            this.currentTeam.requirementType = 'Current Resource';
        }
    }

    resetTeamDetailsForm() {
        this.selectedMembersEmpId = [];
    }

    filterActiveResourceRequirement() {
        if (this.roleFilterActionLabel === 'Show All PO Roles') {
            this.roleFilterActionLabel = "Show Active PO Roles";
            this.filteredActiveResourceRequirement = [...this.resourceRequirementList];
        } else {
            this.roleFilterActionLabel = "Show All PO Roles";
            this.filteredActiveResourceRequirement = this.resourceRequirementList.filter(r => !r.isExpired);
        }
    }

    filterActiveResourceRequirementEmployeeSpecific(member:RmgTeamMember) {
        if (member.roleFilterActionLabel === 'Show All PO Roles') {
            member.roleFilterActionLabel = "Show Active PO Roles";
            member.filteredActiveResourceRequirement = [...member.resourceRequirementList];
        } else {
            member.roleFilterActionLabel = "Show All PO Roles";
            member.filteredActiveResourceRequirement = member.resourceRequirementList.filter(r => !r.isExpired);
        }
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

    toggleMarkDefaultProjectCompletionSearch() {
        this.markDefaultProjectCompletionPage = 0;
        this.isMarkDefaultProjectCompletionSearchEnabled = !this.isMarkDefaultProjectCompletionSearchEnabled;
        if (!this.isMarkDefaultProjectCompletionSearchEnabled) {
            this.markDefaultProjectCompletionFilters = {};
        }
    }

    searchMarkDefaultProjectCompletion(searchData: any) {
        this.markDefaultProjectCompletionPage = 0;
        this.markDefaultProjectCompletionFilters = searchData;
    }

    markDefaultProjectCompletionPageChange(event: any) {
        this.markDefaultProjectCompletionPage = event.pageIndex + 1;
        this.markDefaultProjectCompletionPageSize = event.pageSize;
    }

    sortMarkDefaultProjectCompletionData(sort: Sort) {
        if (sort.active) {
            let sortParams: any[] = sort.active?.split("|");
            this.markDefaultProjectCompletionSortColumn = sortParams[0];
            this.markDefaultProjectCompletionSortColumnType = sortParams[0];
            this.markDefaultProjectCompletionSortDirection = sort.direction;
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
    // Table Pagination & Searching & Sorting Methods End 

    // Steppers Method Start
    onProjectConfigStepChange(event: StepperSelectionEvent) {
        if (!event) {
            return;
        }
        if (event?.selectedIndex === 1) {
            this.getAllTeamsByProjectId();
            if (!this.isInternalProject) {
                this.getResourceRequirementDetailsByProjectId(false);
            }
        }
    }
    // Steppers Method End

    // Team Method & APIs Start
    async getAllTeamsByProjectId() {
        this.rmgProjectObj.teamDetailsList = [];
        this.getResourceRequirementCountByProjectId();
        try {
            const response: any = await firstValueFrom(this.teamService.getActiveTeamDetailsByProjectId(this.rmgProjectObj?.projectId));
            if (response.serviceStatus === "Success") {
                this.rmgProjectObj.teamDetailsList = response.serviceResponse || [];
                if (!this.isValidList(this.rmgProjectObj.teamDetailsList)) {
                    this.toggleAddNewTeam();
                }
                this.setTeamDepartmentNames(this.rmgProjectObj?.teamDetailsList);
                this.updateAddTeamButton();
                this.updateTeamActionButton();
            } else {
                this.openAlertMessageModal(response.serviceResponse || 'Something went wrong!!');
            }
        } catch (error) {
            console.error(error);
            this.openAlertMessageModal('Something went wrong!!');
        }
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

        if (!this.isInternalProject) {
            this.getPoDetailsByProjectId();
            this.getResourceRequirementDetailsByProjectId(false);
        }
        this.getResourceRequirementCountByProjectId();
        try {
            const response: any = await firstValueFrom(this.teamService.getTeamDetailsByTeamId(team?.teamId, this.rmgProjectObj?.projectId));
            this.addNewObject(team);

            if (response.serviceStatus === "Success") {
                team.rmgTeamMemberList = response.serviceResponse || [];
                this.employeeListFilteredByDept = this.spocList?.filter(emp =>
                    team.deptIds?.includes(emp.deptId)
                );

                this.createCurrentAndOldResourceList(team);
                this.setRequirementResourceTypeForTeam(team, true);

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
                this.updateTeamSelection(team);
                team?.rmgCurrentTeamMemberList?.map(member => {
                    if (this.selectedMembersEmpId?.includes(member?.empId)) {
                        member.isMemberSelected = true;
                    }
                });
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

    addOrUpdateTeamDetails(isUpdate: boolean) {
        let teamList = isUpdate ? this.rmgProjectObj?.teamDetailsList : this.rmgProjectObj?.teamDetailsList.filter(team => team.isNotSaved);
        if (!this.isValidList(teamList)) {
            this.openAlertMessageModal('No Newly Added Teams found in this Project!!');
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

    saveNewTeamDetails() {
        const team = this.rmgProjectObj?.newTeamObj;
        if (!team || team == undefined || team == null) {
            this.openAlertMessageModal('New Team Cannot be Null!!');
            return;
        }

        const isTeamNameValid = this.validateTeamName(team);
        if (!isTeamNameValid) {
            return;
        }

        if (!this.isValidList(team.deptIds)) {
            this.openAlertMessageModal("Kindly Select atleast one department!!");
            return;
        }

        let newTeamPoDetails = new PoDetails();
        newTeamPoDetails.teamList = [team];
        newTeamPoDetails.updatedBy = this.currentUser.empId;
        newTeamPoDetails.isHod = this.isHOD;
        newTeamPoDetails.isupdate = false;
        newTeamPoDetails.projectId = this.rmgProjectObj.projectId;
        newTeamPoDetails.projectType = this.projectType;
        this.teamService.addOrUpdateTeamDetails(newTeamPoDetails).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.getAllTeamsByProjectId();
                this.rmgProjectObj.newTeamObj = new RmgTeam();
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

    async getActivePoDetailsByProjectIdForTeamMigration() {
        this.teamMigrationPoDetailsList = [];
        this.teamMigrationTeamMembersList?.forEach(emp => { emp.poDetailsList = [] });
        const project = this.projectList.find(p => p?.projectId === this.teamMembersMigrationObj?.targetProjectId);

        this.teamMembersMigrationObj.projectType = this.getProjectType(project);
        this.teamMembersMigrationObj.isInternalProject = project?.internalProjectType != undefined && project?.internalProjectType != null && ['internal', 'internalrndproducts', 'bench'].includes(project?.internalProjectType?.trim()?.toLowerCase());

        await this.getActiveTeamMembersByTeamIdsAndProjectId();
        if (this.migrateTeamModalRef) {
            this.migrateTeamModalRef?.close();
        }
        this.migrateTeamModalRef = this.modalService?.open(this.migrateTeamTemplateRef, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });

        if (this.teamMembersMigrationObj.isInternalProject) {
            return;
        }
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
    async getActiveTeamMembersByTeamIdsAndProjectId() {
        this.teamMigrationTeamMembersList = [];
        let poObj = new PoDetails();
        poObj.activeEtmFlag = true;
        poObj.projectId = this.rmgProjectObj.projectId;
        poObj.selectedTeamIds = this.rmgProjectObj?.teamDetailsList.filter(team => team.isTeamSelected).map(team => team.teamId);
        try {
            const response: any = await firstValueFrom(this.teamService.getTeamDetailsByTeamIdsAndProjectId(poObj));
            if (response.serviceStatus === "Success") {
                this.teamMigrationTeamMembersList = response.serviceResponse || [];
            } else {
                this.openAlertMessageModal(response.serviceResponse || "Something went wrong!!");
            }
        } catch (error) {
            this.toastService.error("Something went wrong!");
        }
    }

    async saveNewTeamMemberDetails() {
        let newTeamMember = this.currentTeam.newRmgTeamMember;
        if (!newTeamMember?.empId || newTeamMember.empId == undefined || newTeamMember.empId == null) {
            this.openAlertMessageModal(`Kindly Select an Employee!!`);
            return;
        }
        if (this.projectType === 'TNM' || this.employeeExistingProjectDetails[0]?.billableType === 'TNM') {
            const existingProjectFlag = await this.getEmployeeExistingProjectDetailsByEmpId(this.currentTeam.newRmgTeamMember.empId, false, this.rmgProjectObj.projectId);
            if (!existingProjectFlag) {
                return;
            }
        }

        if (!this.isInternalProject) {
            if (!newTeamMember.poId || !this.isValidNumber(newTeamMember.poId)) {
                this.openAlertMessageModal("Kindly Select a PO!!");
                return;
            }
            const selectedPo = this.poDetailsList?.find(po => po?.poId === newTeamMember?.poId);
            if (!selectedPo || selectedPo == undefined || selectedPo == null) {
                this.openAlertMessageModal("Selected PO not found in the List!!");
                return;
            }
            if (this.projectType === 'TNM' && (!newTeamMember.roleId || !this.isValidNumber(newTeamMember.roleId))) {
                this.openAlertMessageModal("Kindly Select a Requirement Role!!");
                return;
            }
        }
        if (!this.isValidList(newTeamMember?.employeeRoles)) {
            this.openAlertMessageModal(`Select atleast one Employee Role!!`);
            return;
        }
        if (!newTeamMember.startDate || newTeamMember.startDate == undefined || newTeamMember.startDate == null) {
            this.openAlertMessageModal("Kindly Provide Start Date!!");
            return;
        }
        if (newTeamMember.startDate !== undefined && newTeamMember.startDate !== null) {
            const flag: boolean = await this.validateEmployeeProjectStartDate(newTeamMember, this.rmgProjectObj.projectId, this.rmgProjectObj.startDate, this.projectType);
            if (!flag) {
                return;
            }
        }

        if (!this.isValidList(this.currentTeam.rmgCurrentTeamMemberList)) {
            this.currentTeam.rmgCurrentTeamMemberList = [];
        }
        newTeamMember.startDate = newTeamMember.startDate ? moment(newTeamMember.startDate).format('YYYY-MM-DDTHH:mm:ss') : null;
        newTeamMember.endDate = newTeamMember.endDate ? moment(newTeamMember.endDate).format('YYYY-MM-DDTHH:mm:ss') : null;
        newTeamMember.isShadow = newTeamMember?.isShadow != null && newTeamMember.isShadow ? 1 : 0;
        newTeamMember.teamId = this.currentTeam.teamId;
        newTeamMember.poRequirementMappingId = this.resourceRequirementList?.find(req => req?.roleId === newTeamMember?.roleId)?.poRequirementMappingId;

        let team: RmgTeam = new RmgTeam();
        team.isupdate = false;
        team.projectId = this.rmgProjectObj.projectId;
        team.teamId = this.currentTeam.teamId;
        team.updatedBy = this.currentUser.empId;
        team.clientName = this.rmgProjectObj?.clientName;
        team.rmgTeamMemberList = [newTeamMember];
        team.projectType = this.projectType;

        this.teamService.addOrUpdateTeamMembers(team).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.getTeamDetailsByTeamId(this.currentTeam);
                this.toastService.success(response.serviceResponse);
                team.deptIds = response?.serviceResponse1;
                if (this.isValidList(team.deptIds)) {
                    const departmentNames = this.departmentsList
                        .filter(x => team.deptIds?.includes(x.deptId))
                        .map(x => x.name).join(', ');
                    team.departmentNames = departmentNames
                }
            } else {
                this.toastService.error(response.serviceResponse);
            }
        });
    }

    async addOrUpdateTeamMembers(team: RmgTeam, isUpdate: boolean) {
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
            if (member.startDate !== undefined && member.startDate !== null
                && (!member.dbStartDate || member.dbStartDate == undefined || member.dbStartDate == null)) {
                const flag: boolean = await this.validateEmployeeProjectStartDate(member, this.rmgProjectObj.projectId, this.rmgProjectObj.startDate, this.projectType);
                if (!flag) {
                    return;
                }
            }
            if (member.startDate !== undefined && member.startDate !== null
                && member.dbStartDate !== undefined && member.dbStartDate !== null
                && this.normalizeDate(member.dbStartDate) !== this.normalizeDate(member.startDate)) {
                const flag: boolean = await this.validateEmployeeProjectStartDate(member, this.rmgProjectObj.projectId, this.rmgProjectObj.startDate, this.projectType);
                if (!flag) {
                    return;
                }
            }
            if (member.endDate && member.endDate != undefined && member.endDate != null && member.startDate > member.endDate) {
                this.openAlertMessageModal(`End Date cannot be less than Start Date for Member # ${i + 1}`);
                return;
            }
            member.startDate = member.startDate ? moment(member.startDate).format('YYYY-MM-DDTHH:mm:ss') : null;
            member.endDate = member.endDate ? moment(member.endDate).format('YYYY-MM-DDTHH:mm:ss') : null;
            member.isShadow = team?.rmgCurrentTeamMemberList[i].isShadow != null && team?.rmgCurrentTeamMemberList[i].isShadow ? 1 : 0;
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

                team.deptIds = response?.serviceResponse1;
                if (this.isValidList(team.deptIds)) {
                    const departmentNames = this.departmentsList
                        .filter(x => team.deptIds?.includes(x.deptId))
                        .map(x => x.name).join(', ');
                    team.departmentNames = departmentNames
                }
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
                this.openAlertMessageModal(`Member End date cannot be less then Member Start date for ${member.employementId}!!`);
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
        this.closeRemoveMembersModal();
    }

    extendTeamMembersEndDate(currentTeam: RmgTeam) {
        const isAnyMemberEndDateChanged = currentTeam?.rmgOldTeamMemberList.some(member => { return this.normalizeDate(member.endDate) != this.normalizeDate(member.dbEndDate) });
        if (!isAnyMemberEndDateChanged) {
            this.openAlertMessageModal("Please change the end date of at least one member before updating the end date!!");
            return false;
        }

        let tempRmgMemberEndDateList: RmgMemberEndDate[] = [];

        for (let i = 0; i < currentTeam?.rmgOldTeamMemberList?.length; i++) {
            let member = currentTeam?.rmgOldTeamMemberList[i];
            if (!member.endDate || member.endDate == undefined || member.endDate == null) {
                member.endDate = member.dbEndDate;
                this.openAlertMessageModal(`Kindly provide an End date for Member # ${i + 1}`);
                return false;
            }

            let startDate = this.normalizeDate(member.startDate);
            let endDate = this.normalizeDate(member.endDate);
            if (startDate > endDate) {
                member.endDate = member.dbEndDate;
                this.openAlertMessageModal(`Member End date cannot be less then Member Start date for Member # ${i + 1}`);
                return false;
            }
            if (endDate !== this.normalizeDate(member.dbEndDate)) {
                tempRmgMemberEndDateList.push({ etmId: member.etmId, empId: member.empId, endDate: member.endDate });
            }
        }

        let rmgTeam = new RmgTeam();
        rmgTeam.projectId = this.rmgProjectObj.projectId;
        rmgTeam.teamId = currentTeam?.teamId;
        rmgTeam.updatedBy = this.currentUser?.empId;
        rmgTeam.projectType = this.projectType;
        rmgTeam.rmgMemberEndDateList = [...tempRmgMemberEndDateList];

        this.teamService.extendTeamMembersEndDate(rmgTeam).pipe(first()).subscribe(async (response: any) => {
            if (response.serviceStatus == "Success") {
                this.toastService.success(response.serviceResponse);
                await this.getTeamDetailsByTeamId(this.currentTeam);
            } else {
                this.toastService.error(response.serviceResponse);
            }
        });
    }

    migrateTeamMembers(employee: any) {
        if (!this.teamMembersMigrationObj.targetProjectId || !this.isValidNumber(this.teamMembersMigrationObj.targetProjectId)) {
            this.openAlertMessageModal("Kindly Select target Project!!");
            return;
        }
        if (!employee.targetPoId || !this.isValidNumber(employee.targetPoId)) {
            this.openAlertMessageModal("Kindly Select PO!!");
            return;
        }
        let tempTeamMembersMigrationObj = new MigrateTeams();
        tempTeamMembersMigrationObj.sourceProjectId = this.rmgProjectObj.projectId;
        tempTeamMembersMigrationObj.targetProjectId = this.teamMembersMigrationObj.targetProjectId;
        tempTeamMembersMigrationObj.currentUserEmpId = this.currentUser.empId;
        tempTeamMembersMigrationObj.empIds = employee.empId ? [employee.empId] : [];
        tempTeamMembersMigrationObj.targetPoId = employee.targetPoId;
        tempTeamMembersMigrationObj.targetRoleId = employee.targetRoleId;
        tempTeamMembersMigrationObj.migrationTeamIds = this.teamMembersMigrationObj.migrationTeamIds;
        this.teamService.migrateTeamMembers(tempTeamMembersMigrationObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.toastService.success(response.serviceResponse);
                this.getResourceRequirementDetailsByProjectIdForTeamMemberMigration(this.teamMembersMigrationObj.targetProjectId);
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
                this.getResourceRequirementDetailsByProjectIdForTeamMemberMigration(this.teamMembersMigrationObj.targetProjectId);
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

    async validateEmployeeProjectStartDate(member: RmgTeamMember, projectId: any, projectStartDate: any, projectType: any): Promise<boolean> {
        const selectedDate = this.normalizeDate(member.startDate);
        const normalisedProjectStartDate = this.normalizeDate(projectStartDate);
        if (selectedDate < normalisedProjectStartDate) {
            this.openUpdateProjectStartDateErrorModal();
            return false;
        }

        this.isProjectOverlapping = false;
        this.gapStartDate = null;
        this.gapEndDate = null;
        this.existingEmployeeProjectTimesheetEntries = [];

        let rmgMember = new RmgTeamMember();
        rmgMember.empId = member.empId;
        rmgMember.projectId = this.rmgProjectObj.projectId;
        rmgMember.projectStartDate = normalisedProjectStartDate;
        rmgMember.projectType = projectType;
        rmgMember.startDate = selectedDate;
        rmgMember.projectIds = [this.rmgProjectObj.projectId, projectId];
        try {
            const response: any = await firstValueFrom(this.teamService.validateEmployeeProjectStartDate(rmgMember));
            if (response.serviceStatus === "Success") {
                if (response.serviceResponse === 'CONFLICTING_TIMESHEET_RECORDS_FOUND' || response.serviceResponse === 'OTHER_TNM_PROJECT_OVERLAPPING' || response.serviceResponse === 'CURRENT_TNM_PROJECT_OVERLAPPING') {
                    this.isProjectOverlapping = response.serviceResponse === 'OTHER_TNM_PROJECT_OVERLAPPING' || response.serviceResponse === 'CURRENT_TNM_PROJECT_OVERLAPPING';
                    this.existingEmployeeProjectTimesheetEntries = response?.serviceResponse2;
                    this.openExistingEmployeeProjectTimesheetInfoModal();
                    return false;
                } else if (response.serviceResponse === 'GAP_EXISTS') {
                    let resp = response?.serviceResponse2;
                    this.gapStartDate = resp.employeeTeamStartDate;
                    this.gapEndDate = (resp.employeeTeamEndDate != undefined && resp.employeeTeamEndDate != null) ? resp.employeeTeamEndDate : null;
                    this.openProjectGapMessageModal();
                    return true;
                }
                return true;
            } else {
                this.openAlertMessageModal(response.serviceResponse || "Something went wrong, unable to validate the selected start date at the moment!!");
                return false;
            }
        } catch (error) {
            this.openAlertMessageModal("Something went wrong, unable to validate the selected start date at the moment!!");
            return false;
        }
    }

    async validateEmployeeProjectStartDateBulk(startDate: any, projectId: any, projectStartDate: any, projectType: any): Promise<boolean> {
        const selectedDate = this.normalizeDate(startDate);
        const normalisedProjectStartDate = this.normalizeDate(projectStartDate);
        if (selectedDate < normalisedProjectStartDate) {
            this.openAlertMessageModal("Member Start Date must be after Project Start Date!!");
            return false;
        }

        this.isProjectOverlapping = false;
        this.gapStartDate = null;
        this.gapEndDate = null;
        this.existingEmployeeProjectTimesheetEntries = [];

        let rmgMember = new RmgTeamMember();
        rmgMember.selectedEmpIds = this.markDefaultProjectCompletionList?.map(member => member.empId)
        rmgMember.projectId = projectId;
        rmgMember.projectStartDate = normalisedProjectStartDate;
        rmgMember.projectType = projectType;
        rmgMember.startDate = selectedDate;
        try {
            const response: any = await firstValueFrom(this.teamService.validateEmployeeProjectStartDate(rmgMember));
            if (response.serviceStatus === "Success") {
                if (response.serviceResponse === 'CONFLICTING_TIMESHEET_RECORDS_FOUND' || response.serviceResponse === 'OTHER_TNM_PROJECT_OVERLAPPING' || response.serviceResponse === 'CURRENT_TNM_PROJECT_OVERLAPPING') {
                    this.isProjectOverlapping = response.serviceResponse === 'OTHER_TNM_PROJECT_OVERLAPPING' || response.serviceResponse === 'CURRENT_TNM_PROJECT_OVERLAPPING';
                    this.existingEmployeeProjectTimesheetEntries = response?.serviceResponse2;
                    this.openExistingEmployeeProjectTimesheetInfoModal();
                    return false;
                } else if (response.serviceResponse === 'GAP_EXISTS') {
                    let resp = response?.serviceResponse2;
                    this.gapStartDate = resp.employeeTeamStartDate;
                    this.gapEndDate = (resp.employeeTeamEndDate != undefined && resp.employeeTeamEndDate != null) ? resp.employeeTeamEndDate : null;
                    this.openProjectGapMessageModal();
                    return true;
                }
                return true;
            } else {
                this.openAlertMessageModal(response.serviceResponse || "Something went wrong, unable to validate the selected start date at the moment!!");
                return false;
            }
        } catch (error) {
            this.openAlertMessageModal("Something went wrong, unable to validate the selected start date at the moment!!");
            return false;
        }
    }

    validateEmployeeProjectStartDateIndividual(event: MatDatepickerInputEvent<Date>, member: RmgTeamMember) {

    }

    onMemberRemoveEndDateTypeChange(event: any) {
        const selectedValue = event.value;

        if (selectedValue === 'PO') {
            this.employeeProjectEndDate =
                this.deleteEmployeeExistingProjectMappingObj?.poEndDate
                    ? moment(this.deleteEmployeeExistingProjectMappingObj.poEndDate)
                        .format('YYYY-MM-DD')
                    : null;
        }
        else if (selectedValue === 'Custom') {
            this.employeeProjectEndDate = null;
        }
    }

    onMembersEndDateTypeChange(event: any) {
        const selectedValue = event.value;
        if (selectedValue === 'PO') {
            this.membersEndDate =
                this.deleteEmployeeExistingProjectMappingObj?.poEndDate
                    ? moment(this.deleteEmployeeExistingProjectMappingObj.poEndDate)
                        .format('YYYY-MM-DD')
                    : null;
        }
        else if (selectedValue === 'Custom') {
            this.membersEndDate = null;
        }
    }

    onMemberEndDateChange(event: MatDatepickerInputEvent<Date>, member: RmgTeamMember) {
        const selectedDate = this.normalizeDate(event.value);
        const memberStartDate = this.normalizeDate(member.startDate);
        if (selectedDate < memberStartDate) {
            member.endDate = member.dbEndDate;
            this.openAlertMessageModal("Member End Date cannot be less than Member Start Date!!");
            return false;
        }
        member.endDate = event.value;
    }

    async updateMemberShadowMapping() {
        if (!this.shadowResourceMappingMemberEndDate || this.shadowResourceMappingMemberEndDate == undefined || this.shadowResourceMappingMemberEndDate == null) {
            this.openAlertMessageModal("Kindly provide End Date!!");
            return;
        }
        if (!this.shadowResourceMappingMemberStartDate || this.shadowResourceMappingMemberStartDate == undefined || this.shadowResourceMappingMemberStartDate == null) {
            this.openAlertMessageModal("Kindly provide Start Date!!");
            return;
        }

        const selectedEndDate = this.normalizeDate(this.shadowResourceMappingMemberEndDate);
        const selectedStartDate = this.normalizeDate(this.shadowResourceMappingMemberStartDate);
        const memberStartDate = this.normalizeDate(this.shadowResourceMappingMember.startDate);

        if (selectedEndDate < memberStartDate) {
            this.shadowResourceMappingMemberEndDate = null;
            this.openAlertMessageModal("Member Current End Date cannot be less than Member Current Start Date!!");
            return false;
        }
        if (selectedEndDate > selectedStartDate) {
            this.shadowResourceMappingMemberStartDate = null;
            this.openAlertMessageModal("Member Current End Date cannot be less than Member New Start Date!!");
            return false;
        }

        let rmgMember = new RmgTeamMember();
        rmgMember.empId = this.shadowResourceMappingMember.empId;
        rmgMember.updatedBy = this.currentUser.empId;
        rmgMember.projectId = this.rmgProjectObj.projectId;
        rmgMember.projectType = this.projectType;
        rmgMember.endDate = selectedEndDate;
        rmgMember.startDate = selectedStartDate;
        rmgMember.isShadow = this.shadowResourceMappingMember?.isShadow != null && this.shadowResourceMappingMember?.isShadow ? 1 : 0;
        rmgMember.defaultProject = this.shadowResourceMappingMember.defaultProject;
        rmgMember.teamId = this.currentTeam.teamId;

        this.teamService.updateMemberShadowMapping(rmgMember).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.closeShadowResourceMappingModal();
                this.getTeamDetailsByTeamId(this.currentTeam);
            } else {
                this.toastService.error(response.serviceResponse || 'Something went wrong!!');
            }
        });
    }

    addMemberMappingToCloneList(member: RmgTeamMember) {
        let newMember = new RmgTeamMember();
        newMember.projectId = this.rmgProjectObj.projectId;
        newMember.teamId = this.currentTeam.teamId;
        newMember.poId = member.poId;
        newMember.roleId = member.roleId;
        newMember.employeeRoles = member.employeeRoles;
        newMember.startDate = member.startDate;
        newMember.endDate = member.endDate;
        newMember.resourceRequirementList = this.filteredActiveResourceRequirement;
        if (!this.isValidList(this.cloneMemberMappingList)) {
            this.cloneMemberMappingList = [];
        }
        this.cloneMemberMappingList.push(newMember);
    }

    removeFromCloneMemberMappingList(member: RmgTeamMember, index: number) {
        this.cloneMemberMappingList.splice(index, 1);
    }

    async saveClonedMembers() {
        if (!this.isValidList(this.cloneMemberMappingList)) {
            this.openAlertMessageModal("Kindly add from existing members to Clone!!");
            return;
        }

        for (let i = 0; i < this.cloneMemberMappingList?.length; i++) {
            let member = this.cloneMemberMappingList[i];
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
            if (member.startDate !== undefined && member.startDate !== null
                && (!member.dbStartDate || member.dbStartDate == undefined || member.dbStartDate == null)) {
                const flag: boolean = await this.validateEmployeeProjectStartDate(member, this.rmgProjectObj.projectId, this.rmgProjectObj.startDate, this.projectType);
                if (!flag) {
                    return;
                }
            }
            if (member.startDate !== undefined && member.startDate !== null
                && member.dbStartDate !== undefined && member.dbStartDate !== null
                && this.normalizeDate(member.dbStartDate) !== this.normalizeDate(member.startDate)) {
                const flag: boolean = await this.validateEmployeeProjectStartDate(member, this.rmgProjectObj.projectId, this.rmgProjectObj.startDate, this.projectType);
                if (!flag) {
                    return;
                }
            }
            if (member.endDate && member.endDate != undefined && member.endDate != null && member.startDate > member.endDate) {
                this.openAlertMessageModal(`End Date cannot be less than Start Date for Member # ${i + 1}`);
                return;
            }
            member.startDate = member.startDate ? moment(member.startDate).format('YYYY-MM-DDTHH:mm:ss') : null;
            member.endDate = member.endDate ? moment(member.endDate).format('YYYY-MM-DDTHH:mm:ss') : null;
            member.isShadow = this.cloneMemberMappingList[i].isShadow != null && this.cloneMemberMappingList[i].isShadow ? 1 : 0;
        }

        let rmgTeam = new RmgTeam();
        rmgTeam.projectId = this.rmgProjectObj.projectId;
        rmgTeam.projectType = this.projectType;
        rmgTeam.teamId = this.currentTeam.teamId;
        rmgTeam.clientName = this.rmgProjectObj?.clientName;
        rmgTeam.updatedBy = this.currentUser.empId;
        rmgTeam.rmgTeamMemberList = this.cloneMemberMappingList;

        this.teamService.addOrUpdateTeamMembers(rmgTeam).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.getTeamDetailsByTeamId(this.currentTeam);
                this.toastService.success(response.serviceResponse);
            } else {
                this.toastService.error(response.serviceResponse);
            }
        });
    }

    // Team Members Method & APIs End

    // Resource Requirements Method & APIs Start
    getResourceRequirementByPoId(poId: any) {
        this.resourceRequirementList = [];
        this.filteredActiveResourceRequirement = [];
        this.resourceManagementService.getResourceRequirementByPoId(poId).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.resourceRequirementList = response.serviceResponse || [];
                this.resourceRequirementList?.forEach(req => {
                    req.isExpired =
                        (req.poEndDate && moment(req.poEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD')) ||
                        (req.requirementEndDate && moment(req.requirementEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD'));
                });
                this.resourceRequirementList.sort((a, b) =>
                    moment(b?.requirementEndDate || 0).diff(moment(a?.requirementEndDate || 0))
                );
                this.filteredActiveResourceRequirement = [...this.resourceRequirementList];
            } else {
                this.toastService.error(response.serviceResponse || "Something went wrong!!");
            }
        });
    }

    getResourceRequirementByPoIdForEmployee(poId: any, member : RmgTeamMember) {
        member.resourceRequirementList = [];
        member.filteredActiveResourceRequirement = [];
        this.resourceManagementService.getResourceRequirementByPoId(poId).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                member.resourceRequirementList = response.serviceResponse || [];
                member.resourceRequirementList?.forEach(req => {
                    req.isExpired =
                        (req.poEndDate && moment(req.poEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD')) ||
                        (req.requirementEndDate && moment(req.requirementEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD'));
                });
                member.resourceRequirementList.sort((a, b) =>
                    moment(b?.requirementEndDate || 0).diff(moment(a?.requirementEndDate || 0))
                );
                member.filteredActiveResourceRequirement = [...member.resourceRequirementList];
            } else {
                this.toastService.error(response.serviceResponse || "Something went wrong!!");
            }
        });
    }

    getResourceRequirementByTeamIdForEmployee(member: RmgTeamMember) {
        if (this.allNonBillableProjectTypes.includes(member.projectType?.toLowerCase())) {
            return;
        }
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
        if (this.allNonBillableProjectTypes.includes(this.defaultProjectObj.projectType?.toLowerCase())) {
            return;
        }
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
                    this.currentRmgProjectResourceRequirementList?.forEach(req => {
                        req.isExpired =
                            (req.poEndDate && moment(req.poEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD')) ||
                            (req.requirementEndDate && moment(req.requirementEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD'));
                    });
                    this.currentRmgProjectResourceRequirementList.sort((a, b) =>
                        moment(b?.requirementEndDate || 0).diff(moment(a?.requirementEndDate || 0))
                    );
                } else {
                    this.allRmgProjectResourceRequirementList = response.serviceResponse || [];
                    this.allRmgProjectResourceRequirementList?.forEach(req => {
                        req.isExpired =
                            (req.poEndDate && moment(req.poEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD')) ||
                            (req.requirementEndDate && moment(req.requirementEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD'));
                    });
                    this.allRmgProjectResourceRequirementList.sort((a, b) =>
                        moment(b?.requirementEndDate || 0).diff(moment(a?.requirementEndDate || 0))
                    );
                }
            } else {
                this.toastService.error(response.serviceResponse || "Something went wrong!!");
            }
        });
    }

    getResourceRequirementDetailsByProjectIdForTeamMemberMigration(projectId: any) {
        if (this.teamMembersMigrationObj.isInternalProject) {
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
        if (this.teamMembersMigrationObj.isInternalProject) {
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
                this.rmgProjectObj.dbProjectManagerIds = this.rmgProjectObj?.projectManagerIds || [];
                this.validateProjectManagerIds();
            } else {
                this.openAlertMessageModal(response.serviceResponse);
            }
        });
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

    async getEmployeeExistingProjectDetailsByEmpId(empId: any, resetObj: boolean, projectId: any): Promise<boolean> {
        this.employeeExistingProjectDetails = [];
        this.employeeExistingProjectDetailsPage = 1;
        this.employeeExistingProjectEmploymentId = this.employeeListFilteredByDept?.find(emp => emp?.empId === this.currentTeam.newRmgTeamMember?.empId)?.employmentId;
        this.employeeExistingProjectEmpName = this.employeeListFilteredByDept?.find(emp => emp?.empId === this.currentTeam.newRmgTeamMember?.empId)?.name;
        if (resetObj) {
            this.currentTeam.newRmgTeamMember = new RmgTeamMember();
        }
        this.currentTeam.newRmgTeamMember.empId = empId;
        this.currentTeam.newRmgTeamMember.employementId = this.employeeExistingProjectEmploymentId;
        this.currentTeam.newRmgTeamMember.dbDefaultProject = !this.currentTeam.newRmgTeamMember.defaultProject && this.rmgProjectObj.projectId === this.employeeListFilteredByDept?.find(emp => emp?.empId === this.currentTeam.newRmgTeamMember?.empId)?.defaultProjectId;

        try {
            const response: any = await firstValueFrom(this.projectService.getEmployeeExistingProjectDetailsByEmpId(empId, projectId));
            if (response?.serviceStatus !== "Success") {
                this.openAlertMessageModal(response?.serviceResponse || "Something went wrong!!");
                return false;
            }
            const projects = this.isValidList(response.serviceResponse) ? response.serviceResponse || [] : [];
            this.employeeExistingProjectDetails = this.mapEmployeeProjectDates(projects);
            if (!this.isValidList(this.employeeExistingProjectDetails)) {
                return true;
            }
            this.openEmployeeExistingProjectDetailsModal();
            return false;
        } catch (error) {
            this.openAlertMessageModal("Something went wrong!");
            return false;
        }
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
        const flag: boolean = await this.validateEmployeeProjectStartDate(employee, employee.projectId, employee.selectedProject.projectStartDate, this.projectType);
        if (!flag) {
            return;
        }
        employee.selectedProject.updatedBy = this.currentUser.empId;

        try {
            const response: any = await firstValueFrom(this.projectService.updateMappingToOtherProjectAsDefault(employee.selectedProject));
            if (response.serviceStatus == "Success") {
                this.toastService.success(response.serviceResponse);
                this.closeMappingToOtherProjectAsDefaultModal();
                if (this.defaultProjectMappingActionType === 'DELETE_TEAM') {
                    this.validateDeleteTeams();
                } else if (this.defaultProjectMappingActionType === 'PROJECT_COMPLETION') {
                    this.validateProjectForCompletion(this.rmgProjectObj.projectId);
                } else if (this.defaultProjectMappingActionType === 'REMOVE_MEMBERS') {
                    await this.getTeamDetailsByTeamId(this.currentTeam);
                    await this.validateRemoveMembers(this.currentTeam?.rmgCurrentTeamMemberList);
                }
            } else {
                this.toastService.error(response.serviceResponse || "Something went wrong!");
            }
        } catch (error) {
            this.toastService.error("Something went wrong!");
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
        tempRmgTeamMember.clientName = this.rmgProjectObj?.clientName;
        tempRmgTeamMember.selectedEmpIds = isBulk ? this.markDefaultProjectCompletionList?.map(member => member.empId) ?? [] : employee?.empId ? [employee.empId] : [];
        tempRmgTeamMember.projectType = this.projectType;
        tempRmgTeamMember.startDate = this.normalizeDate(this.defaultProjectObj.startDate);

        this.teamService.updateDefaultProjectCompletion(tempRmgTeamMember).pipe(first()).subscribe(async (response: any) => {
            if (response.serviceStatus == "Success") {
                this.closeMarkDefaultProjectCompletionModal();
                if (this.defaultProjectMappingActionType === 'DELETE_TEAM') {
                    this.validateDeleteTeams();
                } else if (this.defaultProjectMappingActionType === 'PROJECT_COMPLETION') {
                    this.validateProjectForCompletion(this.rmgProjectObj);
                } else if (this.defaultProjectMappingActionType === 'REMOVE_MEMBERS') {
                    await this.getTeamDetailsByTeamIdForUpdationDefaultProject();
                }
            } else {
                this.toastService.error(response.serviceResponse || "Something went wrong!!");
            }
        });
    }

    updateDefaultProjectCompletionBulk() {
        if (!this.isValidString(this.defaultProjectObj?.projectType)) {
            this.openAlertMessageModal('Project Type must be selected!!');
            return;
        }
        if (!this.defaultProjectObj.projectId) {
            this.openAlertMessageModal('Kindly Select a Project!!');
            return;
        }
        if (!this.defaultProjectObj.teamId) {
            this.openAlertMessageModal('Kindly Select a Team!!');
            return;
        }
        if (!this.isValidList(this.defaultProjectObj.employeeRoles)) {
            this.openAlertMessageModal('Employee Role must be selected!!');
            return;
        }
        if (!this.defaultProjectObj.startDate || this.defaultProjectObj.startDate == undefined || this.defaultProjectObj.startDate == null) {
            this.openAlertMessageModal("Kindly Provide Start Date!!");
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
        tempRmgTeamMember.startDate = this.normalizeDate(this.defaultProjectObj.startDate);

        this.teamService.updateDefaultProjectCompletion(tempRmgTeamMember).pipe(first()).subscribe(async (response: any) => {
            if (response.serviceStatus == "Success") {
                this.closeMarkDefaultProjectCompletionModal();
                if (this.defaultProjectMappingActionType === 'DELETE_TEAM') {
                    this.validateDeleteTeams();
                } else if (this.defaultProjectMappingActionType === 'PROJECT_COMPLETION') {
                    this.validateProjectForCompletion(this.rmgProjectObj);
                } else if (this.defaultProjectMappingActionType === 'REMOVE_MEMBERS') {
                    await this.getTeamDetailsByTeamIdForUpdationDefaultProject();
                }
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
                this.getEmployeeExistingProjectDetailsByEmpId(this.deleteEmployeeExistingProjectMappingObj.empId, true, this.deleteEmployeeExistingProjectMappingObj.projectId);
                this.toastService.success(response.serviceResponse);
            } else {
                this.toastService.error(response.serviceResponse);
            }
        });
        this.closeEmployeeExistingProjectDetailsModal();
        this.closeDeleteEmployeeFromExistingProjectModal();
    }

    saveProjectInformation() {
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
        employee.projectStartDate = this.projectList?.find(p => p.projectId === this.defaultProjectObj.projectId)?.startDate;
        employee.calculatedProjectType = this.projectList?.find(p => p.projectId === this.defaultProjectObj.projectId)?.projectType;
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
        this.defaultProjectObj.projectStartDate = this.projectList?.find(p => p.projectId === this.defaultProjectObj.projectId)?.startDate;
        this.defaultProjectObj.calculatedProjectType = this.projectList?.find(p => p.projectId === this.defaultProjectObj.projectId)?.projectType;
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

    async validateProjectForCompletion(projectId: any) {
        this.markDefaultProjectCompletionList = [];
        this.mappingToOtherProjectAsDefaultList = [];
        let poObj = new PoDetails();
        poObj.projectId = projectId;
        poObj.activeEtmFlag = false;
        await this.getActiveProjectList();
        this.teamService.getTeamDetailsByProjectId(poObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === "Success") {
                let teamMembers: RmgTeamMember[] = response.serviceResponse || [];

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

                this.resetDefaultProjectCompletion(this.markDefaultProjectCompletionList);
                this.mapProjectListToEmployees(this.mappingToOtherProjectAsDefaultList);

                if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
                    this.openMarkDefaultProjectCompletionModal('PROJECT_COMPLETION');
                    this.openMappingToOtherProjectAsDefaultModal('PROJECT_COMPLETION');
                } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length === 0) {
                    this.openMarkDefaultProjectCompletionModal('PROJECT_COMPLETION');
                } else if (noOtherActiveAndCurrentIsDefaultProjectEmpIds.length === 0 && otherActiveAndCurrentIsDefaultProjectEmpIds.length !== 0) {
                    this.openMappingToOtherProjectAsDefaultModal('PROJECT_COMPLETION');
                } else {
                    this.openProjectCompletionDatePicker.emit();
                }
            } else {
                this.toastService.error(response.serviceResponse || 'Something went wrong!!');
            }
        });
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