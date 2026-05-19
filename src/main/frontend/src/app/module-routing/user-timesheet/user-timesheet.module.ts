import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserTimesheetRoutingModule } from './user-timesheet-routing.module';
import { NgxPaginationModule } from 'ngx-pagination';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    UserTimesheetRoutingModule,
    NgxPaginationModule
  ]
})
export class UserTimesheetModule { }
