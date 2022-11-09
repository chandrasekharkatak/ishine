import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { Log } from '../models/log';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { LogService } from '../services/log.service';
import { CompOffComponent } from './comp-off/comp-off.component';
import { HolidaysComponent } from './holidays/holidays.component';
import { LeaveComponent } from './leave/leave.component';

@Component({
  selector: 'app-user-leaves',
  templateUrl: './user-leaves.component.html',
  styleUrls: ['./user-leaves.component.css']
})
export class UserLeavesComponent implements OnInit,OnDestroy,AfterViewInit{

  tabName:any = 'My Leave';
  currentUser:User;
  userMapping:any = {};
  log:Log;

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
    private logService:LogService
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.logService.log.subscribe(x => {
      this.log = x;
      this.log.tabName = this.tabName;
    });
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);
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
      }else{
        const tab = document.getElementById('leaveTab').querySelector('.nav-link');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
      }
    });
  }

  removeActiveTab(){
    const tab = document.getElementById('leaveTab').querySelector('.nav-link.active');
    console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}
