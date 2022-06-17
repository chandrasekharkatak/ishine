import { AfterViewInit, Component, Input, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { DeptConfigComponent } from './dept-config/dept-config.component';
import { EmployeeConfigComponent } from './employee-config/employee-config.component';
import { LeaveConfigComponent } from './leave-config/leave-config.component';
import { RoleConfigComponent } from './role-config/role-config.component';

@Component({
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.css']
})
export class ConfigurationComponent implements OnInit, AfterViewInit{

  employeeConfig: EmployeeConfigComponent;
  departmentConfig: DeptConfigComponent;
  roleConfig: RoleConfigComponent;
  leaveConfig: LeaveConfigComponent;

  tabName:any = 'Configurations';
  currentUser:User;
  userMapping:any = {};


  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    console.log("this.currentUser : ", this.currentUser);
    console.log("Mapped Features : ", this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName));
    

    // Dynamic feature Flags 
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if(sub.isActive === false)return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });
    console.log(this.tabName, this.userMapping);
  }

  ngAfterViewInit(): void {
    // setTimeout(this.setActiveTab, 100);
  }

  setActiveTab(){
    const tab = document.getElementById('configTab').querySelector('.nav-link');
    const tabPane = document.getElementById('userTabContent').querySelector('.tab-pane');
    
    console.log(tab);
    console.log(tabPane);

    tab.classList.add('active');
    tabPane.classList.add('active');
    tabPane.classList.add('show');

    let activeRouteLink = tab.getAttribute('routerLink');
    console.log("activeRouteLink :", activeRouteLink);
    this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  }

  resetEmpConfig(){
    this.employeeConfig.ngOnInit();
  }
  resetDeptConfig(){
    this.departmentConfig.ngOnInit();
  }
  resetRoleConfig(){
    this.roleConfig.ngOnInit();
  }
  resetLeaveConfig(){
    this.leaveConfig.ngOnInit();
  }
}
