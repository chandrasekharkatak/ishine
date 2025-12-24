import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BiomaxApprovalComponent } from 'src/app/user-timesheet/biomax-approval/biomax-approval.component';
import { CalendarViewComponent } from 'src/app/user-timesheet/calendar-view/calendar-view.component';
import { HrDashboardComponent } from 'src/app/user-timesheet/hr-dashboard/hr-dashboard.component';
import { MyTimesheetComponent } from 'src/app/user-timesheet/my-timesheet/my-timesheet.component';
import { TeamTimesheetComponent } from 'src/app/user-timesheet/team-timesheet/team-timesheet.component';
import { UserTimesheetComponent } from 'src/app/user-timesheet/user-timesheet.component';

const routes: Routes = [{
  path: '', component: UserTimesheetComponent,
  children: [
    { path: 'my-timesheet', component: MyTimesheetComponent, },
    { path: 'team-timesheet', component: TeamTimesheetComponent, },
    { path: 'biomax-request', component: BiomaxApprovalComponent, },
    { path: 'hr-dashboard', component: HrDashboardComponent, },
    { path: 'calendar-view', component: CalendarViewComponent, },
  ]
},];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserTimesheetRoutingModule { }
