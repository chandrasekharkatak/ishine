import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { Employee360AppreciationComponent } from 'src/app/employee360/employee360-appreciation/employee360-appreciation.component';
import { Employee360BiomaxComponent } from 'src/app/employee360/employee360-biomax/employee360-biomax.component';
import { Employee360LeaveComponent } from 'src/app/employee360/employee360-leave/employee360-leave.component';
import { Employee360ProfileComponent } from 'src/app/employee360/employee360-profile/employee360-profile.component';
import { Employee360ProjectComponent } from 'src/app/employee360/employee360-project/employee360-project.component';
import { Employee360RewardsComponent } from 'src/app/employee360/employee360-rewards/employee360-rewards.component';
import { Employee360TimesheetComponent } from 'src/app/employee360/employee360-timesheet/employee360-timesheet.component';
import { Employee360Component } from 'src/app/employee360/employee360.component';
import { Employee360Resolver } from 'src/app/employee360/Employee360Resolver';
import { LMSComponent } from 'src/app/employee360/lms/lms.component';

const routes: Routes = [{
  path: '', component: Employee360Component, resolve: { employeeData: Employee360Resolver },
  children: [
    { path: 'profile', component: Employee360ProfileComponent },
    { path: 'leave', component: Employee360LeaveComponent },
    { path: 'project', component: Employee360ProjectComponent },
    { path: 'timesheet', component: Employee360TimesheetComponent },
    { path: 'biomax', component: Employee360BiomaxComponent },
    { path: 'rewards', component: Employee360RewardsComponent },
    { path: 'appreciation', component: Employee360AppreciationComponent },
    { path: 'lms', component: LMSComponent },
  ],
},];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class Employee360RoutingModule { }
