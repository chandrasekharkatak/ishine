

import { AfterViewInit, Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeConfigComponent } from '../configuration/employee-config/employee-config.component';
import { DeptConfigComponent } from '../configuration/dept-config/dept-config.component';
import { LeaveConfigComponent } from '../configuration/leave-config/leave-config.component';
import { RoleConfigComponent } from '../configuration/role-config/role-config.component';
import { Subscription } from 'rxjs';
import { Employee360Service } from '../services/employee360.service';
import { BreadcrumbService } from '../services/breadcrumb.service';
import { Employee } from '../models/employee';


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
  breadcrumbUrl:any[] = [];
  
  private navigationSubscription: Subscription;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
    private employee360Service: Employee360Service,
    private breadcrumbService: BreadcrumbService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.breadcrumbUrl = x);
  }



  ngOnInit(): void {
    this.navigationSubscription = this.employee360Service.getNavigationEvent().subscribe(() => {
      this.removeActiveTab();
      this.setActiveTab();
    });

    const storedData = localStorage.getItem('employee360Data');
    const parsedData = storedData ? JSON.parse(storedData) : null;
  
    if(parsedData != null || parsedData != undefined ){
      this.employeeData =  parsedData;
    }else{
      this.employeeData = history.state.data;
    }
    
    let findBreadcrumbObject = this.breadcrumbUrl.findIndex(x => x.title === "Employee-360");
    if (findBreadcrumbObject >= 0) {
      this.breadcrumbUrl.splice(findBreadcrumbObject + 1);
      this.breadcrumbService.setBreadcrumbSubject(this.breadcrumbUrl);
    } else {
      let breadcrumbObject = { title: "Employee-360-Profile", url: "/employee-360/profile" };
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }

    console.log("employeeData   ",this.employeeData);

    this.getBioOverTimeandState();

  

    // Dynamic feature Flags 
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);

    console.log("checked logs   ",this.currentUser.userMapping)

    console.log("feature Name ",featureMap);featureMap
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
  if (this.navigationSubscription) {
    this.navigationSubscription.unsubscribe();
  }
  this.removeActiveTab();
  }

  setActiveTab(){

    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link');
    //console.log(tab);

    if (tab) {
      tab.classList.add('active');
      const activeRouteLink = tab.getAttribute('routerLink');
      this.router.navigate(['./' + activeRouteLink], { relativeTo: this.route });
    }
  }

  removeActiveTab(){
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    //console.log("active tab :", tab);
    if (tab) {
      tab.classList.remove('active');
    }
  }




  responseOvertime:any
  responsestate:any;

  getBioOverTimeandState(){

    let currentEmp = new Employee(); 
    currentEmp.empId = this.employeeData.empId;
    currentEmp.isDraft = false;

    this.employee360Service.getBioOverTimeandState(currentEmp).subscribe((response:any) =>
      
      {
       this.responseOvertime= response.serviceResponse[0];
       this.responsestate = response.serviceResponse[1];
      }
    
    );




     

  }

}
