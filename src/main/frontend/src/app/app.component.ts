import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { User } from './models/user';
import { AuthenticationService } from './services/authentication.service';
import { first } from 'rxjs/operators';
// import ClientMonitor from 'skywalking-client-js';


interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}
@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
 export class AppComponent implements OnInit{
  //export class AppComponent {
  title = 'employee-portal-revamp';

  isSideNavCollapsed = false;
  screenWidth = 0;
  currentUser:User = new User();

  static DATE_FORMAT = 'DD-MM-YYYY';
  static DATETIME_FORMAT = 'DD-MM-YYYY HH:mm:ss';
  static LOCAL_DATE_FORMAT = 'YYYY-DD-MM';
  static LOCAL_DATETIME_FORMAT = 'YYYY-DD-MM HH:mm:ss';
  static DB_DATE_FORMAT = 'YYYY-MM-DD';
  static DB_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

  constructor(
    private authenticationService: AuthenticationService,
    private router : Router
  ){

    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit():void{
    import('skywalking-client-js').then(ClientMonitor => {
      console.log('skywalking Client JS loaded:', ClientMonitor);
      if (ClientMonitor.default && typeof ClientMonitor.default.register === 'function') {
        ClientMonitor.default.register({
          service: 'Ishine::ui',
          pagePath: window.location.pathname,
          serviceVersion: '1.0.0',
          useWebVitals:true,
          enableSPA:true,
          collector: "http://192.168.21.175:8081/employeeportal"
        });
        console.log('skywalking initialized successfully');
      } else {
        console.error('skywalking Client JS register function not found.');
      }
    }).catch(err => {
      console.error('Error loading skywalking Client JS:', err);    });

  }
  onToggleSideNav(data: SideNavToggle){
    this.screenWidth = data.screenWidth;
    this.isSideNavCollapsed = data.collapsed;
  }


// added by anurag for auto-logout when user close browser directly before logout their session. It's work like transaction if someone refresh or reload their session will logout

//   @HostListener('window:unload', ['$event'])
// beforeunloadHandler(event: Event) {
//   // event.preventDefault();
//   let user = new User();
//   user.empId = this.currentUser.empId;
//   this.authenticationService.logoutUser(user).pipe(first()).subscribe((response: any) => {
//     //console.log('Session expired');
//   });
// }


  // @HostListener('window:beforeunload',[ '$event' ])
  // browserClosed(event:any){
  //   event.preventDefault();

  //   this.logout();
  // }

  //! unable to Identify Window close defer to Window Reload

  // @HostListener('window:unload', [ '$event' ])
  // logout(event) {
  //   event.preventDefault();

  //   let user = new User();
  //   user.empId = this.currentUser.empId;
  //   this.authenticationService.logoutUser(user).subscribe((response:any)=>{
  //     this.authenticationService.stopUserSessionCheck();
  //     //console.log(response.serviceResponse);
  //     sessionStorage.removeItem('currentUser');
  //     sessionStorage.removeItem('token');
  //     // delete method call for cookies
  //     this.authenticationService.deleteCookies();
  //     this.authenticationService.setcurrentUserSubject(null);
  //   })
  // }

  //implementing cookies
  
  
}
