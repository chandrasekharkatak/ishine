import { Component, HostListener } from '@angular/core';
import { User } from './models/user';
import { AuthenticationService } from './services/authentication.service';


interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'employee-portal-revamp';

  isSideNavCollapsed = false;
  screenWidth = 0;
  currentUser:User = new User();

  constructor(private authenticationService: AuthenticationService){

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  onToggleSideNav(data: SideNavToggle){
    this.screenWidth = data.screenWidth;
    this.isSideNavCollapsed = data.collapsed;
  }

  // @HostListener('window:beforeunload',[ '$event' ])
  // logout() {
  //   let user = new User();
  //   user.empId = this.currentUser.empId;
  //   this.authenticationService.logoutUser(user).subscribe((response:any)=>{
      
  //   })
  // }

  //implementing cookies
  
  
}
