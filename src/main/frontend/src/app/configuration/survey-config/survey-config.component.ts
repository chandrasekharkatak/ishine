import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { SurveyOption } from 'src/app/models/sureyOption';
import { Survey } from 'src/app/models/survey';
import { SurveyQuestion } from 'src/app/models/surveyQuestion';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { SurveyService } from 'src/app/services/survey.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-survey-config',
  templateUrl: './survey-config.component.html',
  styleUrls: ['./survey-config.component.css']
})
export class SurveyConfigComponent implements OnInit {

  feature = "Survey Config";
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
    // let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });
    // console.log(this.feature, this.userMapping);
  
    // let questionObj = ;
    // questionObj.optionsList.push("");
    // this.allSurveyQuestionList.push(questionObj);
    this.sectionViewInit();

    console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);
    
  }

  sectionViewInit(){
    this.showSurveyForm();
  }

  showSurveyForm(){
    this.isSurveyForm = true;
    
    this.isSurveyList = false;

    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [new SurveyQuestion()];
  }

  showSurveys(){
    this.isSurveyList = true;
    
    this.isSurveyForm = false;

    this.getAllSurveys();
  }
 
  // Manage Questions
  addQuestion(i){
    this.allSurveyQuestionList.splice(i+1,0,new SurveyQuestion());
  }

  removeQuestion(i){
    this.allSurveyQuestionList.splice(i,1);
  }

  // Manage Options
  addOption(i, questionObj:SurveyQuestion){
    let question = this.allSurveyQuestionList.find(ques => ques == questionObj);
    question.optionsList.splice(i+1,0, new SurveyOption());
  }

  removeOption(i, questionObj:SurveyQuestion){
    let question = this.allSurveyQuestionList.find(ques => ques == questionObj);
    question.optionsList.splice(i,1);
  }

  setOption(questionObj:SurveyQuestion){
    if(questionObj.optionType == "checkbox" || questionObj.optionType == "radio"){
      let question = this.allSurveyQuestionList.find(ques => ques == questionObj);
      question.optionsList = [];
      question.optionsList.splice(1,0,new SurveyOption());
    }
  }


  onPreiew(template: TemplateRef<any>){
    const surveyTemplate:string = this.createTemplate();
    
    let previewObj = new Survey();
    previewObj.surveyName = this.surveyObj.surveyName;
    previewObj.description = this.surveyObj.description;
    previewObj.surveyQuestionList = this.allSurveyQuestionList;
    previewObj.surveyTemplate = surveyTemplate; 
    
    console.log("previewObj : ", previewObj);
    this.openSurveyPreviewMod(template,previewObj);
  }

  onSubmit(template: TemplateRef<any>){
    const surveyTemplate:string = this.createTemplate();

    let surveyObj = new Survey();
    surveyObj.surveyName = this.surveyObj.surveyName;
    surveyObj.description = this.surveyObj.description;
    surveyObj.surveyQuestionList = this.allSurveyQuestionList;
    surveyObj.surveyTemplate = surveyTemplate;
    surveyObj.createdBy = this.currentUser.empId;
    surveyObj.isActive = true;

    surveyObj.surveyQuestionList.forEach((survey:SurveyQuestion) => {
      survey.options = JSON.stringify(survey.optionsList);
    });

    console.log("survey : ", surveyObj);

    this.surveyService.createSurvey(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllSurveys(){
    this.allSurveyList = [];
    
    this.surveyService.getAllSurveys().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       this.allSurveyList = response.serviceResponse;
       console.log("this.allSurveyList : ", this.allSurveyList); 
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  onSurveyPreview(surveyObj:Survey, template: TemplateRef<any>){

    console.log("For Preiew Survey : ", surveyObj);
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

        const surveyTemplate: string = this.createTemplate();

        let previewObj = new Survey();
        previewObj.surveyName = surveyObj.surveyName;
        previewObj.description = surveyObj.description;
        previewObj.surveyQuestionList = this.allSurveyQuestionList;
        previewObj.surveyTemplate = surveyTemplate;

        console.log("previewObj : ", previewObj);
        this.openSurveyPreviewMod(template, previewObj);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  createTemplate():string{
   console.log("this.surveyObj : ", this.surveyObj);
   console.log("this.allSurveyQuestionList : ", this.allSurveyQuestionList);

   let surveyTemplate = ``;

   this.allSurveyQuestionList.forEach((question:SurveyQuestion, qIndex) => {

    let finalQuestionTemplate = ``;
    const questionStartTemplate = `<div class="row"><div class="form-group">`
    const questionEndTemplate = `</div></div>`
    const questionRequiredTemplate = `<span class="text-danger">*</span>`
    let isQuestionRequired = (question.required == true)? questionRequiredTemplate : '';
    let questionTemplate = `<h5 class="mb-0"><i class="fa-solid fa-q question-icon"></i>.&nbsp; ${(question.question !== undefined)? question.question : ''}${isQuestionRequired}</h5><small class="text-secondary">${(question.description !== undefined)? question.description : ''}</small>`

    finalQuestionTemplate = questionStartTemplate + questionTemplate;
    
     if (question.optionType == "text") {
       let textTemplate: any =`<textarea class="form-control" rows="1"></textarea>`;
       finalQuestionTemplate = finalQuestionTemplate + textTemplate;
     } else if (question.optionType == "checkbox") {

      let optionTemplate = '';
      question.optionsList.forEach((option:SurveyOption, opIndex) => {
        let checkboxTemplate: any =
        `
          <div class="form-check">
           <input class="form-check-input" type="checkbox" id="q-${qIndex+1}-check-option-${opIndex+1}" value="${option.optionValue}">
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
         <input class="form-check-input" type="radio" name="q-${qIndex+1}-radio-option" id="q-${qIndex+1}-radio-option-${index+1}" value="${option.optionValue}">
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

   console.log("surveyTemplate : ", surveyTemplate);
   return surveyTemplate;
  }



  //modals
  openSurveyPreviewMod(template: TemplateRef<any>, surveyObj:Survey) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    let surveyContainer = document.getElementById("survey-container");
    surveyContainer.insertAdjacentHTML('beforeend', surveyObj.surveyTemplate);
  }

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
