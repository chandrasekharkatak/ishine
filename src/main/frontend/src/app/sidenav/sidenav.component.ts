import { Component, EventEmitter, HostListener, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { navbarData } from './nav-data';
import { enableAppreciation } from '../models/enableAppreciation';
import * as moment from 'moment';

interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}
@Component({
  selector: 'app-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.css']
})
export class SidenavComponent implements OnInit, OnDestroy {

  @Output() onToggleSideNav: EventEmitter<SideNavToggle> = new EventEmitter();
  collapsed=false;
  screenWidth = 0;
  navData=navbarData;
  currentUser:User;
  menuItems:any;
  menuItems1:any;
  appreciationEventInfo:enableAppreciation;

  private activatedSubscriptions:Subscription;
  

  @HostListener('window:resize', ['$event'])
  onResize(event:any){
    this.screenWidth = window.innerWidth;
    if(this.screenWidth <= 768){
      this.collapsed = false;
      this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
    }
  }

  constructor(private authenticationService: AuthenticationService){
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    this.menuItems = this.currentUser.tabList;   
  }

   ngOnInit(): void {
    this.screenWidth = window.innerWidth;
    console.log("menuItems : ", this.menuItems);
    this.appreciationEventInfo = this.currentUser.appreciationEventInfo;
    
    const dateFormat = 'YYYY-MM-DD';

    var currentDate = moment(new Date()).format(dateFormat);
    let fromDate = this.appreciationEventInfo.fromDate;
    let toDate = this.appreciationEventInfo.toDate;
    var dateCheck=this.dateCheck(currentDate,fromDate,toDate);

    this.menuItems.forEach((item,index) => {
      if(item.tabName == 'Appreciation' && this.currentUser.isAppreciationEnable != true && dateCheck==false){
        this.menuItems.splice(index,1);
      }
    });
  }

  ngOnDestroy(): void {
    this.activatedSubscriptions.unsubscribe();
  }
  

  toggleCollapse(){
    this.collapsed = !this.collapsed;
    this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
  }

  closeSidenav(){
    this.collapsed = false;
    this.onToggleSideNav.emit({collapsed: this.collapsed, screenWidth: this.screenWidth});
  }
  dateCheck(currentDate,fromDate,toDate) {

    var fDate,lDate,cDate;
    fDate = Date.parse(fromDate);
    lDate = Date.parse(toDate);
    cDate = Date.parse(currentDate);

    if((cDate <= lDate && cDate >= fDate)) {
      console.log("true date");
        return true;
    }
    else{
    console.log("false date");
    return false;
    }
  }
  
}
