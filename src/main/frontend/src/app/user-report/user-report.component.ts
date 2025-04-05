import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';

@Component({
  selector: 'app-user-report',
  templateUrl: './user-report.component.html',
  styleUrls: ['./user-report.component.css']
})
export class UserReportComponent implements OnInit, AfterViewInit, OnDestroy {

  tabName:any = 'Reports';
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
    this.listenToRouteChanges();
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  // setActiveTab(){
  //   const tab = document.getElementById('reportTab').querySelector('.nav-link');
  //   //console.log(tab);

  //   tab.classList.add('active');
  //   let activeRouteLink = tab.getAttribute('routerLink');
  //   //console.log("activeRouteLink :", activeRouteLink);
  //   //console.log("Router :",  this.router);
    
  //   this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  // }

  setActiveTab() {
    const currentChild = this.route.snapshot.firstChild;
  
    if (!currentChild) {
      // No child route is currently active → default to the first tab
      const tab = document.getElementById('reportTab')?.querySelector('.nav-link');
      tab?.classList.add('active');
      let activeRouteLink = tab?.getAttribute('routerLink');
  
      if (activeRouteLink) {
        this.router.navigate(['./' + activeRouteLink], { relativeTo: this.route });
      }
    } else {
      // A child route is already active → don't override
      const path = currentChild.routeConfig.path;
      const matchingTab = document.querySelector(`[routerLink="${path}"]`);
      matchingTab?.classList.add('active');
    }
  }
  

  // removeActiveTab(){
  //   const tab = document.getElementById('reportTab').querySelector('.nav-link.active');
  //   //console.log("active tab :", tab);
  //   tab?.classList.remove('active');
  // }

  removeActiveTab() {
    const activeTabs = document.querySelectorAll('#reportTab .nav-link.active');
    activeTabs.forEach(tab => tab.classList.remove('active'));
  }

  listenToRouteChanges() {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        const currentChild = this.route.snapshot.firstChild;
        const path = currentChild?.routeConfig?.path;
        
        
        const allTabs = document.querySelectorAll('#reportTab .nav-link');
        allTabs.forEach(tab => tab.classList.remove('active'));
  
        
        const matchingTab = document.querySelector(`[routerLink="${path}"]`);
        matchingTab?.classList.add('active');
      }
    });
  }

}
