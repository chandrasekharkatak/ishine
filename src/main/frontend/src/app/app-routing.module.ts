import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigurationComponent } from './configuration/configuration.component';
import { DeptConfigComponent } from './configuration/dept-config/dept-config.component';
import { EmployeeConfigComponent } from './configuration/employee-config/employee-config.component';
import { LeaveConfigComponent } from './configuration/leave-config/leave-config.component';
import { RoleConfigComponent } from './configuration/role-config/role-config.component';
import { TeamConfigComponent } from './configuration/team-config/team-config.component';
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
import { UserTeamComponent } from './user-team/user-team.component';
import { MyTimesheetComponent } from './user-timesheet/my-timesheet/my-timesheet.component';
import { TeamTimesheetComponent } from './user-timesheet/team-timesheet/team-timesheet.component';
import { UserTimesheetComponent } from './user-timesheet/user-timesheet.component';

const routes: Routes = [
  {path:'', redirectTo:'home', pathMatch:'full'},
  {path:'login', component: LoginComponent},
  {path:'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path:'user-profile', component: UserProfileComponent, canActivate: [AuthGuard]},
  {path:'configuration', component: ConfigurationComponent, canActivate: [AuthGuard], 
    children: [
      { path: 'employee', component: EmployeeConfigComponent, },
      { path: 'department', component: DeptConfigComponent, },
      { path: 'role', component: RoleConfigComponent, },
      { path: 'leave', component: LeaveConfigComponent, },
      { path: 'team', component: TeamConfigComponent, },
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
  {path:'user-team', component: UserTeamComponent, canActivate: [AuthGuard]},
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
