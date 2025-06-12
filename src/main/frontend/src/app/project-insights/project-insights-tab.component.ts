import { Component, OnInit } from '@angular/core';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { Feature } from '../models/feature';
import { ActivatedRoute, Router } from '@angular/router';
import { Log } from '../models/log';
import { LogService } from '../services/log.service';

@Component({
  selector: 'app-project-tab-insights',
  templateUrl: './project-insights-tab.component.html',
  styleUrls: ['./project-insights-tab.component.css']
})
export class ProjectInsightsTabComponent implements OnInit {

  tabName:any = 'Project Insights';
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
    // Dynamic feature Flags 
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if(sub.isActive === false)return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });
  }

  ngAfterViewInit(): void {
    this.setActiveTab(); 
  }


  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab(){  
    const tab = document.getElementById('configTab').querySelector('.nav-link');
    //console.log(tab);

    tab.classList.add('active');
    let activeRouteLink = tab.getAttribute('routerLink');
    //console.log("activeRouteLink :", activeRouteLink);
    //console.log("Router :",  this.router);
    
    this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  }

  removeActiveTab(){
    const tab = document.getElementById('configTab').querySelector('.nav-link.active');
    //console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}
