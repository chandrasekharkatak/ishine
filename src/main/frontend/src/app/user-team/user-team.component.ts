import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params, NavigationEnd } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { BreadcrumbService } from '../services/breadcrumb.service';

@Component({
  selector: 'app-user-team',
  templateUrl: './user-team.component.html',
  styleUrls: ['./user-team.component.css']
})
export class UserTeamComponent implements OnInit {

  tabName:any = 'My Team';
  currentUser:User;
  userMapping:any = {};
  projectId:any;
  activeTab: string = 'my-team';
  action: any;

  currentBreadcrumbList: any[] = [];
  employee360Tab: boolean = false;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private breadcrumbService: BreadcrumbService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
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

    this.projectId = this.router.url.split("/")[3];

    this.route.queryParams.subscribe(params => {
      this.activeTab = params['tab'] || 'my-team';
      this.action = params['action'] || null;
      if (this.activeTab === 'my-team' && this.action === 'view-pending-request') {
          this.triggerPendingRequestView();
      }
  });

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null) && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.employee360Tab = true;
      this.setActiveTab('resource-management');
    } else {
      this.employee360Tab = false;
    }
  }

  ngAfterViewInit(): void {
    if(this.projectId != undefined || this.projectId != null){
      this.setActiveTab('resource-management');
    }else{
      this.setActiveTab();
    }
    this.listenToRouteChanges();
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  isActive(tab: string): boolean {
    return this.activeTab === tab;
  }

  //modified by priyadarshini
  setActiveTab(rmgUrl?:any){
    const currentChild = this.route.snapshot.firstChild;
  
    if (!currentChild) {
    const tabs = document.getElementById('teamTab').querySelectorAll('.nav-link');
    let activeRouteLink:any;
    
    if (!rmgUrl) {
      if (this.currentUser.employeeRole === 'RMG' || this.employee360Tab) {
        activeRouteLink = 'resource-management';
      } else {
        const tab = document.getElementById('teamTab').querySelector('.nav-link');
        activeRouteLink = tab ? tab.getAttribute('routerLink') : 'my-team';
      }
    }

    tabs.forEach(tab => {
      let routeLink = tab.getAttribute('routerLink');
      if (activeRouteLink === routeLink) {
        tab.classList.add('active');
      } else {
        tab.classList.remove('active');
      }
    });

    if (this.projectId) {
      this.router.navigate(['./' + activeRouteLink, this.projectId], { relativeTo: this.route });
    } else {
      this.router.navigate(['./' + activeRouteLink], { relativeTo: this.route });
    }
  }else {
    // A child route is already active → don't override
    const path = currentChild.routeConfig.path;
    const matchingTab = document.querySelector(`[routerLink="${path}"]`);
    matchingTab?.classList.add('active');
  }
  
  }

  // removeActiveTab(){
  //   const tab = document.getElementById('teamTab').querySelector('.nav-link.active');
  //   //console.log("active tab :", tab);
  //   tab?.classList.remove('active');
  // }

  removeActiveTab() {
    const activeTabs = document.querySelectorAll('#reportTab .nav-link.active');
    activeTabs.forEach(tab => tab.classList.remove('active'));
  }

  triggerPendingRequestView(): void {
    this.router.navigate(['/user-team'], { queryParams: { tab: 'my-team', action: 'view-pending-request' } });
}

listenToRouteChanges() {
  this.router.events.subscribe(event => {
    if (event instanceof NavigationEnd) {
      const currentChild = this.route.snapshot.firstChild;
      const path = currentChild?.routeConfig?.path;

      const allTabs = document.querySelectorAll('#teamTab .nav-link');
      allTabs.forEach(tab => tab.classList.remove('active'));

      const matchingTab = document.querySelector(`#teamTab [routerLink="${path}"]`);
      matchingTab?.classList.add('active');
    }
  });
}
}
