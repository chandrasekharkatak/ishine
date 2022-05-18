import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SidenavComponent } from './sidenav/sidenav.component';
import { BodyComponent } from './body/body.component';
import { HomeComponent } from './home/home.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { UserLeavesComponent } from './user-leaves/user-leaves.component';
import { UserAttendanceComponent } from './user-attendance/user-attendance.component';
import { UserSalaryComponent } from './user-salary/user-salary.component';
import { UserRequestsComponent } from './user-requests/user-requests.component';
import { UserPerformanceComponent } from './user-performance/user-performance.component';
import { HrPoliciesComponent } from './hr-policies/hr-policies.component';
import { HelpdeskComponent } from './helpdesk/helpdesk.component';
import { AccordionModule } from './accordion/accordion.module';
import { ConfigurationComponent } from './configuration/configuration.component';
import { RoleConfigComponent } from './configuration/role-config/role-config.component';
import { DeptConfigComponent } from './configuration/dept-config/dept-config.component';
import { EmployeeConfigComponent } from './configuration/employee-config/employee-config.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { DatePipe, HashLocationStrategy, LocationStrategy } from '@angular/common';
import { BsModalService, ModalModule } from 'ngx-bootstrap/modal';

@NgModule({
  declarations: [
    AppComponent,
    SidenavComponent,
    BodyComponent,
    HomeComponent,
    UserProfileComponent,
    UserLeavesComponent,
    UserAttendanceComponent,
    UserSalaryComponent,
    UserRequestsComponent,
    UserPerformanceComponent,
    HrPoliciesComponent,
    HelpdeskComponent,
    ConfigurationComponent,
    RoleConfigComponent,
    DeptConfigComponent,
    EmployeeConfigComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    AccordionModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ModalModule.forRoot(),
  ],
  providers: [
    BsModalService,
    DatePipe,
    { provide: LocationStrategy, useClass: HashLocationStrategy },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
