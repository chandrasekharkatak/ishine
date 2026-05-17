import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConnectionLostComponent } from './connection-lost/connection-lost.component';
import { AuthGuard } from './guards/auth.guard';
import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { HomeComponent } from './home/home.component';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { LmstabComponent } from './lmstab/lmstab.component';
import { LoginComponent } from './login/login.component';
import { NewsletterComponent } from './newsletter/newsletter.component';
import { ProjectViewComponent } from './project-view/project-view.component';
import { QrCodeGeneratorComponent } from './qr-code-generator/qr-code-generator.component';
import { RecruitmentComponent } from './recruitment/recruitment.component';
import { TeamEmployeeTimesheetViewComponent } from './team-employee-timesheet-view/team-employee-timesheet-view.component';
import { GrievanceComponent } from './grievance/grievance.component';
import { GrievanceEditComponent } from './grievance/grievance-edit.component';
import { GrievanceIssueScenarioAdminComponent } from './grievance/grievance-issue-scenario-admin.component';
import { UserAppreciationComponent } from './user-appreciation/user-appreciation.component';
import { UserAttendanceComponent } from './user-attendance/user-attendance.component';
import { UserPoliciesComponent } from './user-policies/user-policies.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserReleasenotesComponent } from './user-releasenotes/user-releasenotes.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';
import { UserSurveyComponent } from './user-survey/user-survey.component';
import { CalendarViewComponent } from './user-timesheet/calendar-view/calendar-view.component';
import { TrainingComponent } from './training/training.component';

const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [AuthGuard] },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'calendar-view', component: CalendarViewComponent },
  {
    path: 'update-info',
    loadChildren: () => import('./module-routing/update-info/update-info.module').then(m => m.UpdateInfoModule),
    canActivate: [AuthGuard]
  },
  { path: 'user-profile', component: UserProfileComponent, canActivate: [AuthGuard] },
  {
    path: 'configuration',
    loadChildren: () => import('./module-routing/configuration/configuration.module').then(m => m.ConfigurationModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user-leaves',
    loadChildren: () => import('./module-routing/user-leaves/user-leaves.module').then(m => m.UserLeavesModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user-timesheet',
    loadChildren: () => import('./module-routing/user-timesheet/user-timesheet.module').then(m => m.UserTimesheetModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user-leaves/:tabName',
    loadChildren: () => import('./module-routing/user-leaves-tabname/user-leaves-tabname.module').then(m => m.UserLeavesTabnameModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user-timesheet/:tabName',
    loadChildren: () => import('./module-routing/user-timesheet-tabname/user-timesheet-tabname.module').then(m => m.UserTimesheetTabnameModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'project-insight',
    loadChildren: () => import('./module-routing/project-insight/project-insight.module').then(m => m.ProjectInsightModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user-team',
    loadChildren: () => import('./module-routing/user-team/user-team.module').then(m => m.UserTeamModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'user-reports',
    loadChildren: () => import('./module-routing/user-reports/user-reports.module').then(m => m.UserReportsModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'rewards-tab',
    loadChildren: () => import('./module-routing/rewards-tab/rewards-tab.module').then(m => m.RewardsTabModule),
    canActivate: [AuthGuard]
  },
  { path: 'user-appreciation', component: UserAppreciationComponent, canActivate: [AuthGuard] },
  {
    path: 'employee-360/:id',
    loadChildren: () => import('./module-routing/employee360/employee360.module').then(m => m.Employee360Module)
  },
  { path: 'project-view', component: ProjectViewComponent },
  { path: 'team-employee-timesheet', component: TeamEmployeeTimesheetViewComponent, canActivate: [AuthGuard] },
  { path: 'lms-tab', component: LmstabComponent, canActivate: [AuthGuard] },
  { path: 'user-policies', component: UserPoliciesComponent, canActivate: [AuthGuard] },
  { path: 'user-survey', component: UserSurveyComponent, canActivate: [AuthGuard] },
  { path: 'user-survey/:id', component: UserSurveyComponent, canActivate: [AuthGuard] },
  { path: 'user-survey/:id/edit', component: UserSurveyComponent, canActivate: [AuthGuard] },
  { path: 'recruitment', component: RecruitmentComponent, canActivate: [AuthGuard] },
  {
    path: 'user-exit',
    loadChildren: () => import('./module-routing/user-exit/user-exit.module').then(m => m.UserExitModule),
    canActivate: [AuthGuard]
  },
  { path: 'user-attendance', component: UserAttendanceComponent, canActivate: [AuthGuard] },
  { path: 'user-salary', component: UserSalaryComponent, canActivate: [AuthGuard] },
  { path: 'user-requests', component: UserRequestsComponent, canActivate: [AuthGuard] },
  {
    path: 'user-performance',
    loadChildren: () => import('./module-routing/user-performance/user-performance.module').then(m => m.UserPerformanceModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'skill-matrix',
    loadChildren: () => import('./module-routing/skill-matrix/skill-matrix.module').then(m => m.SkillMatrixModule),
    canActivate: [AuthGuard]
  },
  { path: 'hr-policies', component: HrPoliciesComponent, canActivate: [AuthGuard] },
  { path: 'helpdesk', component: HelpdeskComponent },
  { path: 'helpdesk/:id', component: HelpdeskComponent, canActivate: [AuthGuard] },
  { path: 'release-notes', component: UserReleasenotesComponent, canActivate: [AuthGuard] },
  { path: 'newsletters', component: NewsletterComponent, canActivate: [AuthGuard] },
  { path: 'training', component: TrainingComponent, canActivate: [AuthGuard] },
  { path: 'user-training', component: TrainingComponent, canActivate: [AuthGuard] },
  { path: 'grievance/issue-scenarios', component: GrievanceIssueScenarioAdminComponent, canActivate: [AuthGuard] },
  { path: 'grievance/:ticketId/edit', component: GrievanceEditComponent, canActivate: [AuthGuard] },
  { path: 'grievance', component: GrievanceComponent, canActivate: [AuthGuard] },
  { path: 'tgrievance/:category/:subcategory', component: GrievanceComponent },
  {
    path: 'travelDesk',
    loadChildren: () => import('./module-routing/travel-desk/travel-desk.module').then(m => m.TravelDeskModule),
    canActivate: [AuthGuard]
  },
  {
    path: 'reimbursement',
    loadChildren: () => import('./module-routing/reimbursement/reimbursement.module').then(m => m.ReimbursementModule),
    canActivate: [AuthGuard]
  },
  { path: 'qr-code', component: QrCodeGeneratorComponent, canActivate: [AuthGuard] },
  { path: '**', component: ConnectionLostComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }