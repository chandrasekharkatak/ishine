import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigurationComponent } from './configuration/configuration.component';
import { DeptConfigComponent } from './configuration/dept-config/dept-config.component';
import { EmployeeConfigComponent } from './configuration/employee-config/employee-config.component';
import { HomeConfigComponent } from './configuration/home-config/home-config.component';
import { LeaveConfigComponent } from './configuration/leave-config/leave-config.component';
import { RoleConfigComponent } from './configuration/role-config/role-config.component';
import { TeamConfigComponent } from './user-team/team-config/team-config.component';
import { AuthGuard } from './guards/auth.guard';
import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { HomeComponent } from './home/home.component';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { LoginComponent } from './login/login.component';
import { UserAttendanceComponent } from './user-attendance/user-attendance.component';
import { CompOffComponent } from './user-leaves/comp-off/comp-off.component';
import { HolidaysComponent } from './user-leaves/holidays/holidays.component';
import { LeaveComponent } from './user-leaves/leave/leave.component';
import { UserLeavesComponent } from './user-leaves/user-leaves.component';
import { UserPerformanceComponent } from './user-performance/user-performance.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';
import { MyTeamComponent } from './user-team/my-team/my-team.component';
import { TeamMemberComponent } from './user-team/team-member/team-member.component';
import { UserTeamComponent } from './user-team/user-team.component';
import { MyTimesheetComponent } from './user-timesheet/my-timesheet/my-timesheet.component';
import { TeamTimesheetComponent } from './user-timesheet/team-timesheet/team-timesheet.component';
import { UserTimesheetComponent } from './user-timesheet/user-timesheet.component';
import { DocumentUploadComponent } from './user-update-info/document-upload/document-upload.component';
import { EmployeeInfoComponent } from './user-update-info/employee-info/employee-info.component';
import { InformationPreviewComponent } from './user-update-info/information-preview/information-preview.component';
import { UserUpdateInfoComponent } from './user-update-info/user-update-info.component';
import { PortalConfigComponent } from './configuration/portal-config/portal-config.component';
import { UserReportComponent } from './user-report/user-report.component';
import { ReportListComponent } from './user-report/report-list/report-list.component';
import { ReportDashboardComponent } from './user-report/report-dashboard/report-dashboard.component';
import { SurveyConfigComponent } from './configuration/survey-config/survey-config.component';
import { UserAppreciationComponent } from './user-appreciation/user-appreciation.component';
import { UserPoliciesComponent } from './user-policies/user-policies.component';
import { UploadPoliciesComponent } from './configuration/upload-policies/upload-policies.component';
import { UserSurveyComponent } from './user-survey/user-survey.component';
import { RecruitmentComponent } from './recruitment/recruitment.component';
import { OnBoardingComponent } from './configuration/on-boarding/on-boarding.component';
import { UserExitComponent } from './user-exit/user-exit.component';

const routes: Routes = [
  {path:'', redirectTo:'home', pathMatch:'full'},
  {path:'login', component: LoginComponent},
  {path:'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path:'update-info', component: UserUpdateInfoComponent, canActivate: [AuthGuard],
    children: [
      { path: 'employee-info', component: EmployeeInfoComponent, },
      { path: 'document-upload', component: DocumentUploadComponent, },
      { path: 'info-preview', component: InformationPreviewComponent, },
    ]
  },
  {path:'user-profile', component: UserProfileComponent, canActivate: [AuthGuard]},
  {path:'configuration', component: ConfigurationComponent, canActivate: [AuthGuard], 
    children: [
      { path: 'employee', component: EmployeeConfigComponent, },
      { path: 'department', component: DeptConfigComponent, },
      { path: 'role', component: RoleConfigComponent, },
      { path: 'leave', component: LeaveConfigComponent, },
     // { path: 'team', component: TeamConfigComponent, },
      { path: 'home-config', component: HomeConfigComponent, },
      { path: 'portal-config', component: PortalConfigComponent, },
      { path: 'survey-config', component: SurveyConfigComponent, },
      { path: 'upload-policies', component: UploadPoliciesComponent, },
      { path: 'on-boarding', component: OnBoardingComponent, },
    ]
  },
  {path:'user-leaves', component: UserLeavesComponent, canActivate: [AuthGuard],
    children: [
      { path: 'leave', component: LeaveComponent, },
      { path: 'holiday', component: HolidaysComponent, },
      { path: 'compOff', component: CompOffComponent, },
    ]
  },
  {path:'user-timesheet', component: UserTimesheetComponent, canActivate: [AuthGuard],
    children: [
      { path: 'my-timesheet', component: MyTimesheetComponent, },
      { path: 'team-timesheet', component: TeamTimesheetComponent, },
    ]
  },
  {path:'user-leaves/:tabName', component: UserLeavesComponent, canActivate: [AuthGuard],
    children: [
      { path: 'leave', component: LeaveComponent, },
      { path: 'holiday', component: HolidaysComponent, },
      { path: 'compOff', component: CompOffComponent, },
    ]
  },
  {path:'user-timesheet/:tabName', component: UserTimesheetComponent, canActivate: [AuthGuard],
    children: [
      { path: 'my-timesheet', component: MyTimesheetComponent, },
      { path: 'team-timesheet', component: TeamTimesheetComponent, },
    ]
  },
  {path:'user-team', component: UserTeamComponent, canActivate: [AuthGuard],
    children: [
      { path: 'my-team', component: MyTeamComponent, },
      { path: 'team-member', component: TeamMemberComponent, },
      { path: 'team-config', component: TeamConfigComponent, },

    ]
  },
  {path:'user-reports', component: UserReportComponent, canActivate: [AuthGuard],
    children: [
      { path: 'report-list', component: ReportListComponent, },
      { path: 'report-dashboard', component: ReportDashboardComponent }
    ]
  },
  {path:'user-appreciation', component: UserAppreciationComponent, canActivate: [AuthGuard]},
  {path:'user-policies', component: UserPoliciesComponent, canActivate: [AuthGuard]},
  {path:'user-survey', component: UserSurveyComponent, canActivate: [AuthGuard]},
  {path:'recruitment', component: RecruitmentComponent, canActivate: [AuthGuard]},
  {path:'user-exit', component: UserExitComponent, canActivate: [AuthGuard]},

  {path:'user-attendance', component: UserAttendanceComponent, canActivate: [AuthGuard]},
  {path:'user-salary', component: UserSalaryComponent, canActivate: [AuthGuard]},
  {path:'user-requests', component: UserRequestsComponent, canActivate: [AuthGuard]},
  {path:'user-performance', component: UserPerformanceComponent, canActivate: [AuthGuard]},
  {path:'hr-policies', component: HrPoliciesComponent, canActivate: [AuthGuard]},
  {path:'helpdesk', component: HelpdeskComponent, canActivate: [AuthGuard]},
  {path:'**', redirectTo:'home', pathMatch:'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes,{ onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
