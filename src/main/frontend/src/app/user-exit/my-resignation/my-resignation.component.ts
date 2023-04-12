import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Asset } from 'src/app/models/asset';
import { Employee } from 'src/app/models/employee';
import { EmployeeExit } from 'src/app/models/employeeExit';
import { Feature } from 'src/app/models/feature';
import { Log } from 'src/app/models/log';
import { SurveyOption } from 'src/app/models/sureyOption';
import { Survey } from 'src/app/models/survey';
import { SurveyQuestion } from 'src/app/models/surveyQuestion';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExitService } from 'src/app/services/exit.service';
import { SurveyService } from 'src/app/services/survey.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-my-resignation',
  templateUrl: './my-resignation.component.html',
  styleUrls: ['./my-resignation.component.css']
})
export class MyResignationComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('preview_response_template')
  previewResponseTemplate: TemplateRef<any>

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  feature="My Resignation";
  userMapping:any = {};
  log:Log;

  employeeObj: Employee = new Employee();
  assetObj:Asset = new Asset();
  employeeExitObj:EmployeeExit = new EmployeeExit();

  currentUser:User;
  exitEmployeeId:any;

  isResign:boolean = false;
  isResignDetails: boolean = false;
  isConsentCheck:boolean = false;
  isReleivingDate:boolean = false;
  isConsentReceived:boolean = false;
  isCurrentUser:boolean = false;
  isViewTextEditor: boolean = false;
  showResignationApplication: boolean = false;

  exitAssetDetailList:any[] = [];
  employeeInfo:any[] = [];
  updatedConsentList:any[] = [];
  questionList:any[] = [];

  isSurveyLoaded:boolean = false;
  isAnswered:boolean = false;
  surveyObj:Survey = new Survey();
  allSurveyQuestionList:any[] = [];
  allAnsweredInterviewList:any[] = [];
  myResponseList:any[] = [];
  resignationApplicationList:any[] = [];

  dateOfRelieving:any;
  currentUserName:any;
  data:string;

  //Text Editor
  editorConfig: AngularEditorConfig = {
    editable: true,
    spellcheck: true,
    height: '20rem',
    minHeight: '5rem',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter text here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    uploadWithCredentials: false,
    sanitize: false,
    toolbarPosition: 'top'
  };

  constructor(
    private modalService: BsModalService,
    private employeeService : EmployeeService,
    private exitService : ExitService,
    private surveyService : SurveyService,
    public validationService: ValidationService,
    private authenticationService : AuthenticationService,
    private datePipe: DatePipe,
    private route: ActivatedRoute,
    private router : Router,
    private locationStrategy: LocationStrategy
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.route.params.subscribe((params:Params) => {
      this.exitEmployeeId = params['id'];
    });

    console.log( this.router.url, " : url");

     // Dynamic Subfeature Flags 
     let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
     featureMap.subFeatures?.forEach(sub => {
       this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
     });
     console.log(this.feature, this.userMapping);

    this.currentUserName = this.currentUser.name[0].toUpperCase() + this.currentUser.name.slice(1).toLowerCase();

    //Default Resignation mail
    this.employeeExitObj.resignationMail = "<div><div style='text-align: left;'><br></div><div style='text-align: left;'>Dear HR,</div><div style='text-align: left;'><br></div><div style='text-align: left;'>I am writing to formally submit my resignation from my position at ApMoSys.</div><div style='text-align: left;'><br></div><div style='text-align: left;'>I have truly appreciated the opportunities I have had during my time at ApMoSys. I have learned and grown both professionally and personally, and I am grateful for the support and guidance provided by the entire team.</div><div style='text-align: left;'><br></div><div style='text-align: left;'>I will do my best to ensure a smooth transition during my remaining time at the company.</div><div style='text-align: left;'><br></div><div style='text-align: left;'>Thank you again for the opportunities and experiences I have had at ApMoSys. I am grateful for the relationships I have formed during my tenure here.</div><div style='text-align: left;'><br></div><div style='text-align: left;'>Sincerely,</div><div style='text-align: left;'>"+this.currentUserName+"<br></div></div>";

    this.getExitSurvey();
    this.sectionViewInit();
    this.preventBackButton();
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit(){
    if(this.exitEmployeeId != null && (this.currentUser.employeeRole != 'Employee' && this.currentUser.employeeRole != 'TeamLead')){
      this.getEmployeeInfo(this.exitEmployeeId);
    }else{
      this.getEmployeeResignationDetails();
    }

    if(this.currentUser.employeementId == this.exitEmployeeId || this.exitEmployeeId == undefined){
      this.isCurrentUser = true;
      this.router.navigate(['/user-exit/my-resignation']);
    }else{
      this.isCurrentUser = false;
    }
  }

  openResignRuleModal(template: TemplateRef<any>){
    this.isConsentCheck = false;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  openRevokeApplicationModal(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  showTextEditor(){
    this.isViewTextEditor = true;
  }

  consentCheckbox(event){
    if(event.target.checked){
      this.isConsentCheck = true;
    }else{
      this.isConsentCheck = false;
    }
  }

  getEmployeeInfo(employmentId:any){
    this.employeeObj.employeementId = employmentId;
    this.exitService.getEmployeeInfo(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeInfo = response.serviceResponse;

        this.employeeInfo.forEach((obj) => {
          if(obj.dateOfResign != null){
            this.isReleivingDate = true;
            this.getEmployeeExitAssetDetails(this.alertTemplate, this.exitEmployeeId);
          }else{
            this.employeeInfo = [];
            this.exitEmployeeId = null;
            this.router.navigate(['/user-exit/my-resignation']);
            // this.getEmployeeResignationDetails();
          }
        });
        console.log(this.employeeInfo, " :   this.employeeInfo");
      } else {
        console.log(response.serviceResponse);
      }
    });
  }

  applyResignationApplication(template: TemplateRef<any>){
    this.cancelRequest();

    this.employeeExitObj.empId = this.currentUser.empId;
    this.exitService.createResignationApplication(this.employeeExitObj).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.isResign = false;
            this.showResignationApplication = true;
            this.getEmployeeResignationDetails();
            this.openAlertMod(template, response.serviceResponse);
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
  }

  // resign(template: TemplateRef<any>){
  //   this.cancelRequest();
  //   const currentDate = this.datePipe.transform(new Date(), 'yyyy-MM-dd');

  //   this.employeeObj.dateOfResign = currentDate;
  //   this.employeeObj.empId = this.currentUser.empId;

  //   this.exitService.updateEmployeeResignationDetails(this.employeeObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.isResignDetails = true;
  //       this.isResign = false;
  //       this.getEmployeeResignationDetails();
  //       console.log(response.serviceResponse);
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
  // }

  getEmployeeResignationDetails(){
    this.cancelRequest();

    let employeeExitObj = new EmployeeExit();
    employeeExitObj.empId = this.currentUser.empId;
    this.exitService.getEmployeeResignationDetails(employeeExitObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.resignationApplicationList = response.serviceResponse;
        this.isResign = true;

        let resignationApproved = this.resignationApplicationList.find((x) => x.resignationStatus == 'Approved');
        if(resignationApproved != null && resignationApproved.dateOfResign != null){
          this.employeeExitObj = resignationApproved;
          this.isResignDetails = true;
          this.isResign = false;

          if (moment(new Date()).format('YYYY-MM-DD') >= moment(resignationApproved.dateOfRelieving).format('YYYY-MM-DD')) {
            this.isReleivingDate = true;
            this.isResignDetails = false;
            this.isResign = false;
            this.getEmployeeExitAssetDetails(this.alertTemplate,this.exitEmployeeId);
          }
        }

        let resignationPending = this.resignationApplicationList.find((x) => x.resignationStatus == 'Pending');
        console.log(resignationPending, " : resignationPending");
        
        if(resignationPending != null){
          this.showResignationApplication = true;
          this.isResign = false;
        }
      } else {
        this.isResign = true;
        console.log(response.serviceResponse);
      }
    });
  }

  getEmployeeExitAssetDetails(template: TemplateRef<any>, exitEmployeeId:any){
    this.cancelRequest();

    if(exitEmployeeId != null){
      this.employeeObj.employeementId = exitEmployeeId;
    }else{
      this.employeeObj.employeementId = this.currentUser.employeementId;
    }

    this.exitService.getEmployeeExitAssetDetails(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.exitAssetDetailList = response.serviceResponse;

        if(exitEmployeeId != null){
          if(this.currentUser.departmentName != "Director"){
            this.exitAssetDetailList = this.exitAssetDetailList.filter(x => x.departmentName == this.currentUser.departmentName);
          }
        }
        
        this.exitAssetDetailList.forEach((x) => {
          if(x.deptConsent == 'false' || x.deptConsent == null){
            x.deptConsent = false;
          }else{
            x.deptConsent = true;
          }

          if(x.departmentName == null){
            x.departmentName = 'Manager';
          }

          if(x.isAssigned == 'true'){
            x.isAssigned = 'Yes';
          }else{
            x.isAssigned = 'No';
          }
        });
        console.log(this.exitAssetDetailList, " : exitAssetDetailList");
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  selectDeptConsentCheckbox(updatedConsent){
    const alreadyUpdatedConsent = this.updatedConsentList.findIndex((x) => x.deptConsent == updatedConsent.deptConsent && x.employeeAssetMapId == updatedConsent.employeeAssetMapId);
      if(alreadyUpdatedConsent >= 0){
        this.updatedConsentList.splice(alreadyUpdatedConsent,1);
      }else{
        this.updatedConsentList.push(updatedConsent);
      }
      console.log(this.updatedConsentList);
  }

  submitConsent(template: TemplateRef<any>){

    if(this.updatedConsentList.length == 0 || this.updatedConsentList == undefined){
        this.alertMessage = `Please give consent to atleast one asset !!`;
        this.openAlertMod(template, this.alertMessage);
        return;
    }

    this.employeeObj.deptHeadConsentList = this.updatedConsentList;
    this.employeeObj.updatedBy = this.currentUser.empId;
    this.exitService.setDeptHeadConcent(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getEmployeeResignationDetails();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getExitSurvey(){
    this.questionList = [];
    this.surveyService.getAllSurveys().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.questionList = response.serviceResponse;
        this.questionList = this.questionList.filter(x => x.type == "exit");
        this.getAllAnsweredExitInterview();
        console.log("this.questionList : ", this.questionList[0]);
      }else{
        console.log(response.serviceResponse);
      }
    });
  }

  revokeMyResignationApplication(template: TemplateRef<any>){
    this.cancelRequest();

    let employeeExitObj = new EmployeeExit();
    employeeExitObj.empId = this.currentUser.empId;
    employeeExitObj.revokeReason = this.employeeExitObj.revokeReason;
    
    this.exitService.revokeMyResignationApplication(employeeExitObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.isReleivingDate = false;
        this.isResignDetails = false;
        this.showResignationApplication = false;
        this.isViewTextEditor = false;
        this.isResign = true;
        this.openAlertMod(template, response.serviceResponse);
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getExitInterviewForm(template: TemplateRef<any>, exitTemplate: TemplateRef<any>){
          //take generate exit interview form

          let surveyObj = this.questionList[0];
          this.isSurveyLoaded = false;
          this.surveyObj = new Survey();
          this.allSurveyQuestionList = [];

          this.surveyService.getAllQuestionsBySurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              this.allSurveyQuestionList = response.serviceResponse;
              console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);

              this.surveyObj = surveyObj;
              this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
                survey.optionsList = JSON.parse(survey.options);
                survey.required = JSON.parse(survey.required);
              });

              const surveyQuestionsTemplate: string = this.createTemplate();

              const formStart = `<form id="surveyForm">`
              const formEnd = `</form>`
              const surveyTemplate = formStart + surveyQuestionsTemplate + formEnd;

              this.modalRef = this.modalService.show(exitTemplate, { class: 'modal-lg' });

              setTimeout(() => {
                let surveyContainer = document.getElementById('surveyContainer');
                surveyContainer.insertAdjacentHTML('afterbegin', surveyTemplate);
                this.isSurveyLoaded = true;
              }, 1000)
            } else {
              console.error(response.serviceResponse);
            }
          });
  }

  getAllAnsweredExitInterview(){
    this.allAnsweredInterviewList = [];
    
    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;

    this.exitService.getAnsweredInterviewByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allAnsweredInterviewList = response.serviceResponse;
       console.log("this.allAnsweredInterviewList : ", response.serviceResponse);
       this.allAnsweredInterviewList.forEach((Object) => {
          let exitInterviewObj = this.questionList.find((ques) => Object.surveyId == ques.surveyId);
          if(exitInterviewObj) this.isAnswered = true;
        }); 
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  createTemplate():string{
    let surveyTemplate = ``;
    this.allSurveyQuestionList.forEach((question:SurveyQuestion, qIndex) => {
 
     let finalQuestionTemplate = ``;
     const questionStartTemplate = `<div class="row"><div class="form-group">`
     const questionEndTemplate = `</div></div>`
     const questionRequiredTemplate = `<span class="text-danger">*</span>`
     let isQuestionRequired = (question.required == true)? questionRequiredTemplate : '';
     let questionTemplate = `<h5 class="mb-0"><i class="fa-solid fa-q question-icon"></i>.&nbsp; ${(question.question !== undefined && question.question !== null)? question.question : ''}${isQuestionRequired}</h5><small class="text-secondary">${(question.description !== undefined && question.description !== null)? question.description : ''}</small>`
 
     finalQuestionTemplate = questionStartTemplate + questionTemplate;
     
      if (question.optionType == "text") {
        let textTemplate: any =`<textarea class="form-control mt-1" rows="1" name="question-${qIndex+1}"></textarea>`;
        finalQuestionTemplate = finalQuestionTemplate + textTemplate;
      } else if (question.optionType == "checkbox") {
 
       let optionTemplate = '';
       question.optionsList.forEach((option:SurveyOption, opIndex) => {
         let checkboxTemplate: any =
         `
           <div class="form-check">
            <input class="form-check-input" type="checkbox" id="q-${qIndex+1}-check-option-${opIndex+1}" value="${option.optionValue}" name="question-${qIndex+1}">
            <label class="form-check-label" for="q-${qIndex+1}-check-option-${opIndex+1}">${option.optionValue}</label>
            </div>
          `;
 
          optionTemplate = optionTemplate + checkboxTemplate;
       });
       
       finalQuestionTemplate = finalQuestionTemplate + optionTemplate;
      } else if (question.optionType == "radio") {
 
       let optionTemplate = '';
       question.optionsList.forEach((option:SurveyOption, index) => {
         let radioboxTemplate: any =
         `
         <div class="form-check">
          <input class="form-check-input" type="radio" id="q-${qIndex+1}-radio-option-${index+1}" value="${option.optionValue}" name="question-${qIndex+1}">
          <label class="form-check-label" for="q-${qIndex+1}-radio-option-${index+1}">${option.optionValue}</label>
          </div>
        `;
 
        optionTemplate = optionTemplate + radioboxTemplate;
       });
       finalQuestionTemplate = finalQuestionTemplate + optionTemplate;
      }
 
      finalQuestionTemplate = finalQuestionTemplate + questionEndTemplate;
      surveyTemplate = surveyTemplate + finalQuestionTemplate;
    });
 
    // console.log("surveyTemplate : ", surveyTemplate);
    return surveyTemplate;
  }

  validateSurveyResponse(surveyObj:Survey,template: TemplateRef<any>){
    let flag = true;

    for (let index = 0; index < surveyObj.surveyQuestionList.length; index++) {
      let question = surveyObj.surveyQuestionList[index];
      if(question.required && !this.validationService.validateNullUndefinedEmptyString(question.response)){
        this.alertMessage = `Please provide response for Question ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        break;
      }
    }
    return flag;
  }

  onSubmit(template: TemplateRef<any>){
    this.cancelRequest();
    const form:any = document.getElementById('surveyForm');

    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;
    surveyObj.surveyQuestionList = [];

    this.allSurveyQuestionList.forEach((question, index) => {

      let surveyQuestion = new SurveyQuestion();
      surveyQuestion.question = question.question;
      surveyQuestion.surveyId = question.surveyId;
      surveyQuestion.required = question.required;

      if(question.optionType == 'checkbox'){
        let response = [];
        form.elements[`question-${index+1}`]?.forEach((checkboxOption) => {
          if(checkboxOption.checked) response.push(checkboxOption.value);
        });
        surveyQuestion.response = response.join(", ");
      }else{
        surveyQuestion.response = form.elements[`question-${index+1}`].value;
      }
      surveyObj.surveyQuestionList.push(surveyQuestion);
    });

    console.log("On Survey Submit : ", surveyObj);
    let inputValidated: boolean = this.validateSurveyResponse(surveyObj, template);
    if (!inputValidated) return;
    this.exitService.setExitInterviewResponseByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.getExitSurvey();
        this.openAlertMod(template, response.serviceResponse);
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    }); 
  }

  viewExitInterviewResponse(){
    this.myResponseList = [];
    this.surveyObj = new Survey();

    if(this.exitEmployeeId != null){
      this.surveyObj.employeementId = this.exitEmployeeId;
    }else{
      this.surveyObj.employeementId = this.currentUser.employeementId;
    }
    this.surveyObj.surveyId = this.questionList[0].surveyId;
    console.log(this.surveyObj.surveyId, "this.surveyObj.surveyId==");
    
    console.log("For View My Response : ", this.surveyObj);
    this.exitService.getExitInterviewResponseBySurveyIdAndEmp(this.surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.myResponseList = response.serviceResponse;
        console.log("this.myResponseList : ", this.myResponseList);
        this.openExitInterviewPreviewMod(this.previewResponseTemplate);
      }else{
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    }); 
  }

  openExitInterviewPreviewMod(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
