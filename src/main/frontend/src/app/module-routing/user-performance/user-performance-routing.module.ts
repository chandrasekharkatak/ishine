import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PerformanceDashboardComponent } from 'src/app/user-performance/performance-dashboard/performance-dashboard.component';
import { PerformanceManagementSystemComponent } from 'src/app/user-performance/performance-management-system/performance-management-system.component';
import { QuarterCycleComponent } from 'src/app/user-performance/quarter-cycle/quarter-cycle.component';
import { TeamDashboardComponent } from 'src/app/user-performance/team-dashboard/team-dashboard.component';
import { TemplatesComponent } from 'src/app/user-performance/templates/templates.component';
import { UserPerformanceComponent } from 'src/app/user-performance/user-performance.component';
import { ViewPerformanceComponent } from 'src/app/user-performance/view-performance/view-performance.component';

const routes: Routes = [{
  path: '', component: UserPerformanceComponent,
  children: [
    { path: '', redirectTo: 'performance-dashboard', pathMatch: 'full' },
    { path: 'performance-dashboard', component: PerformanceDashboardComponent },
    { path: 'team-dashboard', component: TeamDashboardComponent },
    { path: 'templates', component: TemplatesComponent },
    { path: 'quarter-cycle', component: QuarterCycleComponent },
    { path: 'view-performance/:id', component: ViewPerformanceComponent },
    { path: 'performance-management-system', component: PerformanceManagementSystemComponent },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserPerformanceRoutingModule { }
