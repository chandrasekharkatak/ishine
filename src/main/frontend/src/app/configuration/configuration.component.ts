import { Component, OnInit, ViewChild } from '@angular/core';
import { DeptConfigComponent } from './dept-config/dept-config.component';
import { EmployeeConfigComponent } from './employee-config/employee-config.component';
import { RoleConfigComponent } from './role-config/role-config.component';
import { UserTypeConfigComponent } from './user-type-config/user-type-config.component';

@Component({
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.css']
})
export class ConfigurationComponent implements OnInit {

  @ViewChild('empCfg')
  employeeConfig!: EmployeeConfigComponent;
  @ViewChild('deptCfg')
  departmentConfig!: DeptConfigComponent;
  @ViewChild('roleCfg')
  roleConfig!: RoleConfigComponent;
  // @ViewChild('userTypeCfg')
  // userTypeConfig!: UserTypeConfigComponent;


  constructor() { }

  ngOnInit(): void {
  }

  resetEmpConfig(){
    this.employeeConfig.showCreateForm();
  }
  resetDeptConfig(){
    this.departmentConfig.showCreateForm();
  }
  resetRoleConfig(){
    this.roleConfig.showCreateForm();
  }
  // resetUsertypeConfig(){
  //   this.userTypeConfig.showCreateForm();
  // }

}
