import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ConfigurationComponent } from './configuration/configuration.component';
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
  {path:'', redirectTo:'login', pathMatch:'full'},
  {path:'login', component: LoginComponent},
  {path:'home', component: HomeComponent},
  {path:'user-profile', component: UserProfileComponent},
  {path:'configuration', component: ConfigurationComponent},
  {path:'user-leaves', component: UserLeavesComponent},
  {path:'user-attendance', component: UserAttendanceComponent},
  {path:'user-salary', component: UserSalaryComponent},
  {path:'user-requests', component: UserRequestsComponent},
  {path:'user-performance', component: UserPerformanceComponent},
  {path:'hr-policies', component: HrPoliciesComponent},
  {path:'helpdesk', component: HelpdeskComponent},
  {path:'**', redirectTo:'home', pathMatch:'full'},
];

@NgModule({
  imports: [RouterModule.forRoot(routes,{ onSameUrlNavigation: 'reload' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
