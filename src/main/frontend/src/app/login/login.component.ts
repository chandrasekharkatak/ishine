import { DatePipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from '../models/feature';
import { SubFeature } from '../models/subFeature';
import { Tab } from '../models/tab';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { SubfeatureService } from '../services/subfeature.service';
import { ValidationService } from '../services/validation.service';
import * as CryptoJS from 'crypto-js';

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

  /* User-Mappings */
  allSubFeatures:any[] = [];
  featureList:any[] = [];
  allMappedSubfeatures:any[] = [];



  constructor(
    private validationService:ValidationService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private router: Router,
    private authenticationService: AuthenticationService,
    private subfeatureService: SubfeatureService
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

    this.reset();
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
    this.fieldTextType = false;
    this.fieldTextTypePassword = false;
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
    this.user.password = this.setEncryption("PkdtRsJidheGitvS",this.password);
    // this.user.password = this.password;

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

  async onConfirmLoginOTP(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter otp !!';
      return;
    }

    this.user.otp = this.userOTP;

    const response: any = await this.authenticationService.authenticateUserWithOTP(this.user).toPromise();
    if (response.serviceStatus == "Success") {
      console.log("USER", response.serviceResponse);
      const responseObj = response.serviceResponse;
      let user = responseObj[0];
      this.allMappedSubfeatures = responseObj[1];

      let getAllSubFeaturesResp: any = await this.subfeatureService.getAllSubFeatures().toPromise();
      if (getAllSubFeaturesResp.serviceStatus == "Success") {
        this.allSubFeatures = getAllSubFeaturesResp.serviceResponse;
        this.getFeatureList();
      
      /*Mapping & tab list*/
      this.user.empId = user.empId;
      this.user.name = user.name;
      this.user.managerId = user.managerId;
      this.user.departmentId = user.departmentId;
      this.user.userMapping = this.getActiveSubFeatures();
      this.user.tabList = this.getTabList();
      sessionStorage.setItem('currentUser', JSON.stringify(this.user));
      this.authenticationService.setcurrentUserSubject(this.user);
      this.router.navigate(['/home']);
      }
    } else {
      this.isError = true;
      this.errorMsg = response.serviceResponse;
    }
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

  getTabList(){
    let tabList:Tab[] = [];
        this.allMappedSubfeatures.forEach(userMap => {
          if(tabList.length == 0 || !tabList.find(tab => tab.tabName === userMap.tabName)){
            let tab = new Tab();
            tab.tabName = userMap.tabName;
            tab.tabRouteName = userMap.tabRouteName;
            tab.tabIcon = userMap.tabIcon;
    
            tabList.push(tab);
          }      
        });
    return tabList;
  }

  getFeatureList():any{
    this.featureList = [];
    this.allSubFeatures.forEach(featureMap => {
      if(this.featureList.length == 0 || !this.featureList.find(feature => feature.featureName === featureMap.featureName))
      {
        let feat = new Feature();
        feat.featureId = featureMap.featureId;
        feat.featureName = featureMap.featureName;
        feat.tabName = featureMap.tabName;

        this.featureList.push(feat);
      }      
    });
  }

  getActiveSubFeatures():any{
    // console.log("featureList :", this.featureList);
    this.allSubFeatures.forEach(sub => {
      let _sub = new SubFeature();
      _sub.subFeatureMasterId = sub.subFeatureMasterId;
      _sub.subFeatureName = sub.subFeatureName;

      if(this.allMappedSubfeatures.find(subMap => subMap.subFeatureMasterId == sub.subFeatureMasterId)){
        _sub.isActive = true;
      }else{
        _sub.isActive = false;
      }

      
      // let feature  = this.featureList.find(feature => feature.featureId == sub.featureId);
      // console.log("sub - featureId : ", sub.featureId, " | feature :", feature);
      this.featureList.find(feature => feature.featureId == sub.featureId)?.subFeatures.push(_sub);
    });
    console.log("featureList :", this.featureList);
    return this.featureList;
  }

}
