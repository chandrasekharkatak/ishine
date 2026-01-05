import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CompOffComponent } from 'src/app/user-leaves/comp-off/comp-off.component';
import { HolidaysComponent } from 'src/app/user-leaves/holidays/holidays.component';
import { LeaveComponent } from 'src/app/user-leaves/leave/leave.component';
import { UserLeavesComponent } from 'src/app/user-leaves/user-leaves.component';

const routes: Routes = [{
  path: '', component: UserLeavesComponent,
  children: [
    { path: 'leave', component: LeaveComponent, },
    { path: 'holiday', component: HolidaysComponent, },
    { path: 'compOff', component: CompOffComponent, },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserLeavesRoutingModule { }
