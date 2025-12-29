import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AttendanceReconciliationComponent } from 'src/app/user-report/attendance-reconciliation/attendance-reconciliation.component';
import { QueryMasterComponent } from 'src/app/user-report/query-master/query-master/query-master.component';
import { ReportDashboardComponent } from 'src/app/user-report/report-dashboard/report-dashboard.component';
import { ReportListComponent } from 'src/app/user-report/report-list/report-list.component';
import { UserReportComponent } from 'src/app/user-report/user-report.component';

const routes: Routes = [{
  path: '', component: UserReportComponent,
  children: [
    { path: 'report-list', component: ReportListComponent, },
    { path: 'report-dashboard', component: ReportDashboardComponent },
    { path: 'query-master', component: QueryMasterComponent },
    { path: 'attendance-reconciliation', component: AttendanceReconciliationComponent },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserReportsRoutingModule { }
