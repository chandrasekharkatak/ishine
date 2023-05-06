import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { AuthenticationService } from '../services/authentication.service';
import { User } from '../models/user';
import { ActivatedRoute, Router } from '@angular/router';
import { Log } from '../models/log';
import { LogService } from '../services/log.service';
import { Feature } from '../models/feature';

@Component({
  selector: 'app-user-exit',
  templateUrl: './user-exit.component.html',
  styleUrls: ['./user-exit.component.css']
})
export class UserExitComponent implements OnInit,OnDestroy,AfterViewInit {

  tabName:any = 'Exit';
  currentUser:User;
  userMapping:any = {};
  log:Log;
  
  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
    private logService:LogService
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
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
        const tab = document.getElementById('exitTab').querySelector('.nav-link');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
      }
    });
  }

  removeActiveTab(){
    const tab = document.getElementById('exitTab').querySelector('.nav-link.active');
    console.log("active tab :", tab);
    tab?.classList.remove('active');
  }
  
}
