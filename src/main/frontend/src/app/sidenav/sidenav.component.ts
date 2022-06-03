import { Component, EventEmitter, HostListener, OnDestroy, OnInit, Output } from '@angular/core';
import { Subscription } from 'rxjs';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { navbarData } from './nav-data';
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

}
