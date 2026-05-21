import { Component, EventEmitter, HostListener, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { navbarData,navbarItemData  } from './nav-data';
import { enableAppreciation } from '../models/enableAppreciation';
import * as moment from 'moment';
import { BreadcrumbService } from '../services/breadcrumb.service';

interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}
export interface NavGroup {
  groupLabel: string;
  items: any[];
  isExpanded: boolean;
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

   groupedMenuItems: NavGroup[] = [];

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
       this.buildGroupedMenu();
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

  //  private buildGroupedMenu(): void {
  //   const groupMap: { [label: string]: any[] } = {};

  //   this.menuItems.forEach(item => {
  //     const groupLabel = TAB_GROUP_MAP[item.tabName] || 'OTHER';
  //     if (!groupMap[groupLabel]) {
  //       groupMap[groupLabel] = [];
  //     }
  //     groupMap[groupLabel].push(item);
  //   });

  //   // Build in GROUP_ORDER, then append any unlisted groups
  //   const ordered: NavGroup[] = [];
  //   GROUP_ORDER.forEach(label => {
  //     if (groupMap[label]) {
  //       ordered.push({ groupLabel: label, items: groupMap[label], isExpanded: true });
  //     }
  //   });
  //   // Any group not in GROUP_ORDER goes at the end
  //   Object.keys(groupMap).forEach(label => {
  //     if (!GROUP_ORDER.includes(label)) {
  //       ordered.push({ groupLabel: label, items: groupMap[label], isExpanded: true });
  //     }
  //   });

  //   this.groupedMenuItems = ordered;
  // }

  private buildGroupedMenu(): void {
  const groupMap: { [label: string]: { items: any[], sequence: number } } = {};

   console.log('menuItems from API:', this.menuItems);


  this.menuItems.forEach(item => {
    // tabGroup and groupSequence now come directly from API response
    const groupLabel    = item.tabGroup      || 'OTHER';
    const groupSequence = item.groupSequence ?? 999;

    if (!groupMap[groupLabel]) {
      groupMap[groupLabel] = { items: [], sequence: groupSequence };
    }
    groupMap[groupLabel].items.push(item);
  });

  // Sort by groupSequence from DB — no hardcoded ORDER array needed
  this.groupedMenuItems = Object.entries(groupMap)
    .sort(([, a], [, b]) => a.sequence - b.sequence)
    .map(([groupLabel, { items }]) => ({
      groupLabel,
      items,
      isExpanded: true
    }));
}


    toggleGroup(group: NavGroup): void {
    group.isExpanded = !group.isExpanded;
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
