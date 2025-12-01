import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import * as XLSX from 'xlsx';

@Component({
  standalone: false,
  selector: 'app-user-timesheet',
  templateUrl: './user-timesheet.component.html',
  styleUrls: ['./user-timesheet.component.css']
})
export class UserTimesheetComponent implements OnInit,OnDestroy,AfterViewInit {

  tabName:any = 'Timesheets';
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
    //console.log("this.currentUser : ", this.currentUser);
    //console.log("Mapped Features : ", this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName));
    

    // Dynamic feature Flags 
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if(sub.isActive === false)return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });
    //console.log(this.tabName, this.userMapping);
  }

  ngAfterViewInit(): void {
    this.setActiveTab(); 
  }


  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab(){
    this.route.queryParams.subscribe((params) => {
      let tabName = params.tabName;
      if(tabName){
        const tab = document.getElementById(tabName);
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], 
        { relativeTo: this.route,
          queryParams: params, 
          queryParamsHandling: 'merge'
        });
      } 
      else{
        const tab = document.getElementById('timesheetTab').querySelector('.nav-link');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
      }
    });
  }

  removeActiveTab(){
    const tab = document.getElementById('timesheetTab').querySelector('.nav-link.active');
    //console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}
