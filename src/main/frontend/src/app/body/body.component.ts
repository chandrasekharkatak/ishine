import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../models/user';
import { first } from 'rxjs/operators';
import { AuthenticationService } from '../services/authentication.service';
import { EmployeeService } from '../services/employee.service';
import { ValidationService } from '../services/validation.service';
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

  fieldTextType: boolean = false;
  fieldTextTypePassword: boolean = false;
  fieldTextTypeOldPass: boolean = false;
  isError:boolean=false;
  oldPasswordValid:boolean = false;

  password:any;
  userNewPass:any;
  userOldPassword:any;
  newpassword:any;
  errorMsg:any;
  empId:any;
  user:User = new User();

  constructor(
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private router: Router,
  ){
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
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
      styleClass= 'navbar-trimmed'
    }else if(this.collapsed && this.screenWidth <= 768 && this.screenWidth > 0){
      styleClass= 'navbar-md-screen'
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
        location.reload();
        this.router.navigate(['/login']);
      } else {
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

    reset(){
      this.userOldPassword= "";
      this.password= "";
      this.userNewPass= "";
    }


    checkEmployeeOldPassword(){

      console.log("butoon clickedddd");
      this.isError=false;
      this.errorMsg='';

      this.user.empId = this.currentUser.empId;
      this.user.password = this.setEncryption("PkdtRsJidheGitvS",this.password);

      this.employeeService.checkEmployeeOldPassword(this.user).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Fail") {
              this.isError=true;
              this.errorMsg=response.serviceResponse;
            }else{
              this.oldPasswordValid=true;
            }
      });
    } 

  updateEmployeePassword(){

    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.password)){
      this.isError=true;
      this.errorMsg='Please enter old Password!!';
      return;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.userNewPass)){
      this.isError=true;
      this.errorMsg='Please enter new Password!!';
      return;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(this.newpassword)){
      this.isError=true;
      this.errorMsg='Please enter Confirm password !!';
      return;
    }

    if(this.userNewPass == this.newpassword){
    this.user.email = this.currentUser.email;
    this.user.password = this.setEncryption("PkdtRsJidheGitvS",this.password);
    this.user.newPassword = this.setEncryption("PkdtRsJidheGitvS",this.newpassword);

    this.employeeService.updateEmployeePassword(this.user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isError=true;
        this.errorMsg='Password changed successfully!!';
        this.reset();
      } else {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });
   }else{
      this.isError=true;
      this.errorMsg='Password and Confirm Password do not match !!';
      return;
   }
  }

}
