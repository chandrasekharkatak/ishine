import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { Feature } from '../models/feature';
import { AuthenticationService } from '../services/authentication.service';
import { ActivatedRoute, Router } from '@angular/router';
import { User } from '../models/user';
import { RewardsAndRecognisationComponent } from './rewards-and-recognisation/rewards-and-recognisation.component';

@Component({
  selector: 'app-rewards',
  templateUrl: './rewards.component.html',
  styleUrls: ['./rewards.component.css']
})
export class RewardsComponent implements OnInit, AfterViewInit, OnDestroy  {

  currentUser:User;
  userMapping:any = {};
  tabName:any = 'Rewards';
  
  // rewardsAndReecognisation:RewardsAndRecognisationComponent

  constructor(
    private authenticationService: AuthenticationService,
    private router: Router,
    private route: ActivatedRoute,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if(sub.isActive === false)return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });

    console.log("usermapping   ", this.userMapping);
  }
  ngAfterViewInit(): void {
    this.setActiveTab();
    // setTimeout(this.setActiveTab,2000)
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab(){
    const tab = document.getElementById('rewards-tab').querySelector('.nav-link');
    console.log(tab);

    tab.classList.add('active');
    let activeRouteLink = tab.getAttribute('routerLink');
    console.log("activeRouteLink :", activeRouteLink);
    console.log("Router :",  this.router);
    
    this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  }

  removeActiveTab(){
    const tab = document.getElementById('rewards-tab').querySelector('.nav-link.active');
    console.log("active tab :", tab);
    tab?.classList.remove('active');
  }





  // tabName:any = 'Reports';
  // currentUser:User;
  // userMapping:any = {};

  // constructor(
  //   private authenticationService: AuthenticationService,
  //   private router: Router,
  //   private route: ActivatedRoute,
  // ) { 
  //   this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  // }

  // ngOnInit(): void {
  //   //console.log("this.currentUser : ", this.currentUser);
  //   //console.log("Mapped Features : ", this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName));
    
  //   // Dynamic feature Flags 
  //   let featureMap:Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
  //   featureMap?.forEach(feat => {
  //     let inActiveSubfeatures = feat.subFeatures.filter(sub => {
  //       if(sub.isActive === false)return sub;
  //     });
  //     this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
  //   });
  //   //console.log(this.tabName, this.userMapping);
  // }

  // ngAfterViewInit(): void {
  //   this.setActiveTab();
  // }

  // ngOnDestroy(): void {
  //   this.removeActiveTab();
  // }

  // setActiveTab(){
  //   const tab = document.getElementById('reportTab').querySelector('.nav-link');
  //   //console.log(tab);

  //   tab.classList.add('active');
  //   let activeRouteLink = tab.getAttribute('routerLink');
  //   //console.log("activeRouteLink :", activeRouteLink);
  //   //console.log("Router :",  this.router);
    
  //   this.router.navigate(['./'+activeRouteLink], {relativeTo: this.route});
  // }

  // removeActiveTab(){
  //   const tab = document.getElementById('reportTab').querySelector('.nav-link.active');
  //   //console.log("active tab :", tab);
  //   tab?.classList.remove('active');
  // }
}
