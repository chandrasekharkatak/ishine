import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { User } from './models/user';
import { AuthenticationService } from './services/authentication.service';
import { filter, first } from 'rxjs/operators';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { NotificationService } from './services/notification.service';
import { NotificationMessage } from './models/notification';
import { EncryptionService } from './services/EncryptionService';
// import ClientMonitor from 'skywalking-client-js';
import { environment } from 'src/environments/environment';


interface SideNavToggle{
  screenWidth: number;
  collapsed: boolean;
}
@Component({
  standalone: false,
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
  
  // LinkedIn Page Notification properties
  linkedinPageNotification: any = null;
  linkedinPageModalRef: NgbModalRef;
  linkedinPageUrl: string = '';
  hasVisitedLinkedInLink: boolean = false;

  @ViewChild('linkedin_page_notification_template') linkedinPageNotificationTemplate: TemplateRef<any>;

  static DATE_FORMAT = 'DD-MM-YYYY';
  static DATETIME_FORMAT = 'DD-MM-YYYY HH:mm:ss';
  static LOCAL_DATE_FORMAT = 'YYYY-DD-MM';
  static LOCAL_DATETIME_FORMAT = 'YYYY-DD-MM HH:mm:ss';
  static DB_DATE_FORMAT = 'YYYY-MM-DD';
  static DB_DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';


  constructor(
    private authenticationService: AuthenticationService,
    private router : Router,
    private modalService: NgbModal,
    private notificationService: NotificationService,
    private encryptionService: EncryptionService
  ){

    this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      // Check for LinkedIn notification whenever user changes
      if (x && x.empId) {
        this.checkLinkedInPageNotification();
      }
    });
    // this.router.events.pipe(
    //   filter(event => event instanceof NavigationEnd)
    // ).subscribe(() => {
    //   if (ClientMonitor?.setPerformance) {
    //     ClientMonitor.setPerformance({
    //       collector: "https://ishine.apmosys.com",
    //       service: 'Ishine::local',
    //       serviceVersion: '1.0.0',
    //       pagePath: location.href,
    //       useWebVitals: true
    //     });
    //   }
    // });
  }

   
  ngOnInit():void{
    // Check for LinkedIn notification on app initialization
    if (this.currentUser && this.currentUser.empId) {
      this.checkLinkedInPageNotification();
    }
    
    // import('skywalking-client-js').then(ClientMonitor => {
    //   console.log('skywalking Client JS loaded:', ClientMonitor);
    //   if (ClientMonitor.default && typeof ClientMonitor.default.register === 'function') {
    //     ClientMonitor.default.register({
    //       service: 'Ishine::local',
    //       pagePath: location.href,
    //       serviceVersion: '1.0.0',
    //       useWebVitals:true,
    //       autoTracePerf:true,
    //       enableSPA:true,
    //       collector: "https://ishine.apmosys.com"
    //     });
    //     console.log('skywalking initialized successfully');
    //   } else {
    //     console.error('skywalking Client JS register function not found.');
    //   }
    // }).catch(err => {
    //   console.error('Error loading skywalking Client JS:', err);    });

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
  
  // LinkedIn Page Notification Methods
  checkLinkedInPageNotification() {
    if (this.currentUser && this.currentUser.linkedinPageNotification != null && this.currentUser.linkedinPageNotification != undefined) {
      // Only open if modal is not already open
      if (!this.linkedinPageModalRef || (this.linkedinPageModalRef && !this.linkedinPageModalRef.componentInstance)) {
        this.linkedinPageNotification = this.currentUser.linkedinPageNotification;
        this.linkedinPageUrl = this.linkedinPageNotification.notificationMessage || '';
        this.hasVisitedLinkedInLink = false;
        this.openLinkedInPageModal();
      }
    }
  }

  openLinkedInPageModal() {
    if (this.linkedinPageNotificationTemplate) {
      const modalConfig = {
        backdrop: 'static' as const, // Prevents closing on backdrop click
        ignoreBackdropClick: true,
        keyboard: false, // Prevents closing on ESC key
        modalDialogClass: 'modal-lg',
        windowClass: 'linkedin-modal'
      };
      this.linkedinPageModalRef = this.modalService.open(this.linkedinPageNotificationTemplate, modalConfig);
    }
  }

  onLinkedInLinkClick() {
    if (this.linkedinPageUrl) {
      window.open(this.linkedinPageUrl, '_blank');
      this.hasVisitedLinkedInLink = true;
    }
  }

  submitLinkedInPageConsent() {
    if (!this.hasVisitedLinkedInLink) {
      return;
    }

    let notificationObj = new NotificationMessage();
    notificationObj.empId = this.currentUser.empId;
    notificationObj.notificationId = this.linkedinPageNotification.notificationId;
    notificationObj.notificationType = this.linkedinPageNotification.notificationType;

    this.notificationService.submitNotificationConsent(notificationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let dtoResponse = response.serviceResponse;
        this.currentUser.linkedinPageNotification = dtoResponse.linkedinPageNotification;
        
        // Update session storage
        const encrypted = this.encryptionService.encrypt(JSON.stringify(this.currentUser));
        sessionStorage.setItem('currentUser', encrypted);
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        
        if (this.linkedinPageModalRef) {
          this.linkedinPageModalRef.close();
        }
        this.linkedinPageNotification = null;
        this.hasVisitedLinkedInLink = false;
        
        // Check if there are more LinkedIn notifications
        if (this.currentUser.linkedinPageNotification != null && this.currentUser.linkedinPageNotification != undefined) {
          setTimeout(() => {
            this.checkLinkedInPageNotification();
          }, 500);
        }
      }
    });
  }
}
