import { Component, TemplateRef, ViewChild } from '@angular/core';
import { NgbModalRef, NgbModal, NgbModalOptions } from '@ng-bootstrap/ng-bootstrap';
import { ModalEvent, AppModalService } from '../app-modal.service';
import { EmployeeProjectTimesheetDto } from 'src/app/models/employeeProjectTimesheetDto';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { MomentDateAdapter, MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';
import { Sort } from '@angular/material/sort';
import { GlobalRightDrawerService } from 'src/app/services/global-right-drawer.service';
import { ResultType } from 'src/app/services/employee-project.service';
import * as moment from 'moment';
import { RmgTeamMember } from 'src/app/models/rmgTeamMember';

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
  selector: 'app-rmg-modal-host',
  templateUrl: './rmg-modal-host.component.html',
  styleUrl: './rmg-modal-host.component.css',
  providers: [
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_MOMENT_DATE_ADAPTER_OPTIONS] },
    { provide: MAT_DATE_FORMATS, useValue: MY_DATE_FORMATS },
  ]
})

export class RmgModalHostComponent {

  @ViewChild("alert_message") alertMessageTemplateRef: TemplateRef<any>;
  @ViewChild("update_project_start_date_error") updateProjectStartDateErrorTemplateRef!: TemplateRef<any>;
  @ViewChild("update_project_start_date_confirmation") updateProjectStartDateConfirmationTemplateRef!: TemplateRef<any>;
  @ViewChild("existing_employee_project_timesheet_info") existingEmployeeProjectTimesheetInfoTemplateRef!: TemplateRef<any>;
  @ViewChild("project_gap_message") projectGapMessageTemplateRef!: TemplateRef<any>;
  @ViewChild("employee_existing_project_details") employeeExistingProjectDetailsTemplateRef: TemplateRef<any>;
  @ViewChild("delete_employee_from_existing_project") deleteEmployeeFromExistingProjectTemplateRef: TemplateRef<any>;

  private modalConfigMap = new Map<ResultType, NgbModalOptions>([
    [ResultType.ALERT, { size: 'sm'  }],
    [ResultType.PROJECT_START_DATE_ERROR, { size: 'md', backdrop: 'static', keyboard: false }],
    [ResultType.PROJECT_START_DATE_UPDATE_CONFIRMATION, { size: 'sm', backdrop: 'static', keyboard: false }],
    [ResultType.EMPLOYEE_PROJECT_TIMESHEET_CONFLICT, { size: 'xl', backdrop: 'static', keyboard: false }],
    [ResultType.PROJECT_GAP, { size: 'md'}],
    [ResultType.EMPLOYEE_EXISTING_PROJECT_DETAILS, { size: 'sm', backdrop: 'static', keyboard: false }],
    [ResultType.DELETE_EMPLOYEE_FROM_EXISTING_PROJECT, { size: 'sm', backdrop: 'static', keyboard: false }],
  ]);

  // private modalConfigMap = new Map<ResultType, NgbModalOptions>([
  //   [ResultType.ALERT, { size: 'sm' }],
  //   [ResultType.PROJECT_START_DATE_ERROR, { size: 'md' }],
  //   [ResultType.PROJECT_START_DATE_UPDATE_CONFIRMATION, { size: 'sm' }],
  //   [ResultType.EMPLOYEE_PROJECT_TIMESHEET_CONFLICT, { size: 'xl' }],
  //   [ResultType.PROJECT_GAP, { size: 'md' }],
  //   [ResultType.EMPLOYEE_EXISTING_PROJECT_DETAILS, { size: 'sm' }],
  //   [ResultType.DELETE_EMPLOYEE_FROM_EXISTING_PROJECT, { size: 'sm' }],
  // ]);

  private modalMap = new Map<string, NgbModalRef>();

  isProjectOverlapping: boolean = false;

  alertMessage: string = '';
  employeeProjectEndDateType: 'PO' | 'Custom' = 'Custom';

  startDateUpdateProjectId: any;

  employeeProjectEndDate: any;
  projectNewStartDate: any
  gapStartDate: any;
  gapEndDate: any;

  deleteEmployeeExistingProjectMappingObj: RmgTeamMember;

  employeeExistingProjectEmpName: any;
  employeeExistingProjectEmploymentId: any;
  employeeExistingProjectDetails: any[] = [];

  rmgTeamMember: RmgTeamMember = new RmgTeamMember();
  existingEmployeeProjectTimesheetEntries: EmployeeProjectTimesheetDto[] = [];

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
    private modalService: NgbModal,
    private appModalService: AppModalService,
    private drawerService: GlobalRightDrawerService,
  ) { }

  ngOnInit() {
    this.appModalService.rmgModal$.subscribe(event => this.handleEvent(event));
  }

  handleEvent(event: ModalEvent) {
    switch (event.action) {
      case 'OPEN':
        this.openModal(event);
        break;

      case 'CLOSE':
        this.closeModal(event.id!);
        break;

      case 'CLOSE_ALL':
        this.closeAll();
        break;
    }
  }

  openModal(event: ModalEvent) {
    let template: TemplateRef<any>;
    console.log(event);
    const data = event?.data;

    switch (event.modalType) {
      case 'ALERT':
        this.alertMessage = data;
        template = this.alertMessageTemplateRef;
        break;

      case 'PROJECT_START_DATE_ERROR':
        template = this.updateProjectStartDateErrorTemplateRef;
        break;

      case 'PROJECT_START_DATE_UPDATE_CONFIRMATION':
        this.projectNewStartDate = null;
        this.startDateUpdateProjectId = data;
        template = this.updateProjectStartDateConfirmationTemplateRef;
        break;

      case 'EMPLOYEE_PROJECT_TIMESHEET_CONFLICT':
        this.rmgTeamMember = data?.rmgMember || new RmgTeamMember();
        this.existingEmployeeProjectTimesheetEntries = data?.entries || [];
        this.isProjectOverlapping = data?.isOverlap || false;
        template = this.existingEmployeeProjectTimesheetInfoTemplateRef;
        break;

      case 'PROJECT_GAP':
        this.gapStartDate = data.employeeTeamStartDate;
        this.gapEndDate = (data.employeeTeamEndDate != undefined && data.employeeTeamEndDate != null) ? data.employeeTeamEndDate : null;
        template = this.projectGapMessageTemplateRef;
        break;

      case 'EMPLOYEE_EXISTING_PROJECT_DETAILS':
        this.employeeExistingProjectDetails = data?.data || [];
        this.employeeExistingProjectEmploymentId = data?.employmentId;
        this.employeeExistingProjectEmpName = data?.name;
        this.drawerService.open(this.employeeExistingProjectDetailsTemplateRef);
        return;

      case 'DELETE_EMPLOYEE_FROM_EXISTING_PROJECT':
        this.employeeProjectEndDate = null
        this.deleteEmployeeExistingProjectMappingObj = data;
        template = this.deleteEmployeeFromExistingProjectTemplateRef;
        break;

      default:
        return;
    }

    const config = this.modalConfigMap.get(event.modalType as ResultType) || { size: 'md' };
    const ref = this.modalService.open(template, config);

    this.modalMap.set(event.id!, ref);

    ref.result.finally(() => {
      this.modalMap.delete(event.id!);
    });
  }

  closeModal(id: string) {
    const ref = this.modalMap.get(id);
    ref?.close();
    this.modalMap.delete(id);
  }

  closeAll() {
    this.modalMap.forEach(ref => ref.close());
    this.modalMap.clear();
  }

  closeEmployeeExistingProjectDetailsModal() {
    this.drawerService?.close();
  }

  onAction(actionType: string, data?: any) {
    this.appModalService.triggerAction({ actionType, data });
  }

  get isEmployeeMappedToAnyTNMProject() {
    return this.employeeExistingProjectDetails?.some(p => p.projectType === 'TNM');
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

  handleEmployeeProjectDetailsPageChange(event: any) {
    this.employeeExistingProjectDetailsPage = event;
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

}
