import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';

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

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
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

    this.projectId = this.router.url.split("/")[3];
  }

  ngAfterViewInit(): void {
    if(this.projectId != undefined || this.projectId != null){
      this.setActiveTab('resource-management');
    }else{
      this.setActiveTab();
    }
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab(rmgUrl?:any){
    let activeRouteLink:any;
    if(rmgUrl != null){
      const tabs = document.getElementById('teamTab').querySelectorAll('.nav-link');
      tabs.forEach(tab => {
        let routeLink =  tab.getAttribute('routerLink');

        if(rmgUrl == routeLink){
          tab.classList.add('active');
        }
      });
      activeRouteLink = rmgUrl;

      this.router.navigate(['./'+activeRouteLink, this.projectId], {relativeTo: this.route});
    }else{
      const tab = document.getElementById('teamTab').querySelector('.nav-link');
      console.log(tab);

      tab.classList.add('active');

      activeRouteLink = tab.getAttribute('routerLink');
      this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
    }
  }

  removeActiveTab(){
    const tab = document.getElementById('teamTab').querySelector('.nav-link.active');
    console.log("active tab :", tab);
    tab?.classList.remove('active');
  }

}


