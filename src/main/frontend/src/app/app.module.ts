import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SidenavComponent } from './sidenav/sidenav.component';
import { BodyComponent } from './body/body.component';
import { HomeComponent } from './home/home.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserLeavesComponent } from './user-leaves/user-leaves.component';
import { UserAttendanceComponent } from './user-attendance/user-attendance.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserPerformanceComponent } from './user-performance/user-performance.component';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { AccordionModule } from './accordion/accordion.module';
import { ConfigurationComponent } from './configuration/configuration.component';
import { RoleConfigComponent } from './configuration/role-config/role-config.component';
import { DeptConfigComponent } from './configuration/dept-config/dept-config.component';
import { EmployeeConfigComponent } from './configuration/employee-config/employee-config.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { DatePipe, HashLocationStrategy, LocationStrategy } from '@angular/common';
import { BsModalService, ModalModule } from 'ngx-bootstrap/modal';
import { LoginComponent } from './login/login.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { MatSelectModule } from '@angular/material/select';
import { LeaveComponent } from './user-leaves/leave/leave.component';
import { HolidaysComponent } from './user-leaves/holidays/holidays.component';
import { CompOffComponent } from './user-leaves/comp-off/comp-off.component';
import { LeaveConfigComponent } from './configuration/leave-config/leave-config.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { LoaderComponent } from './loader/loader.component';
import { LoaderInterceptor } from './helpers/loader.interceptor';
import { UserTeamComponent } from './user-team/user-team.component';
import { UserTimesheetComponent } from './user-timesheet/user-timesheet.component';
import { TeamConfigComponent } from './user-team/team-config/team-config.component';
import { MyTimesheetComponent } from './user-timesheet/my-timesheet/my-timesheet.component';
import { TeamTimesheetComponent } from './user-timesheet/team-timesheet/team-timesheet.component';
import { MyTeamComponent } from './user-team/my-team/my-team.component';
import { TeamMemberComponent } from './user-team/team-member/team-member.component';
import { NgxPaginationModule } from 'ngx-pagination';
import { MatListModule } from '@angular/material/list';
import { HomeConfigComponent } from './configuration/home-config/home-config.component';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { Ng2SearchPipeModule } from 'ng2-search-filter';
import { ClipboardModule } from 'ngx-clipboard';
import { CalendarComponent } from './helpers/calendar/calendar.component';
import { EmployeePortalInterceptor } from './helpers/employeePortal.interceptor';
import { UserUpdateInfoComponent } from './user-update-info/user-update-info.component';
import { EmployeeInfoComponent } from './user-update-info/employee-info/employee-info.component';
import { DocumentUploadComponent } from './user-update-info/document-upload/document-upload.component';
import { InformationPreviewComponent } from './user-update-info/information-preview/information-preview.component';
import { EmployeeUpdateListComponent } from './user-update-info/employee-update-list/employee-update-list.component';
import { PortalConfigComponent } from './configuration/portal-config/portal-config.component';
import { UserReportComponent } from './user-report/user-report.component';
import { ReportListComponent } from './user-report/report-list/report-list.component';
import { CustomFilterComponent } from './helpers/custom-filter/custom-filter.component';
import { ReportDashboardComponent } from './user-report/report-dashboard/report-dashboard.component';
import { UserSurveyComponent } from './user-survey/user-survey.component';
import { SurveyConfigComponent } from './configuration/survey-config/survey-config.component';
import { UserAppreciationComponent } from './user-appreciation/user-appreciation.component';
import { UserPoliciesComponent } from './user-policies/user-policies.component';
import { UploadPoliciesComponent } from './configuration/upload-policies/upload-policies.component';
import { RecruitmentComponent } from './recruitment/recruitment.component';
import { NgHorizontalScrollModule } from 'angular-horizontal-scroll-table';
import { OnBoardingComponent } from './configuration/on-boarding/on-boarding.component';
import { UserExitComponent } from './user-exit/user-exit.component';
import { MatTableModule } from '@angular/material/table';
import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgxMatSelectModule } from 'ngx-mat-select';
import { ProjectConfigComponent } from './configuration/project-config/project-config.component';
import { NgxOrgChartModule } from 'ngx-org-chart';
import { OwlDateTimeModule, OwlNativeDateTimeModule } from 'ng-pick-datetime';
import {AutocompleteLibModule} from 'angular-ng-autocomplete';
import { ResourceManagementComponent } from './user-team/resource-management/resource-management.component';
import {ColFilterPipe} from './col-filter.pipe';

import { SortPipe } from './sort.pipe';
import { ColumnFilterBarComponent } from './helpers/column-filter-bar/column-filter-bar.component';
import { MultiColFilterPipe } from './multi-col-filter.pipe';
import { PdfViewerModule } from 'ng2-pdf-viewer';
import { DesignationConfigComponent } from './configuration/designation-config/designation-config.component';
import { AngularEditorModule } from '@kolkov/angular-editor';
import { MyResignationComponent } from './user-exit/my-resignation/my-resignation.component';
import { ResignationComponent } from './user-exit/resignation/resignation.component';
import { DomainConfigComponent } from './configuration/domain-config/domain-config.component';
import { UserReleasenotesComponent } from './user-releasenotes/user-releasenotes.component';
import { NewsletterComponent } from './newsletter/newsletter.component';
import { NewsletterConfigComponent } from './configuration/newsletter-config/newsletter-config.component';
import { DocumentComponent } from './configuration/document/document.component';
import { QueryMasterComponent } from './user-report/query-master/query-master/query-master.component';
import { AttendanceReconciliationComponent } from './user-report/attendance-reconciliation/attendance-reconciliation.component';
import { OthersComponent } from './configuration/others/others.component';
import { RewardsConfigComponent } from './configuration/rewards-config/rewards-config.component';
import { MinutesToHoursPipe } from './minutes-to-hours.pipe';
import { RewardsComponent } from './rewards/rewards.component';
import { RewardsAndRecognisationComponent } from './rewards/rewards-and-recognisation/rewards-and-recognisation.component';
import { RewardFilterPipe } from './reward-filter.pipe';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatOptionModule } from '@angular/material/core';
import { Employee360Component } from './employee360/employee360.component';
import { NavigateToEmployee360Directive } from './navigate-to-employee360.directive';
import { Employee360ProfileComponent } from './employee360/employee360-profile/employee360-profile.component';
import { Employee360LeaveComponent } from './employee360/employee360-leave/employee360-leave.component';
import { Employee360ProjectComponent } from './employee360/employee360-project/employee360-project.component';
import { Employee360TimesheetComponent } from './employee360/employee360-timesheet/employee360-timesheet.component';
import { Employee360BiomaxComponent } from './employee360/employee360-biomax/employee360-biomax.component';
import { Employee360RewardsComponent } from './employee360/employee360-rewards/employee360-rewards.component';
import { Employee360AppreciationComponent } from './employee360/employee360-appreciation/employee360-appreciation.component';
import { BreadcrumbComponent } from './helpers/breadcrumb/breadcrumb.component';
//import { TestComponent } from './user-report/test/test.component';
  // Import Owl DateTime modules

@NgModule({
  declarations: [
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
    HrPoliciesComponent,
    HelpdeskComponent,
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
    MyTimesheetComponent,
    TeamTimesheetComponent,
    MyTeamComponent,
    TeamMemberComponent,
    HomeConfigComponent,
    CalendarComponent,
    UserUpdateInfoComponent,
    EmployeeInfoComponent,
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
<<<<<<< Updated upstream
    BreadcrumbComponent
=======
    Employee360BiomaxComponent
>>>>>>> Stashed changes
    //TestComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatAutocompleteModule,
    AccordionModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ModalModule.forRoot(),
    BrowserAnimationsModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatFormFieldModule,
    MatInputModule,
    NgxPaginationModule,
    MatListModule,
    MatSortModule,
    Ng2SearchPipeModule,
    ClipboardModule,
    NgHorizontalScrollModule,
    MatTableModule,
    DragDropModule,
    NgxMatSelectModule.forRoot({
      maxWidthForMobileView: 600,
      inFirstLoadCallSearcher: true,
      inFirstLoadSearcherValue: '',
      emptyLabel: 'no entry found',
      noMoreResultLabel: ' ',
      useInfiniteScroll: false,
      searchBoxPlaceholder: 'search',
      maximumResultForShow: 40,
      useMobileView: false,
      valueMember: 'key',
      displayMember: 'value',
      mobileViewType: 'FullScreen'
  }),
    NgxOrgChartModule,
    OwlDateTimeModule,
    OwlNativeDateTimeModule,
    AutocompleteLibModule,
    PdfViewerModule,
    AngularEditorModule,
    MatInputModule,
    MatFormFieldModule,
    MatOptionModule,
  ],
  providers: [
    BsModalService,
    DatePipe,
    { provide: LocationStrategy, useClass: HashLocationStrategy },
    { provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: EmployeePortalInterceptor, multi: true },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
