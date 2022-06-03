import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Tab } from '../models/tab';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { ValidationService } from '../services/validation.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  //flags 
  isLoginForm:boolean=true;
  isOtpForm:boolean=false;
  isForgotPassForm:boolean=false;
  isChangePassForm:boolean=false;
  isError:boolean=false;
  isLoginOTP:boolean=false;

  fieldTextType: boolean = false;
  fieldTextTypePassword: boolean = false;


  userName: any;
  password: any;
  userOTP:any;
  userEmailId:any;
  userNewPass:any;
  userConfirmNewPass:any;
  user:User = new User();
  errorMsg:any;

  constructor(
    private validationService:ValidationService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private router: Router,
    private authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
  }

  toggleFieldTextType() {
    this.fieldTextType = !this.fieldTextType;
  }

  toggleFieldChangePassword() {
    this.fieldTextTypePassword = !this.fieldTextTypePassword;
  }

  showOtpForm(){
    this.isLoginForm=false;
    this.isOtpForm=true;
    this.isForgotPassForm=false;
    this.isChangePassForm=false;
  }

  showForgotPassForm(){
    this.isLoginForm=false;
    this.isOtpForm=false;
    this.isForgotPassForm=true;
    this.isChangePassForm=false;

    this.reset();
  }

  showLoginForm(){
    this.isLoginForm=true;
    this.isOtpForm=false;
    this.isForgotPassForm=false;
    this.isChangePassForm=false;

    this.reset();
  }

  showChangePassForm(){
    this.isLoginForm=false;
    this.isOtpForm=false;
    this.isForgotPassForm=false;
    this.isChangePassForm=true;
  }

  reset(){
    this.userName= "";
    this.password= "";
    this.userOTP= "";
    this.userEmailId= "";
    this.userNewPass= "";
    this.userConfirmNewPass= "";

    this.isError=false;
  }


  onLogin(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userName)){
      this.isError=true;
      this.errorMsg='Please enter username !!';
      return;
    }else if(!this.validationService.validateEmail(this.userName)){
      this.isError=true;
      this.errorMsg='Please enter valid username !!';
      return;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.password)){
      this.isError=true;
      this.errorMsg='Please enter password !!';
      return;
    }

    this.user.email = this.userName;
    this.user.password = this.password;

    this.authenticationService.authenticateUser(this.user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isLoginOTP=true;
        this.showOtpForm();
      } else {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });
  }

  onConfirmLoginOTP(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter otp !!';
      return;
    }

    this.user.otp = this.userOTP;

    this.authenticationService.authenticateUserWithOTP(this.user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        console.log("USER",response.serviceResponse);
        const responseObj = response.serviceResponse;
        let user = responseObj[0];
        let userMapping = responseObj[1];
        /*Mapping & tab list*/
        let tabList:Tab[] = [];
        userMapping.forEach(userMap => {
          if(tabList.length == 0 || !tabList.find(tab => tab.tabName === userMap.tabName)){
            let tab = new Tab();
            tab.tabName = userMap.tabName;
            tab.tabRouteName = userMap.tabRouteName;
            tab.tabIcon = userMap.tabIcon;
    
            tabList.push(tab);
          }      
        });
    
        console.log("tabList : ", tabList);
        this.user.empId = user.empId;
        this.user.name = user.name;
        this.user.userMapping = userMapping;
        this.user.tabList = tabList;
        sessionStorage.setItem('currentUser', JSON.stringify(this.user));
        this.authenticationService.setcurrentUserSubject(this.user);
        this.router.navigate(['/home']);
      } else {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });
  }

  onSendOTP(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userEmailId)){
      this.isError=true;
      this.errorMsg='Please enter email id !!';
      return;
    }else if(!this.validationService.validateEmail(this.userEmailId)){
      this.isError=true;
      this.errorMsg='Please enter valid email id !!';
      return;
    }

    this.isLoginOTP=false;
    this.showOtpForm();
  }

  onConfirmForgotPassOTP(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter otp !!';
      return;
    }

    this.user.otp = this.userOTP;
    this.showChangePassForm();
  }

  onChangePassword(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userNewPass)){
      this.isError=true;
      this.errorMsg='Please enter New Password !!';
      return;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.userConfirmNewPass)){
      this.isError=true;
      this.errorMsg='Please enter Confirm New Password !!';
      return;
    }

    this.showLoginForm();
  }
}
