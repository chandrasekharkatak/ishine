import { Component, HostListener, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
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

  static DATE_FORMAT = 'DD-MM-YYYY';
  static DATETIME_FORMAT = 'DD-MM-YYYY HH:mm:ss';

  constructor(
    private authenticationService: AuthenticationService,
    private router : Router
  ){

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  onToggleSideNav(data: SideNavToggle){
    this.screenWidth = data.screenWidth;
    this.isSideNavCollapsed = data.collapsed;
  }

  // @HostListener('window:beforeunload',[ '$event' ])
  // browserClosed(event:any){
  //   event.preventDefault();

  //   this.logout();
  // }


  @HostListener('window:unload', [ '$event' ])
  logout(event) {
    event.preventDefault();

    let user = new User();
    user.empId = this.currentUser.empId;
    this.authenticationService.logoutUser(user).subscribe((response:any)=>{
      this.authenticationService.stopUserSessionCheck();
      console.log(response.serviceResponse);
      sessionStorage.removeItem('currentUser');
      // delete method call for cookies
      this.authenticationService.deleteCookies();
      this.authenticationService.setcurrentUserSubject(null);
    })
  }

  //implementing cookies
  
  
}
