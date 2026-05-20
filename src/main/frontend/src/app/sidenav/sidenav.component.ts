import { Component, EventEmitter, HostListener, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { navbarData } from './nav-data';
import { enableAppreciation } from '../models/enableAppreciation';
import * as moment from 'moment';
import { BreadcrumbService } from '../services/breadcrumb.service';

interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}
@Component({
  standalone: false,
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
  firstTimeLogin="false";
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

  constructor(private authenticationService: AuthenticationService,
    private breadcrumbService: BreadcrumbService,){
  }

   ngOnInit(): void {
    this.screenWidth = window.innerWidth;
    this.firstTimeLogin = sessionStorage.getItem('FirstTimeLogin');

    this.activatedSubscriptions = this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      if (!x?.tabList?.length) {
        this.menuItems = [];
        this.appreciationEventInfo = x?.appreciationEventInfo;
        return;
      }
      this.menuItems = [...x.tabList];
      this.appreciationEventInfo = x.appreciationEventInfo;
      this.filterAppreciationTab();
    });
  }

  /** Hide Appreciation tab when disabled (same rules as before; runs whenever user / tabList updates). */
  private filterAppreciationTab(): void {
    if (!this.menuItems?.length || !this.currentUser) {
      return;
    }
    if (!this.appreciationEventInfo?.fromDate || !this.appreciationEventInfo?.toDate) {
      return;
    }
    const dateFormat = 'YYYY-MM-DD';
    const currentDate = moment(new Date()).format(dateFormat);
    const fromDate = this.appreciationEventInfo?.fromDate;
    const toDate = this.appreciationEventInfo?.toDate;
    const dateCheck = this.dateCheck(currentDate, fromDate, toDate);

    this.menuItems = this.menuItems.filter((item) => {
      if (item.tabName !== 'Appreciation') {
        return true;
      }
      if (this.currentUser.isAppreciationEnable !== true && dateCheck === false) {
        return false;
      }
      if (this.currentUser.isAppreciationEnable === false && dateCheck === true) {
        return false;
      }
      if (this.currentUser.isAppreciationEnable === true && dateCheck === false) {
        return false;
      }
      return true;
    });
  }

  ngOnDestroy(): void {
    this.activatedSubscriptions?.unsubscribe();
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

    var fDate,tDate,cDate;
    fDate = Date.parse(fromDate);
    tDate = Date.parse(toDate);
    cDate = Date.parse(currentDate);

    if((cDate <= tDate && cDate >= fDate)) {
      //console.log("true date");
        return true;
    }
    else{
    //console.log("false date");
    return false;
    }
  }

  // onTabClick(){
  //   this.breadcrumbService.setBreadcrumbSubject(null)
  // }
  onTabClick(event: Event): void {
    if (this.firstTimeLogin === 'true') {
      event.preventDefault();  // Prevent the click from triggering navigation
      event.stopPropagation();  // Prevent further propagation of the event
      console.log('Routing is disabled because firstTimeLogin is false');
    }else{
      this.breadcrumbService.setBreadcrumbSubject(null)
  
    }
  }
}
