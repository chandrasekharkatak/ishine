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
import { ProjectModule } from 'src/app/models/projectModule';
import { ProjectSubModule } from 'src/app/models/projectSubModule';

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

  // tab clicked
  projectInsightTabClick: boolean = false;
  prospectiveProjectTabClick: boolean = false;
  searchTabClick: boolean = false;
  dashboardTabClick: boolean = false;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isQuestionForm:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;

  isProjectInsightList:boolean = false;
  isSurveyResponseList:boolean = false;
  isProjectInsightResponseList:boolean = false;
  // isSurveyResponseList

  surveyObj:Survey = new Survey();
  allSurveyQuestionList:SurveyQuestion[] = [new SurveyQuestion()];
  projectInsightQuestionList:ProjectInsightQuestion[] = [new ProjectInsightQuestion()]
  allProjectList:any[] = [];
  projectInsightQuestion:ProjectInsightQuestion = new ProjectInsightQuestion();

  allProjectInsightList:any[] = [];
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
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit(){
    
  }

  isProjectInsightTabClick(){
    this.showProjectInsight();
    this.getAllProjects();
  }

  showProjectInsightForm(){
    this.isQuestionForm = true;
    this.isCreation = true;

    this.isUpdation = false;
    this.isProjectInsightList = false;
    this.isProjectInsightResponseList = false;

    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [new SurveyQuestion()];
  }

  showProjectInsight(){
    this.isProjectInsightList = true;

    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isProjectInsightResponseList = false;

    this.getAllProjectInsightList();
  }

  showAddProjectInsightResponses(projectObj){
    this.isProjectInsightResponseList = true;

    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isProjectInsightList = false;

    //console.log("surveyObj for responses : ", surveyObj);
    this.getAllProjctInsightResponsesByProjectId(projectObj);
  }

  showProjectInsightUpdate(projectObj:any, template: TemplateRef<any>){
    this.isQuestionForm = true;
    this.isUpdation = true;

    this.isCreation = false;
    this.isProjectInsightResponseList = false;
    this.isProjectInsightList = false;

    this.projectInsightQuestion = new ProjectInsightQuestion();
    this.projectInsightQuestionList = [];

    this.projectInsightService.getAllQuestionsByProjectId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsightQuestion = response.serviceResponse;
        this.projectInsightQuestionList = this.projectInsightQuestion.projectInsightQuestionList;

        this.projectInsightQuestionList.forEach((project: ProjectInsightQuestion) => {
          project.projectQuestion.forEach((question: ProjectQuestion) => {
            question.optionsList = JSON.parse(question.options);
            question.required = JSON.parse(question.required);
            question.documentUpload = JSON.parse(question.documentUpload);
          });
        });
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
  // addQuestion(mileIndex: any, i: any) {
  //   this.projectInsightQuestionList[mileIndex].projectQuestion.splice(i + 1, 0, new ProjectQuestion());
  // }
  

  removeQuestion(mileIndex,i){
    this.projectInsightQuestionList[mileIndex].projectQuestion.splice(i,1);
  }

  // Add button config
  //Milestone
  addMilestone(mileIndex){
    this.projectInsightQuestionList.splice(mileIndex+1,0,new ProjectInsightQuestion());
  }

  addModule(type: any, index:any){
    this.projectInsightQuestionList[index].moduleList.splice(this.projectInsightQuestionList[index].moduleList.length+1,0,new ProjectModule());
  }

  addSubModule(type: any, milIndex:any, modIndex:any){
    this.projectInsightQuestionList[milIndex].moduleList[modIndex].subModuleList.splice(this.projectInsightQuestionList[milIndex].moduleList[modIndex].subModuleList.length+1,0,new ProjectSubModule());
  }

  addQuestion(type: any, mileIndex: any, modIndex?:any, subModIndex?: any){
    if(type == 'milestone'){
      this.projectInsightQuestionList[mileIndex].projectQuestion.splice(this.projectInsightQuestionList[mileIndex].projectQuestion.length + 1, 0, new ProjectQuestion());
    }else if(type == 'module'){
      this.projectInsightQuestionList[mileIndex].moduleList[modIndex].projectQuestion.splice(this.projectInsightQuestionList[mileIndex].moduleList[modIndex].projectQuestion.length + 1, 0, new ProjectQuestion());
    }else if(type == 'submodule'){
      this.projectInsightQuestionList[mileIndex].moduleList[modIndex].subModuleList[subModIndex].projectQuestion.splice(this.projectInsightQuestionList[mileIndex].moduleList[modIndex].subModuleList[subModIndex].projectQuestion.length + 1, 0, new ProjectQuestion());
    }
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

      if(!this.validationService.validateNullUndefinedEmptyString(milestone.assignedTo)){
        this.alertMessage = `Please select assign user in milestone ${mileIndex+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return false;
      }

      if(milestone.projectQuestion != null && milestone.projectQuestion.length != 0){
        flag = this.questionValidation(milestone.projectQuestion, mileIndex, template, 'Milestone');
      }

      if(milestone.moduleList != null && milestone.moduleList.length != 0){
        milestone.moduleList.forEach((module: any, modIndex) => {
          if(!this.validationService.validateNullUndefinedEmptyString(module.module)){
            this.alertMessage = `Please enter module ${modIndex+1} !!`;
            this.openAlertMod(template, this.alertMessage);
            flag = false;
            return false;
          }
    
          if(!this.validationService.validateNullUndefinedEmptyString(module.assignedTo)){
            this.alertMessage = `Please select assign user in module ${modIndex+1} !!`;
            this.openAlertMod(template, this.alertMessage);
            flag = false;
            return false;
          }

          if(module.projectQuestion != null && module.projectQuestion.length != 0){
            flag = this.questionValidation(module.projectQuestion, modIndex, template, 'Module');
          }

          if(module.subModuleList != null && module.subModuleList.length != 0){
            module.subModuleList.forEach((submodule:any, submodIndex) => {
              if(!this.validationService.validateNullUndefinedEmptyString(submodule.subModule)){
                this.alertMessage = `Please enter sub-module ${submodIndex+1} !!`;
                this.openAlertMod(template, this.alertMessage);
                flag = false;
                return false;
              }
        
              if(!this.validationService.validateNullUndefinedEmptyString(submodule.assignedTo)){
                this.alertMessage = `Please select assign user in sub-module ${submodIndex+1} !!`;
                this.openAlertMod(template, this.alertMessage);
                flag = false;
                return false;
              }
    
              if(submodule.projectQuestion != null && submodule.projectQuestion.length != 0){
                flag = this.questionValidation(submodule.projectQuestion, submodIndex, template, 'sub-module');
              }
            });
          }
        }) 
      }
    });

    return flag;
  }

  questionValidation(questionList:ProjectQuestion[], parentIndex:any, template: TemplateRef<any>, parentType: any){
    let flag = true;
    questionList.forEach((question:ProjectQuestion, index) => {
      if(!this.validationService.validateNullUndefinedEmptyString(question.question)){
        this.alertMessage = `Please enter ${parentType}-${parentIndex+1} Question ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(question.optionType)){
        this.alertMessage = `Please select ${parentType}-${parentIndex+1} Option Type ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(question.required)){
        this.alertMessage = `Please select ${parentType}-${parentIndex+1} reqiured ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if(question.optionType == "radio" || question.optionType == "checkbox"){
        if (question.optionsList.length < 2) {
          this.alertMessage = `Please provide atleast 2 options for ${parentType}-${parentIndex+1} Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          flag = false;
          return;
        }else{
          question.optionsList.forEach((option: SurveyOption, opIndex) => {
            if (!this.validationService.validateNullUndefinedEmptyString(option.optionValue)) {
              this.alertMessage = `Please enter option ${opIndex + 1} for ${parentType}-${parentIndex+1} Question ${index + 1} !!`;
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

  getAllProjctInsightResponsesByProjectId(projectObj: any){
    this.projectInsightQuestion = new ProjectInsightQuestion();
    this.projectInsightQuestionList = [];

    this.projectInsightService.getAllQuestionsByProjectId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsightQuestion = response.serviceResponse;
        this.projectInsightQuestionList = this.projectInsightQuestion.projectInsightQuestionList;

        this.projectInsightQuestionList.forEach((project: ProjectInsightQuestion) => {
          project.projectQuestion.forEach((question: ProjectQuestion) => {
            question.optionsList = JSON.parse(question.options);
            question.required = JSON.parse(question.required);
            question.documentUpload = JSON.parse(question.documentUpload);
          });
        });

        const questionTemplate: string = this.createTemplate();

        const formStart = `<form id="projectInsightForm">`
        const formEnd = `</form>`
        const finalQuestionTemplate = formStart + questionTemplate + formEnd;

        let previewObj = new ProjectInsightQuestion();
        previewObj.projectId = this.projectInsightQuestion.projectId;
        previewObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
        previewObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
        previewObj.projectInsightQuestionList = this.projectInsightQuestionList;
        previewObj.projectInsightQuestionTemplate = finalQuestionTemplate;


        let projectInsightContainer = document.getElementById("response-container");
        projectInsightContainer.insertAdjacentHTML('afterbegin', previewObj.projectInsightQuestionTemplate);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  addUpdateResponse(template: TemplateRef<any>){
  //   const form: any = document.getElementById('surveyForm');


  //   let projObj = new ProjectInsightQuestion();
  //   projObj.projectId = this.projectInsightQuestion.projectId;
  //   projObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
  //   projObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
  //   projObj.projectInsightQuestionList = this.projectInsightQuestionList;
  //   projObj.empId = this.currentUser.empId;

  //   this.projectInsightQuestionList.forEach((milestone, mileindex) => {
  //     milestone.projectQuestion.forEach((question, index) => {
  //       let surveyQuestion = new SurveyQuestion();
  //       surveyQuestion.surveyQuestionId = question.surveyQuestionId;
  //       surveyQuestion.required = question.required;

  //       if (question.optionType == 'checkbox') {
  //         let response = [];
  //         form.elements[`question-${index + 1}`]?.forEach((checkboxOption) => {
  //           if (checkboxOption.checked) response.push(checkboxOption.value);
  //         });
  //         surveyQuestion.response = response.join(", ");
  //       } else {
  //         surveyQuestion.response = form.elements[`question-${index + 1}`].value;
  //       }
  //       surveyObj.surveyQuestionList.push(surveyQuestion);
  //     });
  //   });

  //   //console.log("On Survey Submit : ", surveyObj);
  //   let inputValidated: boolean = this.validateSurveyResponse(surveyObj, template);
  //   if (!inputValidated) return;
  //   this.surveyService.setSurveyResponseByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.openAlertMod(template, response.serviceResponse);
  //       this.router.navigate(['/user-survey']);
  //       this.showSurveys();
  //     } else {
  //       this.openAlertMod(template, response.serviceResponse);
  //     }
  //   });
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

    projObj.projectInsightQuestionList.forEach((proj:ProjectInsightQuestion) => {
      proj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
        questionObj.options = JSON.stringify(questionObj.optionsList);
      });
    });

    console.log(projObj, " : final object");
    this.projectInsightService.createProjectInsightQuestion(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsight();
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

  getAllProjectInsightList(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.allProjectInsightList = [];

    this.projectInsightService.getAllProjectInsightList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       this.allProjectInsightList = response.serviceResponse;
       this.allProjectInsightList.forEach(project => {
        project.createdOn = (project.createdOn)? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
       });
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  onProjectInsightPreview(projectObj:any, template: TemplateRef<any>){
    this.projectInsightQuestion = new ProjectInsightQuestion();
    this.projectInsightQuestionList = [];

    this.projectInsightService.getAllQuestionsByProjectId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsightQuestion = response.serviceResponse;
        this.projectInsightQuestionList = this.projectInsightQuestion.projectInsightQuestionList;

        this.projectInsightQuestionList.forEach((project: ProjectInsightQuestion) => {
          project.projectQuestion.forEach((question: ProjectQuestion) => {
            question.optionsList = JSON.parse(question.options);
            question.required = JSON.parse(question.required);
            question.documentUpload = JSON.parse(question.documentUpload);
          });
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

    let projObj = new ProjectInsightQuestion();
      projObj.projectId = this.projectInsightQuestion.projectId;
      projObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
      projObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
      projObj.projectInsightQuestionList = this.projectInsightQuestionList;
      projObj.createdBy = this.currentUser.empId;
  
      projObj.projectInsightQuestionList.forEach((proj:ProjectInsightQuestion) => {
        proj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      });

    //console.log("updateSurvey : ", surveyObj);
    this.projectInsightService.updateProjectInsightQuestion(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsight();
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
        this.showProjectInsight();
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
        this.showProjectInsight();
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
        this.showProjectInsight();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onImageSelect(event: any){
  }

//   createTemplate(): string {
//     let projectInsightQueTemplate = '';
  
//     this.projectInsightQuestionList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
//       let finalQuestionTemplate = '';
  
//       // Milestone Heading with improved styling
//       const milestoneHeader = `
//         <div>
//           <h4 style="font-weight: bold;">Milestone ${mileIndex + 1}: ${milestone.milestone || ''}</h4>
//           <small style="color: gray;">${milestone.description || ''}</small>
//         </div>
//       `;
  
//       finalQuestionTemplate += milestoneHeader;
  
//       let questionsBlock = '<div style="border: 1px solid black; padding: 10px; margin: 10px 14px;">';
  
//       milestone.projectQuestion.forEach((question: ProjectQuestion, qIndex) => {
//         let isQuestionRequired = question.required ? `<span style="color: red;">*</span>` : '';
//         let isUploadDocument = question.documentUpload ? `<input type="file" accept="image/*" style="float: right; font-size: 13px;">` : '';
        
//         let questionTemplate = `
//           <div style="padding: 10px; margin: 10px 0;">
//             <h5 style="font-weight: bold;">${qIndex + 1} - ${question.question || ''} ${isQuestionRequired}</h5>
//             <small style="color: gray; display: block; margin-bottom: 5px;">${question.description || ''}</small>
//             ${isUploadDocument}
//         `;
  
//         // Question Options
//         if (question.optionType === "text") {
//           questionTemplate += `<input style="width: 50%">`;
//         } else if (question.optionType === "checkbox") {
//           questionTemplate += question.optionsList.map((option, opIndex) => `
//             <div style="display: flex; align-items: center; margin-top: 5px;">
//               <input type="checkbox" id="q-${qIndex + 1}-check-option-${opIndex + 1}" value="${option.optionValue}" name="question-${qIndex + 1}" style="margin-right: 5px;">
//               <label for="q-${qIndex + 1}-check-option-${opIndex + 1}">${option.optionValue}</label>
//             </div>
//           `).join('');
//         } else if (question.optionType === "radio") {
//           questionTemplate += question.optionsList.map((option, index) => `
//             <div style="display: flex; align-items: center; margin-top: 5px;">
//               <input type="radio" id="q-${qIndex + 1}-radio-option-${index + 1}" value="${option.optionValue}" name="question-${qIndex + 1}" style="margin-right: 5px;">
//               <label for="q-${qIndex + 1}-radio-option-${index + 1}">${option.optionValue}</label>
//             </div>
//           `).join('');
//         }
  
//         questionTemplate += `</div>`;
//         questionsBlock += questionTemplate;
//       });
  
//       questionsBlock += '</div>'; // Closing the bordered div
//       finalQuestionTemplate += questionsBlock;
//       projectInsightQueTemplate += finalQuestionTemplate;
//     });
  
//     return projectInsightQueTemplate;
// }

  createTemplate(): string {
    let projectInsightQueTemplate = '';

    this.projectInsightQuestionList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
      let finalQuestionTemplate = '';

      // Milestone Heading with improved styling
      const milestoneHeader = `
      <div>
          <h4 style="font-weight: bold;">Milestone ${mileIndex + 1}: ${milestone.milestone || ''}</h4>
          <small style="color: gray;">${milestone.description || ''}</small>
      </div>
      `;

      finalQuestionTemplate += milestoneHeader;

      let questionsBlock = '<div style="border: 1px solid black; padding: 10px; margin: 10px 14px;">';

      milestone.projectQuestion.forEach((question: ProjectQuestion, qIndex) => {
        let isQuestionRequired = question.required ? `<span style="color: red;">*</span>` : '';
        let isUploadDocument = question.documentUpload ? `<input type="file" accept="image/*" style="float: right; font-size: 13px;">` : '';
        let responseValue = question.response ? question.response : '';

        let questionTemplate = `
          <div style="padding: 10px; margin: 10px 0;">
              <h5 style="font-weight: bold;">${qIndex + 1} - ${question.question || ''} ${isQuestionRequired}</h5>
              <small style="color: gray; display: block; margin-bottom: 5px;">${question.description || ''}</small>
              ${isUploadDocument}
          `;

        // Question Options with response pre-filled
        if (question.optionType === "text") {
          questionTemplate += `<input style="width: 50%" type="text" value="${responseValue}">`;
        } else if (question.optionType === "checkbox") {
          questionTemplate += question.optionsList.map((option, opIndex) => {
            const isChecked = question.response?.includes(option.optionValue) ? 'checked' : '';
            return `
                  <div style="display: flex; align-items: center; margin-top: 5px;">
                      <input type="checkbox" id="q-${qIndex + 1}-check-option-${opIndex + 1}" value="${option.optionValue}" name="question-${qIndex + 1}" ${isChecked} style="margin-right: 5px;">
                      <label for="q-${qIndex + 1}-check-option-${opIndex + 1}">${option.optionValue}</label>
                  </div>
                  `;
          }).join('');
        } else if (question.optionType === "radio") {
          questionTemplate += question.optionsList.map((option, index) => {
            const isChecked = question.response === option.optionValue ? 'checked' : '';
            return `
                  <div style="display: flex; align-items: center; margin-top: 5px;">
                      <input type="radio" id="q-${qIndex + 1}-radio-option-${index + 1}" value="${option.optionValue}" name="question-${qIndex + 1}" ${isChecked} style="margin-right: 5px;">
                      <label for="q-${qIndex + 1}-radio-option-${index + 1}">${option.optionValue}</label>
                  </div>
                  `;
          }).join('');
        }

        questionTemplate += `</div>`;
        questionsBlock += questionTemplate;
      });

      questionsBlock += '</div>'; // Closing the bordered div
      finalQuestionTemplate += questionsBlock;
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

  openDeleteProjectInsightMod(template: TemplateRef<any>, surveyObj:Survey) {
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
