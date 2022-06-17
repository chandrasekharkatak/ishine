import { Component, OnInit, ViewChild } from '@angular/core';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { CompOffComponent } from './comp-off/comp-off.component';
import { HolidaysComponent } from './holidays/holidays.component';
import { LeaveComponent } from './leave/leave.component';

@Component({
  selector: 'app-user-leaves',
  templateUrl: './user-leaves.component.html',
  styleUrls: ['./user-leaves.component.css']
})
export class UserLeavesComponent implements OnInit {

  @ViewChild('leave')
  leave!: LeaveComponent;
  @ViewChild('holidays')
  holidays!: HolidaysComponent;
  @ViewChild('compOff')
  compOff!: CompOffComponent;

  tabName:any = 'My Leave';
  currentUser:User;
  userMapping:any = {};


  constructor(private authenticationService: AuthenticationService) { 
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
  }

  ngAfterViewInit(): void {
    // setTimeout(this.setActiveTab, 100);
  }

  setActiveTab(){
    const tab = document.getElementById('leaveTab').querySelector('.nav-link');
    const tabPane = document.getElementById('leaveTabContent').querySelector('.leave__tab-container');
    
    console.log(tab);
    console.log(tabPane);

    tab.classList.add('active');
    tabPane.classList.add('active');
    tabPane.classList.add('show');
  }

  resetLeave(){
    this.leave.sectionViewInit();
  }
  resetHolidays(){
    this.holidays;
  }
  resetCompOff(){
    this.compOff;
  }

}
