import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { SurveyOption } from '../models/sureyOption';
import { Survey } from '../models/survey';
import { SurveyQuestion } from '../models/surveyQuestion';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { SurveyService } from '../services/survey.service';
import { ValidationService } from '../services/validation.service';

@Component({
  selector: 'app-user-survey',
  templateUrl: './user-survey.component.html',
  styleUrls: ['./user-survey.component.css']
})
export class UserSurveyComponent implements OnInit {

  feature = "Survey";
  currentUser: User;
  userMapping: any = {};

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isSurveyForm:boolean = false;
  isSurveyList:boolean = false;

  surveyObj:Survey = new Survey();
  allSurveyQuestionList:SurveyQuestion[] = [new SurveyQuestion()];

  allSurveyList:any[] = [];
  allAnsweredSurveyList:any[] = [];

  isSurveyLoaded:boolean = false;

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private surveyService : SurveyService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    // let featureMap: Feature = onTakeSurvey()this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });
    // console.log(this.feature, this.userMapping);
  
    this.sectionViewInit();

    console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);
    
  }

  sectionViewInit(){
    this.showSurveys();
  }

  showSurveyForm(){
    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [];

    this.isSurveyForm = true;
    
    this.isSurveyList = false;

  }

  showSurveys(){
    this.isSurveyList = true;
    
    this.isSurveyForm = false;

    this.getAllSurveys();
  }

  getAllSurveys(){
    this.allSurveyList = [];
    
    this.surveyService.getAllSurveys().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       this.allSurveyList = response.serviceResponse;
       this.getAllAnsweredSurveys();
       console.log("this.allSurveyList : ", this.allSurveyList); 
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  getAllAnsweredSurveys(){
    this.allAnsweredSurveyList = [];
    
    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;

    this.surveyService.getAnsweredSurveysByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allAnsweredSurveyList = response.serviceResponse;
       console.log("this.allAnsweredSurveyList : ", response.serviceResponse);
       this.allAnsweredSurveyList.forEach((answeredSurvey:Survey) => {
          let surveyObj = this.allSurveyList.find((survey:Survey) => answeredSurvey.surveyId == survey.surveyId);
          if(surveyObj) surveyObj.isAnswered = true;
        }); 
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  onTakeSurvey(surveyObj:Survey){
    this.isSurveyLoaded = false;
    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [];
    
    this.isSurveyForm = true;
    this.isSurveyList = false;
    
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
        
        this.isSurveyForm = true;
        this.isSurveyList = false;

        setTimeout(()=>{
          let surveyContainer = document.getElementById('surveyContainer');
          surveyContainer.insertAdjacentHTML('afterbegin', surveyTemplate);
          this.isSurveyLoaded = true;
        }, 1000)

      } else {
        console.error(response.serviceResponse);
      }
    });
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
    const form:any = document.getElementById('surveyForm');

    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;
    surveyObj.surveyQuestionList = [];

    this.allSurveyQuestionList.forEach((question, index) => {

      let surveyQuestion = new SurveyQuestion();
      surveyQuestion.surveyQuestionId = question.surveyQuestionId;
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
    this.surveyService.setSurveyResponseByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
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
        let textTemplate: any =`<textarea class="form-control" rows="1" name="question-${qIndex+1}"></textarea>`;
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


  //modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
  
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }
}
