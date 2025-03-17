import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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
import { RewardsConfigComponent } from './configuration/rewards-config/rewards-config.component';
import { RoleConfigComponent } from './configuration/role-config/role-config.component';
import { SurveyConfigComponent } from './configuration/survey-config/survey-config.component';
import { UploadPoliciesComponent } from './configuration/upload-policies/upload-policies.component';
import { Employee360AppreciationComponent } from './employee360/employee360-appreciation/employee360-appreciation.component';
import { Employee360BiomaxComponent } from './employee360/employee360-biomax/employee360-biomax.component';
import { Employee360LeaveComponent } from './employee360/employee360-leave/employee360-leave.component';
import { Employee360ProfileComponent } from './employee360/employee360-profile/employee360-profile.component';
import { Employee360ProjectComponent } from './employee360/employee360-project/employee360-project.component';
import { Employee360RewardsComponent } from './employee360/employee360-rewards/employee360-rewards.component';
import { Employee360TimesheetComponent } from './employee360/employee360-timesheet/employee360-timesheet.component';
import { Employee360Component } from './employee360/employee360.component';
import { AuthGuard } from './guards/auth.guard';
import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { HomeComponent } from './home/home.component';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { LoginComponent } from './login/login.component';
import { NewsletterComponent } from './newsletter/newsletter.component';
import { RecruitmentComponent } from './recruitment/recruitment.component';
import { AppreciationComponent } from './rewards/appreciation/appreciation.component';
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
import { UserPoliciesComponent } from './user-policies/user-policies.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserReleasenotesComponent } from './user-releasenotes/user-releasenotes.component';
import { AttendanceReconciliationComponent } from './user-report/attendance-reconciliation/attendance-reconciliation.component';
import { QueryMasterComponent } from './user-report/query-master/query-master/query-master.component';
import { ReportDashboardComponent } from './user-report/report-dashboard/report-dashboard.component';
import { ReportListComponent } from './user-report/report-list/report-list.component';
import { UserReportComponent } from './user-report/user-report.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';
import { UserSurveyComponent } from './user-survey/user-survey.component';
import { MyTeamComponent } from './user-team/my-team/my-team.component';
import { ResourceManagementComponent } from './user-team/resource-management/resource-management.component';
import { TeamConfigComponent } from './user-team/team-config/team-config.component';
import { TeamMemberComponent } from './user-team/team-member/team-member.component';
import { UserTeamComponent } from './user-team/user-team.component';
import { MyTimesheetComponent } from './user-timesheet/my-timesheet/my-timesheet.component';
import { TeamTimesheetComponent } from './user-timesheet/team-timesheet/team-timesheet.component';
import { UserTimesheetComponent } from './user-timesheet/user-timesheet.component';
import { DocumentUploadComponent } from './user-update-info/document-upload/document-upload.component';
import { EmployeeInfoComponent } from './user-update-info/employee-info/employee-info.component';
import { InformationPreviewComponent } from './user-update-info/information-preview/information-preview.component';
import { UserUpdateInfoComponent } from './user-update-info/user-update-info.component';
import { ProjectViewComponent } from './project-view/project-view.component';
import { PerformanceConfigComponent } from './configuration/performance-config/performance-config.component';
<<<<<<< HEAD
import { TravelAllowanceComponent } from './travel-allowance/travel-allowance.component';
=======
import { ReimbursementComponent } from './reimbursement/reimbursement.component';
>>>>>>> d70348accb6e0391bd8323274a3554473d37bade

const routes: Routes = [
  {path:'', redirectTo:'home', pathMatch:'full'},
  {path:'login', component: LoginComponent, canActivate: [AuthGuard]},
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
      { path: 'domain', component: DomainConfigComponent, },
      { path: 'department', component: DeptConfigComponent, },
      { path: 'role', component: RoleConfigComponent, },
      { path: 'leave', component: LeaveConfigComponent, },
    //  { path: 'team', component: TeamConfigComponent, },
      { path: 'home-config', component: HomeConfigComponent, },
      { path: 'portal-config', component: PortalConfigComponent, },
      { path: 'survey-config', component: SurveyConfigComponent, },
      { path: 'upload-policies', component: UploadPoliciesComponent, },
      { path: 'on-boarding', component: OnBoardingComponent, },
      { path: 'designation', component: DesignationConfigComponent, },
      { path: 'newsletter', component: NewsletterConfigComponent, },
      { path: 'document', component: DocumentComponent, },
      { path: 'other', component: OthersComponent, },
      { path: 'rewards-config', component: RewardsConfigComponent, },
      { path: 'performance-config' , component: PerformanceConfigComponent},
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
      { path: 'resource-management', component: ResourceManagementComponent, },
      { path:'resource-management/:id', component: ResourceManagementComponent,},
    ]
  },
  {path:'user-reports', component: UserReportComponent, canActivate: [AuthGuard],
    children: [
      { path: 'report-list', component: ReportListComponent, },
      { path: 'report-dashboard', component: ReportDashboardComponent },
      { path: 'query-master', component: QueryMasterComponent},
      { path: 'attendance-reconciliation', component: AttendanceReconciliationComponent},
    ]
  },
  {path:'rewards-tab', component: RewardsComponent, canActivate: [AuthGuard],
       children: [
      { path: 'rewards-and-recognisation', component: RewardsAndRecognisationComponent, },
      { path: 'rewardsappreciation', component: AppreciationComponent,},
    ]
  },
  {path:'user-appreciation', component: UserAppreciationComponent, canActivate: [AuthGuard]},
  
  {path:'employee-360', component: Employee360Component,
     children: [
      { path: 'profile', component: Employee360ProfileComponent, },
      { path:'leave', component: Employee360LeaveComponent, },
      { path: 'project', component: Employee360ProjectComponent, },
      { path: 'timesheet', component: Employee360TimesheetComponent, },
      { path: 'biomax', component: Employee360BiomaxComponent, },
      { path: 'rewards', component: Employee360RewardsComponent, },
      { path: 'appreciation', component: Employee360AppreciationComponent, },
    ]
  },
  {path:'project-view', component: ProjectViewComponent},
  {path:'user-policies', component: UserPoliciesComponent, canActivate: [AuthGuard]},
  {path:'user-survey', component: UserSurveyComponent, canActivate: [AuthGuard]},
  {path:'user-survey/:id', component: UserSurveyComponent, canActivate: [AuthGuard]},
  { path: 'user-survey/:id/edit', component: UserSurveyComponent, canActivate: [AuthGuard] },
  {path:'recruitment', component: RecruitmentComponent, canActivate: [AuthGuard]},
  {path: 'user-exit', component: UserExitComponent, canActivate: [AuthGuard],
    children: [
      { path: 'my-resignation', component: MyResignationComponent, },
      { path:'my-resignation/:id', component: MyResignationComponent, },
      { path: 'resignation', component: ResignationComponent, },
    ]
  },
  {path:'user-attendance', component: UserAttendanceComponent, canActivate: [AuthGuard]},
  {path:'user-salary', component: UserSalaryComponent, canActivate: [AuthGuard]},
  {path:'user-requests', component: UserRequestsComponent, canActivate: [AuthGuard]},
  {path:'user-performance', component: UserPerformanceComponent, canActivate: [AuthGuard]},
  {path:'hr-policies', component: HrPoliciesComponent, canActivate: [AuthGuard]},
  {path:'helpdesk', component: HelpdeskComponent},
  {path:'helpdesk/:id', component: HelpdeskComponent, canActivate: [AuthGuard]},
  {path:'release-notes', component: UserReleasenotesComponent, canActivate: [AuthGuard]},
  {path:'newsletters', component: NewsletterComponent, canActivate: [AuthGuard]},
  {path:'travelDesk', component: TravelAllowanceComponent, canActivate: [AuthGuard]},
  {path:'reimbursement', component: ReimbursementComponent, canActivate: [AuthGuard]},
  {path:'**', redirectTo:'home', pathMatch:'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes,{ onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }