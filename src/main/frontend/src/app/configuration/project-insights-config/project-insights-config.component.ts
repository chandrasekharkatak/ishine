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
import { UtilityService } from 'src/app/services/utility.service';
import { ProjectService } from 'src/app/services/project.service';
import { ProjectInsightQuestion } from 'src/app/models/projectInsightQuestion';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ProjectInsightService } from 'src/app/services/project-insight.service';

@Component({
  selector: 'app-project-insights-config',
  templateUrl: './project-insights-config.component.html',
  styleUrls: ['./project-insights-config.component.css']
})
export class ProjectInsightsConfigComponent implements OnInit {

  feature = "Survey Config";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isQuestionForm:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;

  isProjectInsightList:boolean = false;
  isSurveyResponseList:boolean = false;
  // isSurveyResponseList

  surveyObj:Survey = new Survey();
  allSurveyQuestionList:SurveyQuestion[] = [new SurveyQuestion()];
  projectInsightQuestionList:ProjectInsightQuestion[] = [new ProjectInsightQuestion()]
  allProjectList:any[] = [];
  projectInsightQuestion:ProjectInsightQuestion = new ProjectInsightQuestion();
  allProjectInsightCreatedList:any[] = [];

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

  employeesFor360: any[] = [];

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private surveyService : SurveyService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private clipboardService: ClipboardService,
    private router: Router,
    private utilityService: UtilityService,
    private projectService: ProjectService,
    private projectInsightService:ProjectInsightService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  async ngOnInit(): Promise<void> {
    try {
      this.employeesFor360 = await this.utilityService.getEmployeeDetailsFor360View();
      // console.log("Priyadarshini ", this.employeesFor360);
    } catch (error) {
      console.error("Error fetching employee details for 360 view", error);
    }
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    // let questionObj = ;
    // questionObj.optionsList.push("");
    // this.allSurveyQuestionList.push(questionObj);
    this.sectionViewInit();

    //console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);
    this.preventBackButton();

    this.getAllProjects();
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
    this.isQuestionForm = true;
    this.isCreation = true;

    this.isUpdation = false;
    this.isProjectInsightList = false;
    this.isSurveyResponseList = false;

    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [new SurveyQuestion()];
  }

  showSurveys(){
    this.isProjectInsightList = true;

    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isSurveyResponseList = false;

    this.getAllSurveys();
  }

  showSurveyResponses(surveyObj:Survey){
    this.isSurveyResponseList = true;

    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isProjectInsightList = false;

    //console.log("surveyObj for responses : ", surveyObj);
    this.getAllSurveyResponsesBySurveyId(surveyObj);

  }

  showSurveyUpdate(surveyObj:Survey, template: TemplateRef<any>){
    this.isQuestionForm = true;
    this.isUpdation = true;

    this.isCreation = false;
    this.isSurveyResponseList = false;
    this.isProjectInsightList = false;

    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [];

    this.surveyService.getAllQuestionsBySurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allSurveyQuestionList = response.serviceResponse;
        //console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);

        this.surveyObj = surveyObj;
        this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
          survey.optionsList = JSON.parse(survey.options);
          survey.required = JSON.parse(survey.required);
        });

        //console.log("For Edit SurveyObj ==> ",this.surveyObj, this.allSurveyQuestionList);

      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  getAllProjects(){
    this.allProjectList = [];

    this.projectService.getAllProjects().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectList = response.serviceResponse;
        this.allProjectList.forEach(project =>{
        project.createdOn = (project.createdOn)? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        project.updatedOn = (project.updatedOn)? moment(project.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        })

        //remove duplicate clients
        this.allProjectList = this.allProjectList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.projectId === value.projectId
          ))
        );
        this.allProjectList = this.allProjectList.sort((a,b)=>a.createdOn-b.createdOn);
        //console.log(this.allProjects, " : this.allProjects");
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getProjectManangerInfo(projectId: any){
    this.allProjectList.forEach((object) => {
      if(object.projectId == projectId){
        this.projectInsightQuestion.projectManagerName = object.employeeName;
        this.projectInsightQuestion.projectManagerId = object.empId;
      }
    });
  }

  // Manage Questions
  addQuestion(mileIndex: any, i: any) {
    this.projectInsightQuestionList[mileIndex].projectQuestion.splice(i + 1, 0, new ProjectQuestion());
  }
  

  removeQuestion(mileIndex,i){
    this.projectInsightQuestionList[mileIndex].projectQuestion.splice(i,1);
  }

  addMilestone(mileIndex){
    this.projectInsightQuestionList.splice(mileIndex+1,0,new ProjectInsightQuestion());
  }

  removeMilestone(mileIndex){ 
    this.projectInsightQuestionList.splice(mileIndex,1);
  }

  // Manage Options
  addOption(mileIndex, i, questionObj:ProjectQuestion){
    let question = this.projectInsightQuestionList[mileIndex].projectQuestion.find(ques => ques == questionObj);
    question.optionsList.splice(i+1,0, new SurveyOption());
  }

  removeOption(mileIndex, i, questionObj:ProjectQuestion){
    let question = this.projectInsightQuestionList[mileIndex].projectQuestion.find(ques => ques == questionObj);
    question.optionsList.splice(i,1);
  }

  setOption(questionObj:ProjectQuestion, mileIndex:any, index:any){
    if(questionObj.optionType == "checkbox" || questionObj.optionType == "radio"){
      let question = this.projectInsightQuestionList[mileIndex].projectQuestion.find(ques => ques == questionObj);
      question.optionsList = [];
      question.optionsList.splice(1,0,new SurveyOption());
    }
  }


  onPreiew(previewTemplate: TemplateRef<any>, template: TemplateRef<any>){
    let inputValidated: boolean = this.vallidateProjectInsight(template, this.projectInsightQuestion, this.projectInsightQuestionList);
    if (!inputValidated) return;

    const questionTemplate:string = this.createTemplate();

    let previewObj = new ProjectInsightQuestion();
    previewObj.projectId = this.projectInsightQuestion.projectId;
    previewObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
    previewObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
    previewObj.projectInsightQuestionList = this.projectInsightQuestionList;
    previewObj.projectInsightQuestionTemplate = questionTemplate;

    //console.log("previewObj : ", previewObj);
    this.openSurveyPreviewMod(previewTemplate,previewObj);
  }

  vallidateProjectInsight(template: TemplateRef<any>, projectInsightQuestion:ProjectInsightQuestion, projectInsightQuestionList:ProjectInsightQuestion[]){
    if(!this.validationService.validateNullUndefinedEmptyString(projectInsightQuestion.projectId)){
      this.alertMessage = "Please select Project name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if(!this.validationService.validateNullUndefinedEmptyString(projectInsightQuestion.projectManagerName)){
      this.alertMessage = "Please select Project Manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    let flag = true;
    projectInsightQuestionList.forEach((milestone:ProjectInsightQuestion, mileIndex) => {
      if(!this.validationService.validateNullUndefinedEmptyString(milestone.milestone)){
        this.alertMessage = `Please enter milestone ${mileIndex+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return false;
      }

      milestone.projectQuestion.forEach((question:ProjectQuestion, index) => {
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
    });

    return flag;
  }

  onSubmit(template: TemplateRef<any>){

    let inputValidated: boolean = this.vallidateProjectInsight(template, this.projectInsightQuestion, this.projectInsightQuestionList);
    if (!inputValidated) return;

    const questionTemplate:string = this.createTemplate();

    let projObj = new ProjectInsightQuestion();
    projObj.projectId = this.projectInsightQuestion.projectId;
    projObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
    projObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
    projObj.projectInsightQuestionList = this.projectInsightQuestionList;
    projObj.projectInsightQuestionTemplate = questionTemplate;
    projObj.createdBy = this.currentUser.empId;

    projObj.projectInsightQuestionList.forEach((proj:ProjectQuestion) => {
      proj.options = JSON.stringify(proj.optionsList);
    });

    //console.log("survey : ", surveyObj);
    this.projectInsightService.createProjectInsightQuestion(projObj).pipe(first()).subscribe((response: any) => {
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
       this.allSurveyList.forEach((employee) => {
        // console.log("employee.createdBy ", employee.createdBy);
        let matchingEmployee3 = this.employeesFor360.find(emp => emp.empId === employee.createdBy);
        // console.log("createdby ", matchingEmployee3);
        employee.emp360CreatedBy = matchingEmployee3 ? matchingEmployee3 : {};
        // console.log("employee.updatedBy ", employee.updatedBy);
        let matchingEmployee4 = this.employeesFor360.find(emp => emp.empId === employee.updatedBy);
        // console.log("updatedBy ", matchingEmployee4);
        employee.emp360UpdatedBy = matchingEmployee4 ? matchingEmployee4 : {};
      });
      //  console.log("this.allSurveyList : ", this.allSurveyList);
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  onSurveyPreview(surveyObj:Survey, template: TemplateRef<any>){

    //console.log("For Preiew Survey : ", surveyObj);
    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [];

    this.surveyService.getAllQuestionsBySurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allSurveyQuestionList = response.serviceResponse;
        //console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);

        this.surveyObj = surveyObj;
        this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
          survey.optionsList = JSON.parse(survey.options);
          survey.required = JSON.parse(survey.required);
        });

        const questionTemplate: string = this.createTemplate();

        let previewObj = new ProjectInsightQuestion();
        previewObj.projectId = this.projectInsightQuestion.projectId;
        previewObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
        previewObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
        previewObj.projectInsightQuestionList = this.projectInsightQuestionList;
        previewObj.projectInsightQuestionTemplate = questionTemplate;

        //console.log("previewObj : ", previewObj);
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
      //console.log("questionsList : ", questionsList);
      questionsList.forEach((question:SurveyQuestion, index) => {
        this.responseListTableHeaders.push(question.question);
        // this.responseListTableHeaders.push(`Question ${index+1}`, `Answer ${index+1}`);
      });
    } else {
      console.error(questionResponse.serviceResponse);
    }

    //console.log("Final responseListTableHeaders : ", this.responseListTableHeaders);


    const response: any = await this.surveyService.getSurveyAllResponsesBySurveyId(surveyObj).toPromise();
    if (response.serviceStatus == "Success") {
      responseList = response.serviceResponse;
      //console.log("responseList : ", responseList);
      const key = "employeementId"
      let employees = [...new Map(responseList.map((response:SurveyQuestion) => [response[key], response])).values()].map((response:SurveyQuestion) => {
        return ["A-" + response.employeementId, response.name]
        // return {
        //   name: response.name,
        //   employeementId : response.employeementId
        // }
      });

      //console.log("employees : ", employees);

      employees.forEach(employee => {
        let employeeResponse:any[] = responseList.filter((response:SurveyQuestion) => "A-"+ response.employeementId == employee[0]);

        this.responseListTableHeaders.forEach(header => {
          const surveyResponse = employeeResponse.find((response:SurveyQuestion) => response.question == header);
          if(surveyResponse) employee.push(surveyResponse.response);
        });
      });

      //console.log("employees with responses : ", employees);
      this.allSurveyResponseList = employees;
    } else {
      console.error(response.serviceResponse);
    }


  }

  onUpdate(template: TemplateRef<any>){
    let inputValidated: boolean = this.vallidateProjectInsight(template, this.projectInsightQuestion, this.projectInsightQuestionList);
    if (!inputValidated) return;

    let surveyObj = new Survey();
    surveyObj = this.surveyObj;
    surveyObj.surveyQuestionList = this.allSurveyQuestionList;
    surveyObj.updatedBy = this.currentUser.empId;

    surveyObj.surveyQuestionList.forEach((survey:SurveyQuestion) => {
      survey.options = JSON.stringify(survey.optionsList);
    });

    //console.log("updateSurvey : ", surveyObj);
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

    //console.log("Delete Survey : ", this.surveyObj);
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

    //console.log("Activate Survey : ", surveyObj);
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

    //console.log("Complete Survey : ", surveyObj);
    this.surveyService.changeSurveyStatus(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showSurveys();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  createTemplate(): string {
    let projectInsightQueTemplate = ``;

    this.projectInsightQuestionList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
      let finalQuestionTemplate = ``;

      // Milestone Heading
      const milestoneHeader = `
        <div class="row">
          <div class="form-group">
            <h4 class="mb-0">Milestone ${mileIndex + 1}: ${milestone.milestone || ''}</h4>
            <small class="text-secondary">${milestone.description || ''}</small>
          </div>
        </div>`;

      finalQuestionTemplate += milestoneHeader;

      milestone.projectQuestion.forEach((question: ProjectQuestion, qIndex) => {
        let isQuestionRequired = question.required ? `<span class="text-danger">*</span>` : '';
        let questionTemplate = `
          <div class="row">
            <div class="form-group">
              <h5 class="mb-0"><i class="fa-solid fa-q question-icon"></i>.&nbsp; ${question.question || ''} ${isQuestionRequired}</h5>
              <small class="text-secondary">${question.description || ''}</small>
        `;

        // Question Options
        if (question.optionType === "text") {
          questionTemplate += `<textarea class="form-control" rows="1" name="question-${qIndex + 1}"></textarea>`;
        } else if (question.optionType === "checkbox") {
          questionTemplate += question.optionsList.map((option, opIndex) => `
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="q-${qIndex + 1}-check-option-${opIndex + 1}" value="${option.optionValue}" name="question-${qIndex + 1}">
              <label class="form-check-label" for="q-${qIndex + 1}-check-option-${opIndex + 1}">${option.optionValue}</label>
            </div>
          `).join('');
        } else if (question.optionType === "radio") {
          questionTemplate += question.optionsList.map((option, index) => `
            <div class="form-check">
              <input class="form-check-input" type="radio" id="q-${qIndex + 1}-radio-option-${index + 1}" value="${option.optionValue}" name="question-${qIndex + 1}">
              <label class="form-check-label" for="q-${qIndex + 1}-radio-option-${index + 1}">${option.optionValue}</label>
            </div>
          `).join('');
        }

        questionTemplate += `</div></div>`;
        finalQuestionTemplate += questionTemplate;
      });

      projectInsightQueTemplate += finalQuestionTemplate;
    });

    return projectInsightQueTemplate;
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
      //console.log("questionsList : ", questionsList);
      questionsList.forEach((question:SurveyQuestion, index) => {
        headers.push(question.question);
        // headers.push(`Question ${index+1}`, `Answer ${index+1}`);
      });
    } else {
      console.error(questionResponse.serviceResponse);
    }

    //console.log("Final responseListTableHeaders : ", this.responseListTableHeaders);


    const response: any = await this.surveyService.getSurveyAllResponsesBySurveyId(this.surveyObj).toPromise();
    if (response.serviceStatus == "Success") {
      responseList = response.serviceResponse;
      //console.log("responseList : ", responseList);
      const key = "employeementId"
      let employees = [...new Map(responseList.map((response:SurveyQuestion) => [response[key], response])).values()].map((response:SurveyQuestion) => {
        return ["A-".concat(response.employeementId), response.name]
        // return {
        //   name: response.name,
        //   employeementId : response.employeementId
        // }
      });

      //console.log("employees : ", employees);

      employees.forEach(employee => {
        let employeeResponse:any[] = responseList.filter((response:SurveyQuestion) => response.employeementId == employee[0].substring(2));
        this.responseListTableHeaders.forEach(header => {
          const surveyResponse = employeeResponse.find((response:SurveyQuestion) => response.question == header);
          if(surveyResponse) employee.push(surveyResponse.response);
        });
      })

      dataForExcel = employees;
      //console.log("dataForExcel : ", employees);
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
      //console.log("onlySpecificDataArr : ", onlySpecificDataArr);

       this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  }

  copySurveyLinkToClipBoard(survey:any,template: TemplateRef<any>) {
    let url = window.location.href.split("#")[0].concat("#/user-survey/").concat(survey.surveyId);
    //console.log(url, " : url");

    this.clipboardService.copy(url);
    this.openAlertMod(template, "Link copied to clipboard !!");
  }



  //modals
  openSurveyPreviewMod(template: TemplateRef<any>, surveyObj:ProjectInsightQuestion) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    let surveyContainer = document.getElementById("survey-container");
    surveyContainer.insertAdjacentHTML('beforeend', surveyObj.projectInsightQuestionTemplate);
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
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
