import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { SurveyOption } from 'src/app/models/sureyOption';
import { Survey } from 'src/app/models/survey';
import { SurveyQuestion } from 'src/app/models/surveyQuestion';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { SurveyService } from 'src/app/services/survey.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ClipboardService } from 'ngx-clipboard';
import { Router } from '@angular/router';

@Component({
  selector: 'app-survey-config',
  templateUrl: './survey-config.component.html',
  styleUrls: ['./survey-config.component.css']
})
export class SurveyConfigComponent implements OnInit {

  feature = "Survey Config";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isSurveyForm:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;

  isSurveyList:boolean = false;
  isSurveyResponseList:boolean = false;

  surveyObj:Survey = new Survey();
  allSurveyQuestionList:SurveyQuestion[] = [new SurveyQuestion()];

  allSurveyList:any[] = [];
  allSurveyResponseList:any[] = [];
  selectedSurveyName:any = "Test Survey";
  responseListTableHeaders:any[] =
  [
    "Employee Name",
    "Employee ID",
    "Question 1",
    "Answer 1",
    "Question 2",
    "Answer 2",
    "Question 3",
    "Answer 3",
    "Question 4",
    "Answer 4",
    "Question 5",
    "Answer 5",
  ];

  filters:any = {};
  isSearchEnabled:boolean = false;
  surveyColumns:any[] = ['surveyName','description','isActive','createdByName','createdOn'];
  surveyResponseColumns:any[] = ['0','1','2'];



  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private surveyService : SurveyService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private clipboardService: ClipboardService,
    private router: Router
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    // let questionObj = ;
    // questionObj.optionsList.push("");
    // this.allSurveyQuestionList.push(questionObj);
    this.sectionViewInit();

    console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit(){
    this.showSurveys();
  }

  showSurveyForm(){
    this.isSurveyForm = true;
    this.isCreation = true;

    this.isUpdation = false;
    this.isSurveyList = false;
    this.isSurveyResponseList = false;

    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [new SurveyQuestion()];
  }

  showSurveys(){
    this.isSurveyList = true;

    this.isSurveyForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isSurveyResponseList = false;

    this.getAllSurveys();
  }

  showSurveyResponses(surveyObj:Survey){
    this.isSurveyResponseList = true;

    this.isSurveyForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isSurveyList = false;

    console.log("surveyObj for responses : ", surveyObj);
    this.getAllSurveyResponsesBySurveyId(surveyObj);

  }

  showSurveyUpdate(surveyObj:Survey, template: TemplateRef<any>){
    this.isSurveyForm = true;
    this.isUpdation = true;

    this.isCreation = false;
    this.isSurveyResponseList = false;
    this.isSurveyList = false;

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

        console.log("For Edit SurveyObj ==> ",this.surveyObj, this.allSurveyQuestionList);

      } else {
        console.error(response.serviceResponse);
      }
    });

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


  onPreiew(previewTemplate: TemplateRef<any>, template: TemplateRef<any>){
    let inputValidated: boolean = this.vallidateSurvey(template, this.surveyObj, this.allSurveyQuestionList);
    if (!inputValidated) return;

    const surveyTemplate:string = this.createTemplate();

    let previewObj = new Survey();
    previewObj.surveyName = this.surveyObj.surveyName;
    previewObj.description = this.surveyObj.description;
    previewObj.surveyQuestionList = this.allSurveyQuestionList;
    previewObj.surveyTemplate = surveyTemplate;

    console.log("previewObj : ", previewObj);
    this.openSurveyPreviewMod(previewTemplate,previewObj);
  }

  vallidateSurvey(template: TemplateRef<any>, surveyObj:Survey, allSurveyQuestionList:SurveyQuestion[]){
    if(!this.validationService.validateNullUndefinedEmptyString(surveyObj.surveyName)){
      this.alertMessage = "Please enter Survey Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(!this.validationService.validateTeamName(surveyObj.surveyName.trim())){
      this.alertMessage = "Please enter Valid Survey Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let flag = true;
    allSurveyQuestionList.forEach((question:SurveyQuestion, index) => {
      if(!this.validationService.validateNullUndefinedEmptyString(question.question)){
        this.alertMessage = `Please enter Question ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }
      if(!this.validationService.validateTeamName(question.question)){
        this.alertMessage = `Please enter valid Question ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(question.optionType)){
        this.alertMessage = `Please select Option Type ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(question.required)){
        this.alertMessage = `Please select reqiured ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if(question.optionType == "radio" || question.optionType == "checkbox"){
        if (question.optionsList.length < 2) {
          this.alertMessage = `Please provide atleast 2 options for Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          flag = false;
          return;
        }else{
          question.optionsList.forEach((option: SurveyOption, opIndex) => {
            if (!this.validationService.validateNullUndefinedEmptyString(option.optionValue)) {
              this.alertMessage = `Please enter option ${opIndex + 1} for Question ${index + 1} !!`;
              this.openAlertMod(template, this.alertMessage);
              flag = false;
              return;
            }
          });
        }
      }
    });

    return flag;
  }

  onSubmit(template: TemplateRef<any>){

    let inputValidated: boolean = this.vallidateSurvey(template, this.surveyObj, this.allSurveyQuestionList);
    if (!inputValidated) return;

    const surveyTemplate:string = this.createTemplate();

    let surveyObj = new Survey();
    surveyObj.surveyName = this.surveyObj.surveyName;
    surveyObj.description = this.surveyObj.description;
    surveyObj.surveyQuestionList = this.allSurveyQuestionList;
    surveyObj.surveyTemplate = surveyTemplate;
    surveyObj.createdBy = this.currentUser.empId;
    surveyObj.isActive = false;

    surveyObj.surveyQuestionList.forEach((survey:SurveyQuestion) => {
      survey.options = JSON.stringify(survey.optionsList);
    });

    console.log("survey : ", surveyObj);
    this.surveyService.createSurvey(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  spaceTrimOnSurveyName(){
    if(this.surveyObj.surveyName != null || this.surveyObj.surveyName != ''){
      this.surveyObj.surveyName = this.surveyObj.surveyName?.trim();
    }
  }

  getAllSurveys(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.allSurveyList = [];

    this.surveyService.getAllSurveys().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       this.allSurveyList = response.serviceResponse;
       this.allSurveyList.forEach(survey => {
         survey.createdOn = (survey.createdOn)? moment(survey.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
       });
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

  async getAllSurveyResponsesBySurveyId(surveyObj:Survey){
    this.allSurveyResponseList = []
    this.responseListTableHeaders = ["Employee ID", "Employee Name"];
    this.surveyObj = surveyObj;

    let questionsList:any[] = [];
    let responseList:any[] = [];

    const questionResponse: any = await this.surveyService.getAllQuestionsBySurveyId(surveyObj).toPromise();
    if (questionResponse.serviceStatus == "Success") {
      questionsList = questionResponse.serviceResponse;
      console.log("questionsList : ", questionsList);
      questionsList.forEach((question:SurveyQuestion, index) => {
        this.responseListTableHeaders.push(question.question);
        // this.responseListTableHeaders.push(`Question ${index+1}`, `Answer ${index+1}`);
      });
    } else {
      console.error(questionResponse.serviceResponse);
    }

    console.log("Final responseListTableHeaders : ", this.responseListTableHeaders);


    const response: any = await this.surveyService.getSurveyAllResponsesBySurveyId(surveyObj).toPromise();
    if (response.serviceStatus == "Success") {
      responseList = response.serviceResponse;
      console.log("responseList : ", responseList);
      const key = "employeementId"
      let employees = [...new Map(responseList.map((response:SurveyQuestion) => [response[key], response])).values()].map((response:SurveyQuestion) => {
        return ["A-" + response.employeementId, response.name]
        // return {
        //   name: response.name,
        //   employeementId : response.employeementId
        // }
      });

      console.log("employees : ", employees);

      employees.forEach(employee => {
        let employeeResponse:any[] = responseList.filter((response:SurveyQuestion) => "A-"+ response.employeementId == employee[0]);

        this.responseListTableHeaders.forEach(header => {
          const surveyResponse = employeeResponse.find((response:SurveyQuestion) => response.question == header);
          if(surveyResponse) employee.push(surveyResponse.response);
        });
      });

      console.log("employees with responses : ", employees);
      this.allSurveyResponseList = employees;
    } else {
      console.error(response.serviceResponse);
    }


  }

  onUpdate(template: TemplateRef<any>){
    let inputValidated: boolean = this.vallidateSurvey(template, this.surveyObj, this.allSurveyQuestionList);
    if (!inputValidated) return;

    let surveyObj = new Survey();
    surveyObj = this.surveyObj;
    surveyObj.surveyQuestionList = this.allSurveyQuestionList;
    surveyObj.updatedBy = this.currentUser.empId;

    surveyObj.surveyQuestionList.forEach((survey:SurveyQuestion) => {
      survey.options = JSON.stringify(survey.optionsList);
    });

    console.log("updateSurvey : ", surveyObj);
    this.surveyService.updateSurvey(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDelete(template: TemplateRef<any>){
    this.cancelRequest();

    console.log("Delete Survey : ", this.surveyObj);
    this.surveyService.deleteSurvey(this.surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onActivate(template: TemplateRef<any>){
    this.cancelRequest();

    let surveyObj = new Survey();
    surveyObj.surveyId = this.surveyObj.surveyId;
    surveyObj.updatedBy = this.currentUser.empId;
    surveyObj.isActive = true;

    console.log("Activate Survey : ", surveyObj);
    this.surveyService.changeSurveyStatus(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onComplete(template: TemplateRef<any>){
    this.cancelRequest();

    let surveyObj = new Survey();
    surveyObj.surveyId = this.surveyObj.surveyId;
    surveyObj.updatedBy = this.currentUser.empId;
    surveyObj.isActive = "Completed";

    console.log("Complete Survey : ", surveyObj);
    this.surveyService.changeSurveyStatus(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
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

   console.log("surveyTemplate : ", surveyTemplate);
   return surveyTemplate;
  }

  name = 'EmployeeSheet.xlsx';
  async exportToExcel(): Promise<void> {

    let headers:any[] = ["Employee ID", "Employee Name"];
    let questionsList:any[] = [];
    let responseList:any[] = [];
    let dataForExcel:any[] = [];

    const questionResponse: any = await this.surveyService.getAllQuestionsBySurveyId(this.surveyObj).toPromise();
    if (questionResponse.serviceStatus == "Success") {
      questionsList = questionResponse.serviceResponse;
      console.log("questionsList : ", questionsList);
      questionsList.forEach((question:SurveyQuestion, index) => {
        headers.push(question.question);
        // headers.push(`Question ${index+1}`, `Answer ${index+1}`);
      });
    } else {
      console.error(questionResponse.serviceResponse);
    }

    console.log("Final responseListTableHeaders : ", this.responseListTableHeaders);


    const response: any = await this.surveyService.getSurveyAllResponsesBySurveyId(this.surveyObj).toPromise();
    if (response.serviceStatus == "Success") {
      responseList = response.serviceResponse;
      console.log("responseList : ", responseList);
      const key = "employeementId"
      let employees = [...new Map(responseList.map((response:SurveyQuestion) => [response[key], response])).values()].map((response:SurveyQuestion) => {
        return ["A-".concat(response.employeementId), response.name]
        // return {
        //   name: response.name,
        //   employeementId : response.employeementId
        // }
      });

      console.log("employees : ", employees);

      employees.forEach(employee => {
        let employeeResponse:any[] = responseList.filter((response:SurveyQuestion) => response.employeementId == employee[0].substring(2));
        this.responseListTableHeaders.forEach(header => {
          const surveyResponse = employeeResponse.find((response:SurveyQuestion) => response.question == header);
          if(surveyResponse) employee.push(surveyResponse.response);
        });
      })

      dataForExcel = employees;
      console.log("dataForExcel : ", employees);
    } else {
      console.error(response.serviceResponse);
    }

      const onlySpecificDataArr = dataForExcel.map(response => {
        let data = {};
        headers.forEach((header, index) => {
          data[header] = response[index]
        });

        return data;
      });
      console.log("onlySpecificDataArr : ", onlySpecificDataArr);

       this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }

  copySurveyLinkToClipBoard(survey:any,template: TemplateRef<any>) {
    let url = window.location.href.split("#")[0].concat("#/user-survey/").concat(survey.surveyId);
    console.log(url, " : url");

    this.clipboardService.copy(url);
    this.openAlertMod(template, "Link copied to clipboard !!");
  }



  //modals
  openSurveyPreviewMod(template: TemplateRef<any>, surveyObj:Survey) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    let surveyContainer = document.getElementById("survey-container");
    surveyContainer.insertAdjacentHTML('beforeend', surveyObj.surveyTemplate);
  }

  openDeleteSurveyMod(template: TemplateRef<any>, surveyObj:Survey) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.surveyObj = surveyObj;
  }

  openActivateSurveyMod(template: TemplateRef<any>, surveyObj:Survey) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.surveyObj = surveyObj;
  }

  openCompleteSurveyMod(template: TemplateRef<any>, surveyObj:Survey) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.surveyObj = surveyObj;
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
  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  sortData(sort: Sort){
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }

}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
