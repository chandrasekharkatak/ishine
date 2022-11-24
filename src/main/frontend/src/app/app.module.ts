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
import { OnBoardingComponent } from './configuration/on-boarding/on-boarding.component';
import { UserExitComponent } from './user-exit/user-exit.component';
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
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
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
