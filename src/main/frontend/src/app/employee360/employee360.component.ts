

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

  
  private navigationSubscription: Subscription;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
    private employee360Service: Employee360Service,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
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

}
