import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigurationComponent } from './configuration/configuration.component';
import { AuthGuard } from './guards/auth.guard';
import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { HomeComponent } from './home/home.component';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { LoginComponent } from './login/login.component';
import { UserAttendanceComponent } from './user-attendance/user-attendance.component';
import { UserLeavesComponent } from './user-leaves/user-leaves.component';
import { UserPerformanceComponent } from './user-performance/user-performance.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';

const routes: Routes = [
  {path:'', redirectTo:'home', pathMatch:'full'},
  {path:'login', component: LoginComponent},
  {path:'home', component: HomeComponent, canActivate: [AuthGuard]},
  {path:'user-profile', component: UserProfileComponent, canActivate: [AuthGuard]},
  {path:'configuration', component: ConfigurationComponent, canActivate: [AuthGuard]},
  {path:'user-leaves', component: UserLeavesComponent, canActivate: [AuthGuard]},
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
