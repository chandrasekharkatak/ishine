import { Component, Input, OnInit, TemplateRef, ViewChild} from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { User } from '../models/user';
import { first } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { ValidationService } from '../services/validation.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import * as CryptoJS from 'crypto-js';

@Component({
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
  modalRef: BsModalRef = new BsModalRef();

  // stop modal to close
  config = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard  : false
  };

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private router: Router,
  ){
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
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
    this.currentUserName = this.currentUser.name[0].toUpperCase() + this.currentUser.name.slice(1).toLowerCase();
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
        console.log(response.serviceResponse);
        sessionStorage.removeItem('currentUser');
        // delete method call for cookies
        this.authenticationService.deleteCookies();
        this.authenticationService.setcurrentUserSubject(null);
        this.router.navigate(['/login']);
        location.reload();
      } else {
        if(response.serviceResponse == "Session already destroyed"){
          this.authenticationService.stopUserSessionCheck();
          sessionStorage.removeItem('currentUser');
          // delete method call for cookies
          this.authenticationService.deleteCookies();
          this.authenticationService.setcurrentUserSubject(null);
          this.router.navigate(['/login']);
          location.reload();
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
    console.log(this.currentUser.isNew)
    if(this.currentUser.isNew == 'true'){
      this.modalRef = this.modalService.show(changePasswordTemplate,this.config);
    }else{
      this.modalRef = this.modalService.show(changePasswordTemplate);
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
      this.errorMsg = 'Password should not be set  less than 8 characters. Only alphanumeric and @#$%!+*÷=/_-\'":;,()^{}~[] are allowed !!';
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
    this.modalRef.hide();
    this.reset();
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

}
