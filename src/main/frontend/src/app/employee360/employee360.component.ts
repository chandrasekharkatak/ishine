

import { AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeConfigComponent } from '../configuration/employee-config/employee-config.component';
import { DeptConfigComponent } from '../configuration/dept-config/dept-config.component';
import { LeaveConfigComponent } from '../configuration/leave-config/leave-config.component';
import { RoleConfigComponent } from '../configuration/role-config/role-config.component';
import { Breadcrumb } from '../models/breadcrumd';
import { BreadcrumbService } from '../services/breadcrumb.service';


@Component({
  selector: 'app-employee360',
  templateUrl: './employee360.component.html',
  styleUrls: ['./employee360.component.css']
})
export class Employee360Component implements OnInit {

  employeeData: any;
  emplId:any;
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
    private breadcrumbService: BreadcrumbService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }



  ngOnInit(): void {

    const storedData = localStorage.getItem('employee360Data');
    const parsedData = storedData ? JSON.parse(storedData) : null;
  
    if(parsedData != null || parsedData != undefined ){
      this.employeeData =  parsedData;
    }else{
      this.employeeData = history.state.data;
    }
    
   

    console.log("employeeData   ",this.employeeData);

  

    // Dynamic feature Flags 
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    console.log("feature Name ",featureMap);
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if(sub.isActive === false)return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });
  
  }





  ngAfterViewInit(): void {
    this.setActiveTab();
    // setTimeout(this.setActiveTab,2000)
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab(){

    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link');
    //console.log(tab);

    tab.classList.add('active');
    let activeRouteLink = tab.getAttribute('routerLink');
    //console.log("activeRouteLink :", activeRouteLink);
    //console.log("Router :",  this.router);
    
    this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  }

  removeActiveTab(){

    let breadcrumbObject = new Breadcrumb();
      
      breadcrumbObject.title = "Rewards";
      breadcrumbObject.url = "/employee-360/rewards";
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    //console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}
