import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from '../models/feature';
import { SubFeature } from '../models/subFeature';
import { Tab } from '../models/tab';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { SubfeatureService } from '../services/subfeature.service';
import { ValidationService } from '../services/validation.service';
import * as CryptoJS from 'crypto-js';
import { EmployeeService } from '../services/employee.service';
import { BnNgIdleService } from 'bn-ng-idle';
import { BodyComponent } from '../body/body.component';
import { LogService } from '../services/log.service';
import { Log } from '../models/log';
import * as moment from 'moment';
import { enableAppreciation } from '../models/enableAppreciation';	
import { AuthGuard } from '../guards/auth.guard';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit{

  //flags 
  isLoginForm:boolean=true;
  isOtpForm:boolean=false;
  isForgotPassOtpForm:boolean=false;
  isForgotPassForm:boolean=false;
  isChangePassForm:boolean=false;
  isError:boolean=false;

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
  userEmailIdForOtpVerification:any;

  /* User-Mappings */
  allSubFeatures:any[] = [];
  featureList:any[] = [];
  allMappedSubfeatures:any[] = [];

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();
  enableAppreciation: enableAppreciation  = new enableAppreciation();	

  @ViewChild('reLogin_template') reLoginTemplate: TemplateRef<any>;

  // For session timeout check
  feature = "Profile";
  userMapping: any = {};
  currentUser: User;

  constructor(
    private validationService:ValidationService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private router: Router,
    private authenticationService: AuthenticationService,
    private subfeatureService: SubfeatureService,
    private employeeService: EmployeeService,
    private bnIdle:BnNgIdleService,
    private bodyComponent:BodyComponent,
    private logService:LogService,
    private authGaurd:AuthGuard
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  timeLeft: number = 60;
  timer: any;
  
    ngOnInit(): void {}

  toggleFieldTextType() {
    this.fieldTextType = !this.fieldTextType;
  }

  toggleFieldChangePassword() {
    this.fieldTextTypePassword = !this.fieldTextTypePassword;
  }
  startTimer() {
    this.timer = setInterval(() => {
      if (this.timeLeft > 0) {
        this.timeLeft--;
      } else {
        clearInterval(this.timer);
        // alert("Time's up!");
      }
    }, 1000);
  }

  resetTimer() {
    this.timeLeft = 60;
    if (this.timer) {
      clearInterval(this.timer);
    }
    this.startTimer();
  }

  showOtpForm(){
    this.isOtpForm=true;

    this.isForgotPassOtpForm=false;
    this.isLoginForm=false;
    this.isForgotPassForm=false;
    this.isChangePassForm=false;

    this.reset();
    this.startTimer();
  }

  showForgotPassOtpForm(){
    this.isForgotPassOtpForm=true;
    
    this.isOtpForm=false;
    this.isLoginForm=false;
    this.isForgotPassForm=false;
    this.isChangePassForm=false;

    this.reset();
  }

  showForgotPassForm(){
    this.isForgotPassForm=true;

    this.isLoginForm=false;
    this.isOtpForm=false;
    this.isForgotPassOtpForm=false;
    this.isChangePassForm=false;

    this.reset();
  }

  showLoginForm(){
    this.isLoginForm=true;
    
    this.isOtpForm=false;
    this.isForgotPassOtpForm=false;
    this.isForgotPassForm=false;
    this.isChangePassForm=false;

    this.reset();
  }

  showChangePassForm(){
    this.isChangePassForm=true;

    this.isLoginForm=false;
    this.isOtpForm=false;
    this.isForgotPassOtpForm = false;
    this.isForgotPassForm=false;
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
    }else if(!this.validationService.validateEmail(this.userName) ){
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
        this.user.password = null;
        this.showOtpForm();
      }
      else if (response.serviceStatus == 'Fail_1')
      {
        this.openReLoginMod(this.reLoginTemplate,response.serviceResponse)

      } else {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });
  }

  /* Allow user to relogin when user is already logged in another browser
  *  Added by suraj 12/08/2022
  */
  onReLogin(){
    this.user.password = null;
    this.showOtpForm();
    this.resendOTP(); // used to send otp
  }

  async onConfirmLoginOTP(){
    this.isError=false;
    this.errorMsg='';
    if(!this.validationService.validateNullUndefinedEmptyString(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter otp !!';
      return;
    }
    if(!this.validationService.validateLoginRegex(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter valid otp !!';
      return;
    }

    this.user.otp = this.userOTP;

   if(this.userOTP.length <= 8){
    const response: any = await this.authenticationService.authenticateUserWithOTP(this.user).toPromise();
    if (response.serviceStatus == "Success") {
      //console.log("USER", response.serviceResponse);
      const responseObj = response.serviceResponse;
      let user = responseObj[0];
      this.allMappedSubfeatures = responseObj[1];
      this.authenticationService.sessionString = responseObj[2];
      this.authenticationService.sessionTimeout= responseObj[3];
      sessionStorage.setItem('maxFileSize',responseObj[4]);
      sessionStorage.setItem('maxRequestSize',responseObj[5]);
      /* Saving INFO for Logs */
      let log:Log = responseObj[6];
      log.empId = user.empId;
      this.enableAppreciation = responseObj[7];	
      //console.log("enableAppreciation",this.enableAppreciation);	
      //console.log("checking"+sessionStorage.maxFileSize);
      this.authenticationService.setCookie({name:"SESSIONID",value:this.authenticationService.sessionString,session:true});
      sessionStorage.setItem('token', this.authenticationService.sessionString);
      

      let getAllSubFeaturesResp: any = await this.subfeatureService.getAllSubFeatures().toPromise();
      if (getAllSubFeaturesResp.serviceStatus == "Success") {
        this.allSubFeatures = getAllSubFeaturesResp.serviceResponse;
        this.getFeatureList();
      
      /*Mapping & tab list*/
      this.user.otp = null;
      this.user.empId = user.empId;
      this.user.name = user.name;
      this.user.managerId = user.managerId;
      this.user.departmentId = user.departmentId;
      this.user.employmentstatus = user.employmentstatus;
      this.user.gender = user.gender;
      this.user.dateOfJoining = user.dateOfJoining;
      this.user.timesheetLockDays = user.timesheetLockDays;
      this.user.employeementId = user.employeementId;
      this.user.isNew = user.isNew;
      this.user.departmentName = user.departmentName;
      this.user.dateOfResign = user.dateOfResign;
      this.user.isUserInfoUpdated = (user.isUserInfoUpdated == null) ? true : JSON.parse(user.isUserInfoUpdated);
      this.user.userMapping = this.getActiveSubFeatures();
      this.user.tabList = this.getTabList();	
      this.user.appreciationEventInfo =  this.enableAppreciation;
      this.user.isAppreciationEnable = user.isAppreciationEnable;
      this.user.employeeRole = user.employeeRole;
      this.user.managerName = user.managerName;
      this.user.managerEmail = user.managerEmail;
      this.user.hodId = user.hodId;
      this.user.hodName = user.hodName;
      this.user.hodEmail = user.hodEmail;
      this.user.isTimesheetLockCheckEnable = user.isTimesheetLockCheckEnable;
      this.user.timesheetBackDatedDays = user.timesheetBackDatedDays;
      this.user.compOffLockDays = user.compOffLockDays;
      this.user.leaveBackdatedLockDays = user.leaveBackdatedLockDays;
      this.user.leaveFuturedatedLockDays = user.leaveFuturedatedLockDays;
      this.user.reportingManagerId = user.reportingManagerId;
      this.user.reportingManagerName = user.reportingManagerName;
      this.user.reportingManagerEmail = user.reportingManagerEmail;
      this.user.approvalsTo = user.approvalsTo;
      this.user.revokeReporteeLeaveValidity = user.revokeReporteeLeaveValidity;
      this.user.policyReadConsent = user.policyReadConsent;
      this.user.notificationConsent = user.notificationConsent;
      this.user.poPortalAllProjectApi = user.poPortalAllProjectApi;
      this.user.probationPeriod = user.probationPeriod;
      this.user.releaseNoteNotification = user.releaseNoteNotification;
      this.user.newsletterReadCheck = user.newsletterReadCheck;
      this.user.workLocation = user.workLocation;
      this.user.maritalStatus = user.maritalStatus;
      this.user.jobRoleName = user.jobRoleName;
      if(user.isNew=="true"){
        sessionStorage.setItem('FirstTimeLogin', "true");
    
      }else{
        sessionStorage.setItem('FirstTimeLogin', "false");
    
      }
      sessionStorage.setItem('currentUser', JSON.stringify(this.user));
      this.authenticationService.setcurrentUserSubject(this.user);
      sessionStorage.setItem('logInfo', JSON.stringify(log));
      this.logService.updateLogInfo(log);
      this.timeSession();

      if(this.authGaurd.id != null){
        let url = this.authGaurd.currentUrl;
        //console.log(url, " : url");
        
        if(url.includes("user-survey")){
          this.router.navigate(['/user-survey', this.authGaurd.id]);
        }
        if(url.includes("my-resignation")){
          this.router.navigate(['/user-exit/my-resignation', this.authGaurd.id]);
        }
        if(url.includes("resource-management")){
          this.router.navigate(['/user-team/resource-management', this.authGaurd.id]);
        }
        if(url.includes("helpdesk")){
          this.router.navigate(['/helpdesk', this.authGaurd.id]);
        }
      }else{
        this.router.navigate(['/home']);
      }

      if (this.user.tabList.find(e => e.tabName === 'HR Policies')) {
        // if(this.currentUser.isAllPolicyMarkAsRead == 'false'){
        //   this.router.navigate(['/user-policies']);
        // }
        if(this.currentUser.policyReadConsent != null){
          this.router.navigate(['/user-policies']);
        }
      }
      
      if(this.user.tabList.find(e => e.tabName === 'Newsletters')){
        if(this.currentUser.newsletterReadCheck != null){
          this.router.navigate(['/newsletters']);
        }
      }
      
      this.authenticationService.startUserSessionCheck();
      }
    } else {
      this.isError = true;
      this.errorMsg = response.serviceResponse;
    }
  
  }else {
    this.isError = true;
      this.errorMsg = "Invalid OTP !!";
  }
   
  }

   
  timeSession(){
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

    // //console.log("================== user session timeout : ", this.userMapping.user_session_timeout, " ==================");
    
    if(this.userMapping.user_session_timeout){
      this.bnIdle.startWatching(this.authenticationService.sessionTimeout).subscribe((isTimedOut: boolean) => {
        if (isTimedOut) {
         this.bodyComponent.userLogout();
          //console.log('session expired');
        }
      }); 
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

    let user = new User();
    user.email = this.userEmailId
    this.userEmailIdForOtpVerification = this.userEmailId;

    this.authenticationService.checkEmailWhenForgotPassword(user).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success"){
        this.showForgotPassOtpForm();
      }else{
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });
    // this.startTimer();
    
  }

  onConfirmForgotPassOTP(){
    this.isError=false;
    this.errorMsg='';
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter otp !!';
      return;
    }
    if(!this.validationService.validateLoginRegex(this.userOTP)){
      this.isError=true;
      this.errorMsg='Please enter valid otp !!';
      return;
    }

    let user = new User();
    user.email = this.userEmailIdForOtpVerification;
    user.otp = this.userOTP;

    this.authenticationService.checkOTPWhenForgotPassword(user).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success"){
        let sessionToken = response.serviceResponse;
        sessionStorage.setItem('token', sessionToken);     
        this.showChangePassForm();
      }
      else{
      this.isError=true;
      this.errorMsg=response.serviceResponse;
      return;
      }
    });
    
   
  }

  onChangePassword(template: TemplateRef<any>){
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

    if(this.userNewPass != this.userConfirmNewPass)
    {
      this.isError=true;
      this.errorMsg='Password did not match. Please try again... !!';
      this.userNewPass = '';
      this.userConfirmNewPass = '';
      return;
    }

    if(!this.validationService.validateAlphaNumericSpecialCharacters(this.userNewPass) && 
    !this.validationService.validateAlphaNumericSpecialCharacters(this.userConfirmNewPass)){
      this.isError=true;
      //Password should not be set  less than 8 characters and it should accept special, alphanumeric characters
      this.errorMsg='Password should not be set less than 8 characters and at least 1 lowercase character,  1 uppercase character, 1 digit , 1 special character should be there. Allowed Special characters are !@#$%^&*';
      return;
    }

    let user = new User();
    user.email = this.userEmailIdForOtpVerification;
    user.newPassword = this.setEncryption("PkdtRsJidheGitvS",this.userNewPass);

    this.employeeService.updateEmployeeForgotPassword(user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        sessionStorage.removeItem('token');
        this.openAlertMod(template, "Password changed successfully.");

        this.showLoginForm();
      } else {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });

    
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
    // //console.log("featureList :", this.featureList);
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
      // //console.log("sub - featureId : ", sub.featureId, " | feature :", feature);
      this.featureList.find(feature => feature.featureId == sub.featureId)?.subFeatures.push(_sub);
    });
    //console.log("featureList :", this.featureList);
    return this.featureList;
  }

  resendOTP(){

    this.userOTP = ''
    this.errorMsg = ''
    if(this.isForgotPassOtpForm){	
      this.user.email = this.userEmailIdForOtpVerification;	
    }	
    	
    //console.log("For Resend OTP : ", this.user);
    this.authenticationService.resendOTP(this.user).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      } else {
        this.isError=true;
        this.errorMsg=response.serviceResponse;
      }
    });
  }

   //modals
  openReLoginMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  
}
