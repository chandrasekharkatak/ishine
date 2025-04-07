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
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    this.menuItems = this.currentUser.tabList;   
  }

   ngOnInit(): void {
    this.screenWidth = window.innerWidth;
    //console.log("menuItems : ", this.menuItems);
    this.appreciationEventInfo = this.currentUser.appreciationEventInfo;
    this.firstTimeLogin=sessionStorage.getItem('FirstTimeLogin');
   
    const dateFormat = 'YYYY-MM-DD';

    var currentDate = moment(new Date()).format(dateFormat);
    let fromDate = this.appreciationEventInfo.fromDate;
    let toDate = this.appreciationEventInfo.toDate;
    var dateCheck=this.dateCheck(currentDate,fromDate,toDate);

    //console.log(this.currentUser.isAppreciationEnable, " : isAppreciationEnable");
    

    this.menuItems.forEach((item,index) => {
      if(item.tabName == 'Appreciation' && this.currentUser.isAppreciationEnable != true && dateCheck==false){
        this.menuItems.splice(index,1);
      }
      else if(item.tabName == 'Appreciation' && this.currentUser.isAppreciationEnable == false && dateCheck==true){
        this.menuItems.splice(index,1); 
      }
      else if(item.tabName == 'Appreciation' && this.currentUser.isAppreciationEnable == true && dateCheck==false){
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
