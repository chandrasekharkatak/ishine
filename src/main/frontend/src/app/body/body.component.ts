import { Component, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import * as CryptoJS from 'crypto-js';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { ValidationService } from '../services/validation.service';

@Component({
  standalone: false,
  selector: 'app-body',
  templateUrl: './body.component.html',
  styleUrls: ['./body.component.css']
})
export class BodyComponent implements OnInit {

  @Input() collapsed = false;
  @Input() screenWidth = 0;
  currentUser:User = new User();
  currentUserName = "";

  @ViewChild("change_password")
  changePasswordTemplate: TemplateRef<any>;

  fieldTextType: boolean = false;
  fieldTextTypePassword: boolean = false;
  fieldTextTypeOldPass: boolean = false;
  isError:boolean=false;
  oldPasswordValid:boolean = false;

  password:any;
  userNewPass:any;
  newpassword:any;
  errorMsg:any;
  empId:any;
  user:User = new User();
  isHome:boolean = false;

  //modal
  alertMessage: any;
  modalRef:NgbModalRef;
  resourceManagementFeature: any;
  reportsFeature: any;
  // stop modal to close
  config = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard  : false
  };
  lmsauthentication: any;
    private lmsbaseurl: any = '';


  constructor(
    private validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private router: Router,
  ){
    this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;

      if(this.currentUser){
        console.log("n jsvsdv",this.currentUser);
        this.currentUserName = this.currentUser.name.split(" ")[0];
      this.currentUserName = this.currentUserName[0].toUpperCase() + this.currentUserName.slice(1).toLowerCase();
      this.extractFeatures();
      }
    });
    this.router.events.subscribe((e) => {
      if (e instanceof NavigationEnd) {
        if(e.url == "/home"){
          this.isHome = true;
        }else{
          this.isHome = false;
        }
      }
    });
  }

  ngOnInit(): void {
    this.extractFeatures();
    this.isHome = this.router.url === '/home';

    // Also handle navigation events
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.isHome = event.urlAfterRedirects === '/home';
      }
    });
  
  }

  extractFeatures() {
    // Loop through userMapping to find the required features
    this.currentUser.userMapping.forEach(feature => {
      if (feature.featureName === 'Resource Management') {
        this.resourceManagementFeature = feature.featureName;
       
      } else if (feature.featureName === 'Reports') {
        this.reportsFeature = feature.featureName;
       
      }
    });
  }
  getBreadcrumbClass(): string{
    let styleClass = '';

    if(!this.isHome){
      styleClass= 'mt-5';
    }
    return styleClass;
  }

  getBodyClass(): string{
    let styleClass = '';

    if(this.collapsed && this.screenWidth > 768){
      if(this.currentUser) styleClass= 'body-trimmed body--active';
      else styleClass= 'body-trimmed';
    }else if(this.collapsed && this.screenWidth <= 768 && this.screenWidth > 0){
      if(this.currentUser) styleClass= 'body-md-screen body--active';
      else styleClass= 'body-md-screen';
    }
    else{
      if(this.currentUser) styleClass= 'body--active';
    
    }
    return styleClass;
  }

  getNavClass(): string{
    let styleClass = '';
      if(this.collapsed && this.screenWidth > 768){
        if(this.currentUser) styleClass= 'navbar-trimmed'
      }else if(this.collapsed && this.screenWidth <= 768 && this.screenWidth > 0){
        if(this.currentUser) styleClass= 'navbar-md-screen'
      }else{
        if(this.currentUser) styleClass= 'navbar-md-screen';
      }
    return styleClass;
  }

  userLogout(){
    
    let user = new User();
    user.empId = this.currentUser.empId;
    this.authenticationService.logoutUser(user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.authenticationService.stopUserSessionCheck();
        //console.log(response.serviceResponse);
          sessionStorage.removeItem('currentUser');
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('logInfo');
          sessionStorage.removeItem('maxFileSize');
          sessionStorage.removeItem('maxRequestSize');
          sessionStorage.removeItem('sessioncheck');
          sessionStorage.removeItem('breadcrumb');
        // delete method call for cookies
        this.authenticationService.deleteCookies();
        this.authenticationService.setcurrentUserSubject(null);
        this.router.navigate(['/login']);
        setTimeout(() => {location.reload();});
      } else {
        if(response.serviceResponse == "Session already destroyed"){
          this.authenticationService.stopUserSessionCheck();
          sessionStorage.removeItem('currentUser');
          sessionStorage.removeItem('token');
          sessionStorage.removeItem('logInfo');
          sessionStorage.removeItem('maxFileSize');
          sessionStorage.removeItem('maxRequestSize');
          sessionStorage.removeItem('sessioncheck');
          sessionStorage.removeItem('breadcrumb');
          // delete method call for cookies
          this.authenticationService.deleteCookies();
          this.authenticationService.setcurrentUserSubject(null);
          this.router.navigate(['/login']);
          setTimeout(() => {location.reload();});
        }
        console.error(response.serviceResponse);
      }
    });

    
  }

  toggleFieldTextType() {
    this.fieldTextType = !this.fieldTextType;
  }

  toggleFieldChangePassword() {
    this.fieldTextTypePassword = !this.fieldTextTypePassword;
  }

  toggleFieldTextTypeOldPass() {
    this.fieldTextTypeOldPass = !this.fieldTextTypeOldPass;
  }

  setEncryption(keys,value){

    var key = CryptoJS.enc.Utf8.parse(keys);
    var iv = CryptoJS.enc.Utf8.parse(keys);

    var encrypted = CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(value.toString()), key,
    {
        keySize: 128 / 8,
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
    });

    return encrypted.toString();
  }

  reset() {

    this.password = '';
    this.userNewPass = '';
    this.newpassword = '';
    this.errorMsg = '';
  }


  checkEmployeeOldPassword() {

    this.isError = false;
    this.errorMsg = '';

    this.user.empId = this.currentUser.empId;
    this.user.password = this.setEncryption("PkdtRsJidheGitvS", this.password);

    this.employeeService.checkEmployeeOldPassword(this.user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.oldPasswordValid = true;
      } else {
        this.isError = true;
        this.errorMsg = response.serviceResponse;
        this.oldPasswordValid = false;
      }
    });
  }

  openChangePassword(changePasswordTemplate) {
    this.errorMsg = ''
    this.password = ''
    this.oldPasswordValid = false;
    this.newpassword = ''
    this.userNewPass = ''
    //console.log(this.currentUser.isNew)
    if(this.currentUser.isNew == 'true'){
      this.modalRef = this.modalService.open(changePasswordTemplate,this.config);
    }else{
      this.modalRef = this.modalService.open(changePasswordTemplate);
    }
  }

  openChangePasswordOnFirstTimeLoggin(){
    this.openChangePassword(this.changePasswordTemplate);
  }


  updateEmployeePassword(template: TemplateRef<any>) {
    this.isError = false;
    this.errorMsg = '';

    if (!this.validationService.validateNullUndefinedEmptyString(this.password)) {
      this.isError = true;
      this.errorMsg = 'Please enter old Password!!';
      return;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.userNewPass)) {
      this.isError = true;
      this.errorMsg = 'Please enter new Password!!';
      return;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.newpassword)) {
      this.isError = true;
      this.errorMsg = 'Please enter Confirm password !!';
      return;
    }

    if (this.userNewPass != this.newpassword) {
      this.isError = true;
      this.errorMsg = 'Password did not match. Please try again... !!';
      this.userNewPass = '';
      this.newpassword = '';
      return;
    }

    if (!this.validationService.validateAlphaNumericSpecialCharacters(this.userNewPass) &&
      !this.validationService.validateAlphaNumericSpecialCharacters(this.newpassword)) {
      this.isError = true;
      this.errorMsg = 'Password should not be set less than 8 characters and at least 1 lowercase character,  1 uppercase character, 1 digit , 1 special character should be there. Allowed Special characters are !@#$%^&*';
      return;
    }

    if (this.userNewPass == this.newpassword) {
      this.user.email = this.currentUser.email;
      this.user.password = this.setEncryption("PkdtRsJidheGitvS", this.password);
      this.user.newPassword = this.setEncryption("PkdtRsJidheGitvS", this.newpassword);

      this.employeeService.updateEmployeePassword(this.user).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.cancelRequest();
          this.userLogout();
          this.openAlertMod(template, response.serviceResponse);
          if(this.currentUser.isNew == "true"){
            this.userLogout();
          }
          this.reset();
        } else {
          this.isError = true;
          this.errorMsg = response.serviceResponse;
        }
      });
    } else {
      this.isError = true;
      this.errorMsg = 'Password and Confirm Password do not match !!';
      return;
    }
  }

  //modal

  cancelRequest() {
    this.modalRef.close();
    this.reset();
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }
  
  public openMenu: boolean = false;
  isOver = false;

  // routingFunction(message: string) {
  //      this.router.navigate(['/'+message]);
  //     this.clickMenu();
       
  // }

  routingFunction(message: string) {
    const url = `${window.location.origin}/#/${message}`;
    window.open(url, '_blank');
       //this.router.navigate(['/'+message]);
      this.clickMenu();
       
  }
  
  
  clickMenu() {
    this.openMenu = !this.openMenu;
  }
 
    LmsRedirection() {
    let obj = new Object();
    obj = { email: this.currentUser.email, token: sessionStorage.getItem('token') };


    //obj = { email: "mohamed.owais@apmosys.com"};
    //obj = { email: "mohamed2.owais@apmosys.com"};
    this.employeeService.IsValidateLMSPORTAL(obj).subscribe((response: any) => {
      //this.lmsauthentication = response.serviceResponse;
      this.lmsauthentication = response.serviceResponse;

      if (response.serviceStatus == "success") {
        window.open(response.serviceResponse, '_blank');
      }
      else {
        window.open(`${this.lmsbaseurl}home/sign_up`, '_blank');
      }
    })

  }
 
}
