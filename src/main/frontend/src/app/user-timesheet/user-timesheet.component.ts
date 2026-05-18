import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { Log } from '../models/log';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { LogService } from '../services/log.service';
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
  log: Log;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
    private logService: LogService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.logService.log.subscribe(x => {
      this.log = x;
      if (this.log) {
        this.log.tabName = this.tabName;
        this.log.featureName = this.tabName;
      }
    });
   }

  ngOnInit(): void {
    if (!this.log) {
      this.log = new Log();
    }
    if (this.currentUser?.empId != null) {
      this.log.empId = this.currentUser.empId;
    }
    this.logService.updateLogInfo(this.log);
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
    const params =this.route.snapshot.queryParams;

    if (params.tab === 'team-timesheet' && this.userMapping.team_timesheets) {
    const tab = document.getElementById('team-timesheet-tab');
    if (tab) {
      tab.classList.add('active');
      const activeRouteLink = tab.getAttribute('routerLink');
      this.router.navigate(['./' + activeRouteLink], {
        relativeTo: this.route,
        queryParamsHandling: 'merge'
      });
      return;
    }
  }

    if(this.userMapping.timesheets_dashboard ){
      if(params.date == undefined){
        const tab = document.getElementById('hr-dashboard-tab');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
      }
      else{
        const tab = document.getElementById('timesheetTab').querySelector('.nav-link');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
      }
    }
    else{

        const tab = document.getElementById('timesheetTab').querySelector('.nav-link');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});

  }
  }
  removeActiveTab(){
    const tab = document.getElementById('timesheetTab').querySelector('.nav-link.active');
    //console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}
