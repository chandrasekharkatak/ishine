import { LOCALE_ID, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { DragDropModule } from '@angular/cdk/drag-drop';
import { DatePipe, HashLocationStrategy, LocationStrategy } from '@angular/common';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatNativeDateModule, MatOptionModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatAccordion } from '@angular/material/expansion';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ClipboardModule } from 'ngx-clipboard';
import { NgxMatSelectSearchModule } from 'ngx-mat-select-search';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BodyComponent } from './body/body.component';
import { SidenavComponent } from './sidenav/sidenav.component';

import { ColFilterPipe } from './col-filter.pipe';
import { RmbCamelCaseDisplayPipe } from './rmb-camel-case-display.pipe';
import { ResourceManagementComponent } from './user-team/resource-management/resource-management.component';

import { ColumnFilterBarComponent } from './helpers/column-filter-bar/column-filter-bar.component';
import { MultiColFilterPipe } from './multi-col-filter.pipe';
import { SortPipe } from './sort.pipe';

import { NgxPaginationModule } from 'ngx-pagination';

import { ConfigurationComponent } from './configuration/configuration.component';
import { DeptConfigComponent } from './configuration/dept-config/dept-config.component';
import { DesignationConfigComponent } from './configuration/designation-config/designation-config.component';
import { DocumentComponent } from './configuration/document/document.component';
import { DomainConfigComponent } from './configuration/domain-config/domain-config.component';
import { EmployeeConfigComponent } from './configuration/employee-config/employee-config.component';
import { HomeConfigComponent } from './configuration/home-config/home-config.component';
import { LeaveConfigComponent } from './configuration/leave-config/leave-config.component';
import { NewsletterConfigComponent } from './configuration/newsletter-config/newsletter-config.component';
import { OnBoardingComponent } from './configuration/on-boarding/on-boarding.component';
import { OthersComponent } from './configuration/others/others.component';
import { PortalConfigComponent } from './configuration/portal-config/portal-config.component';
import { ProjectConfigComponent } from './configuration/project-config/project-config.component';
import { RewardsConfigComponent } from './configuration/rewards-config/rewards-config.component';
import { RoleConfigComponent } from './configuration/role-config/role-config.component';
import { SurveyConfigComponent } from './configuration/survey-config/survey-config.component';
import { UploadPoliciesComponent } from './configuration/upload-policies/upload-policies.component';

import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { GrievanceComponent } from './grievance/grievance.component';
import { GrievanceAuditTimelineComponent } from './grievance/grievance-audit-timeline.component';
import { GrievanceEditComponent } from './grievance/grievance-edit.component';
import { GrievanceIssueScenarioAdminComponent } from './grievance/grievance-issue-scenario-admin.component';
import { CalendarComponent } from './helpers/calendar/calendar.component';

import { CustomFilterComponent } from './helpers/custom-filter/custom-filter.component';
import { EmployeePortalInterceptor } from './helpers/employeePortal.interceptor';
import { LoaderInterceptor } from './helpers/loader.interceptor';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { LoaderComponent } from './loader/loader.component';
import { LoginComponent } from './login/login.component';
import { MinutesToHoursPipe } from './minutes-to-hours.pipe';

import { NewsletterComponent } from './newsletter/newsletter.component';
import { RecruitmentComponent } from './recruitment/recruitment.component';
import { RewardFilterPipe } from './reward-filter.pipe';

import { Employee360AppreciationComponent } from './employee360/employee360-appreciation/employee360-appreciation.component';
import { Employee360GrievanceComponent } from './employee360/employee360-grievance/employee360-grievance.component';
import { Employee360BiomaxComponent } from './employee360/employee360-biomax/employee360-biomax.component';
import { Employee360LeaveComponent } from './employee360/employee360-leave/employee360-leave.component';
import { Employee360ProfileComponent } from './employee360/employee360-profile/employee360-profile.component';
import { Employee360ProjectComponent } from './employee360/employee360-project/employee360-project.component';
import { Employee360RewardsComponent } from './employee360/employee360-rewards/employee360-rewards.component';
import { Employee360TimesheetComponent } from './employee360/employee360-timesheet/employee360-timesheet.component';
import { Employee360Component } from './employee360/employee360.component';
import { BreadcrumbComponent } from './helpers/breadcrumb/breadcrumb.component';
import { NavigateToEmployee360Directive } from './navigate-to-employee360.directive';
import { RewardsAndRecognisationComponent } from './rewards/rewards-and-recognisation/rewards-and-recognisation.component';
import { RewardsComponent } from './rewards/rewards.component';

import { UserAppreciationComponent } from './user-appreciation/user-appreciation.component';
import { UserAttendanceComponent } from './user-attendance/user-attendance.component';
import { MyResignationComponent } from './user-exit/my-resignation/my-resignation.component';
import { ResignationComponent } from './user-exit/resignation/resignation.component';
import { UserExitComponent } from './user-exit/user-exit.component';
import { CompOffComponent } from './user-leaves/comp-off/comp-off.component';
import { HolidaysComponent } from './user-leaves/holidays/holidays.component';
import { LeaveComponent } from './user-leaves/leave/leave.component';
import { UserLeavesComponent } from './user-leaves/user-leaves.component';
import { UserPerformanceComponent } from './user-performance/user-performance.component';
import { SkillMatrixContainerComponent } from './skill-matrix/skill-matrix-container.component';
import { SkillMatrixSubmitComponent } from './skill-matrix/skill-matrix-submit.component';
import { SkillMatrixMySubmissionsComponent } from './skill-matrix/skill-matrix-my-submissions.component';
import { SkillMatrixApproveRequestsComponent } from './skill-matrix/skill-matrix-approve-requests.component';
import { SkillMatrixMasterConfigurationComponent } from './skill-matrix/skill-matrix-master-configuration.component';
import { UserPoliciesComponent } from './user-policies/user-policies.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';
import { UserSurveyComponent } from './user-survey/user-survey.component';
import { MyTeamComponent } from './user-team/my-team/my-team.component';

import { registerLocaleData } from '@angular/common';
import localeGb from '@angular/common/locales/en-GB';
import { PerformanceConfigComponent } from './configuration/performance-config/performance-config.component';
import { LMSComponent } from './employee360/lms/lms.component';
import { ExpiedPoAndProjectComponent } from './home/expied-po-and-project/expied-po-and-project.component';
import { NavigateToProjectViewDirective } from './navigate-to-project-view.directive';
import { ProjectViewComponent } from './project-view/project-view.component';
import { QrCodeGeneratorComponent } from './qr-code-generator/qr-code-generator.component';
import { MyReimbursementComponent } from './reimbursement/my-reimbursement/my-reimbursement.component';
import { ReimbursementComponent } from './reimbursement/reimbursement.component';
import { ReimbursementapprovalComponent } from './reimbursement/reimbursementapproval/reimbursementapproval.component';
import { ViewReimbursementComponent } from './reimbursement/view-reimbursement/view-reimbursement.component';
import { AppreciationComponent } from './rewards/appreciation/appreciation.component';
import { MyTravelrequestComponent } from './travel-allowance/my-travelrequest/my-travelrequest.component';
import { TravelAllowanceComponent } from './travel-allowance/travel-allowance.component';
import { TravelrequestapprovalComponent } from './travel-allowance/travelrequestapproval/travelrequestapproval.component';
import { ViewTravelrequestComponent } from './travel-allowance/view-travelrequest/view-travelrequest.component';
import { UserReleasenotesComponent } from './user-releasenotes/user-releasenotes.component';
import { AttendanceReconciliationComponent } from './user-report/attendance-reconciliation/attendance-reconciliation.component';
import { QueryMasterComponent } from './user-report/query-master/query-master/query-master.component';
import { ReportDashboardComponent } from './user-report/report-dashboard/report-dashboard.component';
import { ReportListComponent } from './user-report/report-list/report-list.component';
import { UserReportComponent } from './user-report/user-report.component';
import { TeamConfigComponent } from './user-team/team-config/team-config.component';
import { TeamMemberComponent } from './user-team/team-member/team-member.component';
import { UserTeamComponent } from './user-team/user-team.component';
import { MyTimesheetComponent } from './user-timesheet/my-timesheet/my-timesheet.component';
import { TeamTimesheetComponent } from './user-timesheet/team-timesheet/team-timesheet.component';
import { UserTimesheetComponent } from './user-timesheet/user-timesheet.component';
import { DocumentUploadComponent } from './user-update-info/document-upload/document-upload.component';
import { EmployeeInfoComponent } from './user-update-info/employee-info/employee-info.component';
import { EmployeeUpdateListComponent } from './user-update-info/employee-update-list/employee-update-list.component';
import { InformationPreviewComponent } from './user-update-info/information-preview/information-preview.component';
import { UserUpdateInfoComponent } from './user-update-info/user-update-info.component';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';


import { PerformanceDashboardComponent } from './user-performance/performance-dashboard/performance-dashboard.component';
import { TeamDashboardComponent } from './user-performance/team-dashboard/team-dashboard.component';
import { TemplatesComponent } from './user-performance/templates/templates.component';
import { QuarterCycleComponent } from './user-performance/quarter-cycle/quarter-cycle.component';
import { ViewPerformanceComponent } from './user-performance/view-performance/view-performance.component';
import { PerformanceManagementSystemComponent } from './user-performance/performance-management-system/performance-management-system.component';

import { FilterEmployeePipe } from './filter-employee.pipe';
import { ViewEmployeeComponent } from './user-team/resource-management/view-employee/view-employee.component';
import { TotalReimbursementrequestComponent } from './reimbursement/total-reimbursementrequest/total-reimbursementrequest.component';
import { ReimbursementDashboardComponent } from './reimbursement/reimbursement-dashboard/reimbursement-dashboard.component';
import { SafePipe } from './safe.pipe';
import { TotalTravelrequestComponent } from './travel-allowance/total-travelrequest/total-travelrequest.component';
import { BiomaxApprovalComponent } from './user-timesheet/biomax-approval/biomax-approval.component';
import { TravelConfigComponent } from './configuration/travel-config/travel-config.component';
import { ReimbursmentConfigComponent } from './configuration/reimbursment-config/reimbursment-config.component';
import { ReimbursementRuleSetConfigComponent } from './configuration/reimbursement-rule-set-config/reimbursement-rule-set-config.component';
import { TravelApprovalMatrixConfigComponent } from './configuration/travel-approval-matrix-config/travel-approval-matrix-config.component';
import { TravelTicketModalComponent } from './travel-allowance/travel-ticket-modal/travel-ticket-modal.component';
import { HrDashboardComponent } from './user-timesheet/hr-dashboard/hr-dashboard.component';
import { RoMonthYearFieldComponent } from './user-timesheet/hr-dashboard/ro-month-year-field/ro-month-year-field.component';
import { TimesheetConfigComponent } from './configuration/timesheet-config/timesheet-config.component';
import { TeamEmployeeTimesheetViewComponent } from './team-employee-timesheet-view/team-employee-timesheet-view.component';
import { NavigateToTeamEmployeeTimesheetDirective } from './directives/navigate-to-team-employee-timesheet.directive';
import { CalendarViewComponent } from './user-timesheet/calendar-view/calendar-view.component';
import { NavigateToCalenderViewDirective } from './directives/navigate-to-calender-view.directive';
import { ViewImageComponent } from './user-team/view-image/view-image.component';
import { MatPaginatorModule } from '@angular/material/paginator';


import { EncryptionInterceptor } from './helpers/encryption.interceptor';
import { SkillModalComponent } from './user-profile/skill-modal/skill-modal.component';
import { MessageModalComponent } from './user-profile/message-modal/message-modal.component';
import { ConfirmationModalComponent } from './user-profile/confirmation-modal/confirmation-modal.component';
import { CertificateModalComponent } from './user-profile/certificate-modal/certificate-modal.component';
import { SkillCertfificationConfigComponent } from './configuration/skill-certfification-config/skill-certfification-config.component';
import { TrainingConfigComponent } from './configuration/training-config/training-config.component';
import { TrainingComponent } from './training/training.component';
import { SanitizeInterceptor } from './interceptors/sanitize.interceptor';
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { ExperienceDurationPipe } from './pipes/experience-duration.pipe';
import { HomeComponent } from './home/home.component';
import { ConnectionLostComponent } from './connection-lost/connection-lost.component';
import { ProjectColumnFilterPipe } from './project-column-filter.pipe';
import { HighlightPipe } from './highlight.pipe';
import { FormBuilderComponent } from './user-team/form-builder/form-builder.component';
import { FormRendererComponent } from './helpers/form-renderer/form-renderer.component';
import { QuestionRendererComponent } from './helpers/question-renderer/question-renderer.component';
import { DomainComponent } from './user-team/Domain/Domain.component';
import { SubDomainComponent } from './user-team/Domain/SubDomain/SubDomain.component';
import { SubServiceComponent } from './user-team/Domain/SubService/SubService.component';
import { AddDomainDataModalComponent } from './user-team/project-insight/components/add-domain-data/add-domain-data-modal.component';
import { DomainTablesComponent } from './user-team/Domain/DomainTables/DomainTables.component';
import { ProjectInsightDomainModalComponent } from './user-team/Domain/DomainModal/app-project-insight-domain-modal.component';
import { ViewDomainComponent } from './user-team/Domain/ViewDomain/ViewDomain.component';
import { ProjectInsightComponent } from './user-team/project-insight/project-insight.component';
import { ProjectInsightDetailsComponent } from './user-team/project-insight/components/project-insight-details/project-insight-details.component';
import { FilterProjectInsightComponent } from './user-team/project-insight/components/filter-project-insight/filter-project-insight.component';
import { AllProjectInsightDomainsComponent } from './user-team/project-insight/components/all-project-insight-domains/all-project-insight-domains.component';
import { ProjectTableComponent } from './user-team/project-insight/components';
import { LeftSideMenuComponent } from './user-team/project-insight/components';
import { ProjectStaticFormComponent } from './user-team/project-insight/components';
import { QuestionCardsComponent } from './user-team/project-insight/components';
import { ProjectInsightQuestionLibraryComponent } from './user-team/project-insight/components/project-insight-question-library/project-insight-question-library.component';

import { GroupBrowserComponent } from './user-team/project-insight/components/group-browser/group-browser.component';
import { KnowledgeHubComponent } from './user-team/KnowledgeHub/KnowledgeHub.component';
import { ResizableModule } from 'angular-resizable-element';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { ApproverWorkflowComponent } from './user-team/project-insight/components/approver-workflow/approver-workflow.component';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatSliderModule } from '@angular/material/slider';
import { FileUploadComponent } from './helpers/form-renderer/FileUpload/FileUpload.component';
import { LmstabComponent } from './lmstab/lmstab.component';
import { MySelectComponent } from './helpers/my-select/my-select.component';
import { FloatingScrollWrapperComponent } from './helpers/floating-scroll-wrapper/floating-scroll-wrapper.component';
import { MatTimepickerModule } from '@angular/material/timepicker';
import { MyAutocompleteComponent } from './helpers/my-autocomplete/my-autocomplete.component';
import { NgxEditorModule } from 'ngx-editor';
import { CalendarLegendComponent } from "./user-timesheet/shared/calendar-legend/calendar-legend.component";
import { TeamAllTimesheetsTableComponent } from "./user-timesheet/team-timesheet/team-all-timesheets-table/team-all-timesheets-table.component";
import { DateTimePickerComponent } from './helpers/date-time-picker/date-time-picker.component';
import { TimesheetFormComponent } from './user-timesheet/my-timesheet/timesheet-form/timesheet-form.component';
import { InfoTooltipComponent } from './helpers/shared/info-tooltip/info-tooltip.component';
import { MatStepperModule } from '@angular/material/stepper';
import { MatTabsModule } from '@angular/material/tabs';
import { RmgProjectConfigComponent } from './user-team/resource-management/rmg-project-config/rmg-project-config.component';
import { NgbPopoverModule } from '@ng-bootstrap/ng-bootstrap';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatRadioModule} from '@angular/material/radio';
import { ToastrModule } from 'ngx-toastr';
import { MyTableComponent } from './helpers/my-table/my-table.component';
import { RmgStatusCardsComponent } from './user-team/resource-management/rmg-status-cards/rmg-status-cards/rmg-status-cards.component';
import { MatSidenavModule } from '@angular/material/sidenav';
import { QuizSubmit } from './training/quiz-submit/quiz-submit.component';
import { InterviewTrackerComponent } from './interview-tracker/interview-tracker.component';
import { InterviewListComponent } from './interview-tracker/interview-list/interview-list.component';
import { ScheduleInterviewComponent } from './interview-tracker/schedule-interview/schedule-interview.component';
import { ReimbursementTicketModalComponent } from './reimbursement/reimbursement-ticket-modal/reimbursement-ticket-modal.component';
import { OverlayModule } from '@angular/cdk/overlay';
import { PortalModule } from '@angular/cdk/portal';
import { RmgModalHostComponent } from './user-team/resource-management/rmg-modal-host/rmg-modal-host.component';
import { RmgDashboardComponent } from './user-team/resource-management/new-rmg-dashboard/rmg-dashboard/rmg-dashboard.component';
import { RmgProjectTableComponent } from './user-team/resource-management/rmg-project-table/rmg-project-table.component';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { ResizableDirective } from './resizable.directive';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { DdMmYyyyDateAdapter, DD_MM_YYYY_FORMATS } from './dd-mm-yyyy-date-adapter';
import { AclConfigComponent } from 'src/app/configuration/acl-config/acl-config.component';
registerLocaleData(localeGb);


@NgModule({
  declarations: [
    AclConfigComponent,
    AppComponent,
    SidenavComponent,
    BodyComponent,
    HomeComponent,
    UserProfileComponent,
    UserLeavesComponent,
    UserAttendanceComponent,
    UserSalaryComponent,
    UserRequestsComponent,
    UserPerformanceComponent,
    SkillMatrixContainerComponent,
    SkillMatrixSubmitComponent,
    SkillMatrixMySubmissionsComponent,
    SkillMatrixApproveRequestsComponent,
    SkillMatrixMasterConfigurationComponent,
    HrPoliciesComponent,
    HelpdeskComponent,
    GrievanceComponent,
    GrievanceEditComponent,
    GrievanceAuditTimelineComponent,
    GrievanceIssueScenarioAdminComponent,
    ConfigurationComponent,
    RoleConfigComponent,
    DeptConfigComponent,
    EmployeeConfigComponent,
    LoginComponent,
    LeaveComponent,
    HolidaysComponent,
    CompOffComponent,
    LeaveConfigComponent,
    LoaderComponent,
    UserTeamComponent,
    UserTimesheetComponent,
    TeamConfigComponent,
    ViewDomainComponent,
    MyTimesheetComponent,
    TeamTimesheetComponent,
    MyTeamComponent,
    TeamMemberComponent,
    HomeConfigComponent,
    DomainTablesComponent,
    CalendarComponent,
    UserUpdateInfoComponent,
    EmployeeInfoComponent,
    ProjectInsightDomainModalComponent,
    DocumentUploadComponent,
    InformationPreviewComponent,
    EmployeeUpdateListComponent,
    PortalConfigComponent,
    UserReportComponent,
    ReportListComponent,
    CustomFilterComponent,
    ReportDashboardComponent,
    UserSurveyComponent,
    SurveyConfigComponent,
    UserAppreciationComponent,
    UserPoliciesComponent,
    UploadPoliciesComponent,
    RecruitmentComponent,
    OnBoardingComponent,
    UserExitComponent,
    ProjectConfigComponent,
    ResourceManagementComponent,
    ColFilterPipe,
    RmbCamelCaseDisplayPipe,
    SortPipe,
    ColumnFilterBarComponent,
    MultiColFilterPipe,
    DesignationConfigComponent,
    MyResignationComponent,
    ResignationComponent,
    DomainConfigComponent,
    UserReleasenotesComponent,
    NewsletterComponent,
    NewsletterConfigComponent,
    DocumentComponent,
    QueryMasterComponent,
    AttendanceReconciliationComponent,
    OthersComponent,
    RewardsConfigComponent,
    MinutesToHoursPipe,
    RewardsComponent,
    RewardsAndRecognisationComponent,
    RewardFilterPipe,
    Employee360Component,
    NavigateToEmployee360Directive,
    Employee360ProfileComponent,
    Employee360LeaveComponent,
    Employee360ProjectComponent,
    Employee360TimesheetComponent,
    Employee360BiomaxComponent,
    Employee360RewardsComponent,
    Employee360AppreciationComponent,
    Employee360GrievanceComponent,
    DomainComponent,
    AllProjectInsightDomainsComponent,
    SafeHtmlPipe,
    KnowledgeHubComponent,
    BreadcrumbComponent,
    SubDomainComponent,
    FilterProjectInsightComponent,
    SubServiceComponent,
    AppreciationComponent,
    QrCodeGeneratorComponent,
    ExpiedPoAndProjectComponent,
    NavigateToProjectViewDirective,
    ProjectViewComponent,
    PerformanceConfigComponent,
    TravelAllowanceComponent,
    ReimbursementComponent,
    MyReimbursementComponent,
    ViewReimbursementComponent,
    ReimbursementapprovalComponent,
    MyTravelrequestComponent,
    TravelrequestapprovalComponent,
    ViewTravelrequestComponent,
    LMSComponent,
    BiomaxApprovalComponent,
    LmstabComponent,
    FilterEmployeePipe,
    ViewEmployeeComponent,
    TotalTravelrequestComponent,
    TotalReimbursementrequestComponent,
    ReimbursementDashboardComponent,
    ReimbursementTicketModalComponent,
    SafePipe,
    TravelConfigComponent,
    ReimbursmentConfigComponent,
    ReimbursementRuleSetConfigComponent,
    TravelApprovalMatrixConfigComponent,
    TravelTicketModalComponent,
    HrDashboardComponent,
    TimesheetConfigComponent,
    TeamEmployeeTimesheetViewComponent,
    NavigateToTeamEmployeeTimesheetDirective,
    CalendarViewComponent,
    NavigateToCalenderViewDirective,
     SkillModalComponent,
    MessageModalComponent,
    ConfirmationModalComponent,
    CertificateModalComponent,
    SkillCertfificationConfigComponent,
    TrainingConfigComponent,
    TrainingComponent,
    ViewImageComponent,
    ConnectionLostComponent,
    PerformanceDashboardComponent,
    TeamDashboardComponent,
    AddDomainDataModalComponent,
    TemplatesComponent,
    QuarterCycleComponent,
    ViewPerformanceComponent,
    PerformanceManagementSystemComponent,
    HighlightPipe,
    FormBuilderComponent,
    FormRendererComponent,
    QuestionRendererComponent,
    ProjectInsightComponent,
    GroupBrowserComponent,
    ProjectTableComponent,
    LeftSideMenuComponent,
    ProjectStaticFormComponent,
    QuestionCardsComponent,
    ProjectInsightQuestionLibraryComponent,
    FileUploadComponent,
    ApproverWorkflowComponent,
    ProjectInsightDetailsComponent,
    MySelectComponent,
    FloatingScrollWrapperComponent,
    ProjectColumnFilterPipe,
    MyAutocompleteComponent,
    TeamAllTimesheetsTableComponent,
    TimesheetFormComponent,
    InfoTooltipComponent,
    QuizSubmit,
    InterviewTrackerComponent,
    InterviewListComponent,
    ScheduleInterviewComponent,
    RmgProjectConfigComponent,
    MyTableComponent,
    RmgModalHostComponent,
    RmgStatusCardsComponent,
    RmgProjectTableComponent,
    RmgDashboardComponent,
    QuizSubmit,
    ResizableDirective
  ],
  imports: [
    NgbTooltipModule,
    BrowserModule,
    AppRoutingModule,
    MatCardModule,
    MatProgressBarModule,
    MatAutocompleteModule,
    MatIconModule,
    MatSliderModule,
    NgxMatSelectSearchModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatSortModule,
    ClipboardModule,
    MatPaginatorModule,
    MatTableModule,
    DragDropModule,
    MatDialogModule,
    MatTooltipModule,
    PdfViewerModule,
    MatTimepickerModule,
    MatOptionModule,
    ResizableModule,
    MatMenuModule,
    MatButtonModule,
    MatDividerModule,
    MatExpansionModule,
    NgxPaginationModule,
    NgxEditorModule,
    MatStepperModule,
    MatTabsModule,
    NgbPopoverModule,
    MatSlideToggleModule,
    MatRadioModule,
    OverlayModule,
    PortalModule,
    CalendarLegendComponent,
    DateTimePickerComponent,
    RoMonthYearFieldComponent,
    ToastrModule.forRoot({
      positionClass: 'toast-top-right',
      timeOut: 4000,
      closeButton: true,
      preventDuplicates: true,
      newestOnTop: true
    }),
    MatSidenavModule,
    // MatMomentDateModule
    ExperienceDurationPipe,
    NgxEditorModule
  ],
  providers: [
    DatePipe,
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: HTTP_INTERCEPTORS, useClass: EncryptionInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: EmployeePortalInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: SanitizeInterceptor, multi: true },   // Added here for sanitizerInput
    { provide: MAT_DATE_LOCALE, useValue: 'en-GB' },
    { provide: DateAdapter, useClass: DdMmYyyyDateAdapter, deps: [MAT_DATE_LOCALE] },
    { provide: MAT_DATE_FORMATS, useValue: DD_MM_YYYY_FORMATS },  
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
