import { Component, EventEmitter, HostListener, OnInit, Output } from '@angular/core';
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
export class SidenavComponent implements OnInit {

  @Output() onToggleSideNav: EventEmitter<SideNavToggle> = new EventEmitter();
  collapsed=false;
  screenWidth = 0;
  navData=navbarData;
  currentUser:User;
  menuItems:any;

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
  }

   ngOnInit(): void {
    this.screenWidth = window.innerWidth;
    this.menuItems = this.currentUser.userMapping;
    
    let newNavData =  this.navData.filter((data,index) => {
      if((JSON.parse(this.menuItems[index].moduleVisibilty) != null) ? JSON.parse(this.menuItems[index].moduleVisibilty) : false) return data;
    });
    this.navData = newNavData;
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
