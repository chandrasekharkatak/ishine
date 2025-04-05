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
import { EmployeeService } from 'src/app/services/employee.service';
import { Document } from 'src/app/models/document';
import { ProjectResponse } from 'src/app/models/projectResponse';

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
  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();
  
  isQuestionForm:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;

  isProjectInsightList:boolean = false;
  isSurveyResponseList:boolean = false;
  isProjectInsightResponseList:boolean = false;
  // isSurveyResponseList
  isResponsePreview:boolean = true;

  surveyObj:Survey = new Survey();
  allSurveyQuestionList:SurveyQuestion[] = [new SurveyQuestion()];
  projectInsightQuestionList:ProjectInsightQuestion[] = [new ProjectInsightQuestion()];
  projectInsightResponseList:ProjectInsightQuestion[] = [new ProjectInsightQuestion()];
  employeeList:any[] = [];

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
  isFinalResponseSubmitted:boolean = false;

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
    private projectInsightService:ProjectInsightService,
    private employeeService: EmployeeService,
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
    this.getEmployeeList();
  }

  isProjectInsightTabClick(){
    this.projectInsightTabClick = true;

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
        this.projectInsightQuestion.projectName = object.projectName;
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

  removeModule(mileIndex:any, modIndex:any){
    this.projectInsightQuestionList[mileIndex].moduleList.splice(modIndex);
  }

  removeSubModule(mileIndex:any, modIndex:any, subModIndex:any){
    this.projectInsightQuestionList[mileIndex].moduleList[modIndex].subModuleList.splice(subModIndex);
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

  onCheckboxChange(event: any, value: string, question: any,option:any,response:any) {
    if (response.responseList == null || response.responseList == undefined) {
      response.responseList = []; 
    }
  
    if (event.target.checked) {
      option.isChecked = true;
      response.responseList.push(value);
    } else {
      response.responseList = response.responseList.filter((item: string) => item != value);
      option.isChecked = false;
    }
  }

  isInArray(value: string, array: any[]): boolean {
    return Array.isArray(array) && array.includes(value);
  }
  
  getAllProjectInsightResponsesByProjectId(projectObj: any,alertTemplate:TemplateRef<any>,insightResponseTemplate: TemplateRef<any>,isPreview:any) {
    this.isResponsePreview = isPreview;
    this.projectInsightResponseList = [];
    this.isFinalResponseSubmitted = false;
    let projObj = new ProjectInsightQuestion();
    projObj.empId = this.currentUser.empId;
    projObj.projectId = projectObj.projectId;
    projObj.projectManagerId = projectObj.projectManagerId;
    projObj.projectManagerName = projectObj.projectManagerName;
    projObj.employeeRole = this.currentUser.employeeRole;

    this.projectInsightService.getAllProjectInsightResponsesByProjectId(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsightQuestion = response.serviceResponse;
        this.projectInsightResponseList = this.projectInsightQuestion.projectInsightQuestionList;

        this.projectInsightResponseList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
          milestone.isCollapsed = true;
          if (milestone.projectQuestion != null && milestone.projectQuestion.length != 0) {
            milestone.projectQuestion.forEach((question: ProjectQuestion, index) => {
              if (question.projectResponseList != null && question.projectResponseList.length != 0) {
                question.projectResponseList.forEach((response: ProjectResponse, index) => {
                  response.optionsList = JSON.parse(response.options);
                  if (question.optionType == 'checkbox') {
                    response.responseList = JSON.parse(response.response || '[]');
                    if (response.optionsList?.length) {
                      response.optionsList.forEach((option, index) => {
                        if (response?.responseList.includes(option?.optionValue)) {
                          option.isChecked = true
                        }
                      });
                    }
                  }
                  if(response.isDraft == 'Y'){
                    this.isFinalResponseSubmitted = false;
                  }
                });
              } else {
                let projectResponse= new ProjectResponse();
                projectResponse.optionsList = JSON.parse(question.options);
                if (question.optionType == 'checkbox') {
                  projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                }
                question.projectResponseList.push(projectResponse);
              }
            });
          }

          if (milestone.moduleList != null && milestone.moduleList.length != 0) {
            milestone.moduleList.forEach((module: any, modIndex) => {
              if (module.projectQuestion != null && module.projectQuestion.length != 0) {
                module.projectQuestion.forEach((question: ProjectQuestion, index) => {
                  if (question.projectResponseList != null && question.projectResponseList.length != 0) {
                    question.projectResponseList.forEach((response: ProjectResponse, index) => {
                      response.optionsList = JSON.parse(response.options);
                      if (question.optionType == 'checkbox') {
                        response.responseList = JSON.parse(response.response || '[]');
                        if (response.optionsList?.length) {
                          response.optionsList.forEach((option, index) => {
                            if (response?.responseList.includes(option?.optionValue)) {
                              option.isChecked = true
                            }
                          });
                        }
                      }
                      if(response.isDraft == 'Y'){
                        this.isFinalResponseSubmitted = false;
                      }
                    });
                  } else {
                    let projectResponse= new ProjectResponse();
                    projectResponse.optionsList = JSON.parse(question.options);
                    if (question.optionType == 'checkbox') {
                      projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                    }
                    question.projectResponseList.push(projectResponse);
                  }
                });
              }

              if (module.subModuleList != null && module.subModuleList.length != 0) {
                module.subModuleList.forEach((submodule: any, submodIndex) => {
                  if (submodule.projectQuestion != null && submodule.projectQuestion.length != 0) {
                    submodule.projectQuestion.forEach((question: ProjectQuestion, index) => {
                      if (question.projectResponseList != null && question.projectResponseList.length != 0) {
                        question.projectResponseList.forEach((response: ProjectResponse, index) => {
                          response.optionsList = JSON.parse(response.options);
                          if (question.optionType == 'checkbox') {
                            response.responseList = JSON.parse(response.response || '[]');
                            if (response.optionsList?.length) {
                              response.optionsList.forEach((option, index) => {
                                if (response?.responseList.includes(option?.optionValue)) {
                                  option.isChecked = true
                                }
                              });
                            }
                          }
                          if(response.isDraft == 'Y'){
                            this.isFinalResponseSubmitted = false;
                          }
                        });
                      } else {
                        let projectResponse= new ProjectResponse();
                        projectResponse.optionsList = JSON.parse(question.options);
                        if (question.optionType == 'checkbox') {
                          projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                        }
                        question.projectResponseList.push(projectResponse);
                      }
                    });
                  }
                });
              }
            });
          }
        });
        this.openProjectInsightResponeMod(insightResponseTemplate);
      } else {
        this.openAlertMod(alertTemplate, response.serviceResponse);
      }
    });
  }

  saveProjectInsightResponse(template: TemplateRef<any>,finalSubmit:any) {
    // if(finalSubmit){
    //   let inputValidated: boolean = this.validateProjectInsightResponse(template,this.projectInsightResponseList,this.projectInsightQuestion);
    //   if (!inputValidated) return;
    // }
    let files:File[]=[];
    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightResponseList.length; milestoneIndex++) {
      let milestone = this.projectInsightResponseList[milestoneIndex];
      if (milestone.projectQuestion?.length) {
        let questionList = milestone.projectQuestion;
        for (let index = 0; index < questionList.length; index++) {
          let question = questionList[index];
          for(let responseIndex = 0;responseIndex < question.projectResponseList.length; responseIndex++){
            let response = question.projectResponseList[responseIndex];
            response.responseByEmpId = this.currentUser.empId;
            response.isDraft = finalSubmit ? 'N' : 'Y';
            if (question.optionType == 'checkbox') {
              response.response = JSON.stringify(response.responseList);
            }
            if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
              const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
              files.push(renamedFile);
              response.uploadedFile = null;
            }
          }
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];
        if (module.projectQuestion?.length) {
          let moduleQuestionList = module.projectQuestion;
          for (let index = 0; index < moduleQuestionList.length; index++) {
            let question = moduleQuestionList[index];
            for(let responseIndex = 0;responseIndex < question.projectResponseList.length; responseIndex++){
              let response = question.projectResponseList[responseIndex];
              response.responseByEmpId = this.currentUser.empId;
              response.isDraft = finalSubmit ? 'N' : 'Y';
              if (question.optionType == 'checkbox') {
                response.response = JSON.stringify(response.responseList);
              }
              if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
                const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
                files.push(renamedFile);
                response.uploadedFile = null;
              }
            }
          }
        }

        for (let submoduleIndex = 0; submoduleIndex < (module.subModuleList?.length || 0); submoduleIndex++) {
          let submodule = module.subModuleList[submoduleIndex];
          if (submodule.projectQuestion?.length) {
            let submoduleProjectQuestion = submodule.projectQuestion;
            for (let index = 0; index < submodule.projectQuestion.length; index++) {
              let question = submoduleProjectQuestion[index];
              for(let responseIndex = 0;responseIndex < question.projectResponseList.length; responseIndex++){
                let response = question.projectResponseList[responseIndex];
                response.isDraft = finalSubmit ? 'N' : 'Y';
                response.responseByEmpId = this.currentUser.empId;
                if (question.optionType == 'checkbox') {
                  response.response = JSON.stringify(response.responseList);
                }
                if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
                  const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
                  files.push(renamedFile);
                  response.uploadedFile = null;
                }
              }
            }
          }
        }
      }
    }

    let projObj = new ProjectInsightQuestion();
    projObj.projectId = this.projectInsightQuestion.projectId;
    projObj.projectManagerId = this.projectInsightQuestion.projectManagerId;
    projObj.projectManagerName = this.projectInsightQuestion.projectManagerName;
    projObj.projectInsightQuestionList = this.projectInsightResponseList;
    projObj.empId = this.currentUser.empId;
    this.projectInsightService.saveProjectInsightResponse(projObj,files).pipe(first()).subscribe((response: any) => {
      this.closeProjectInsightResponseModal();
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsight();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  validateProjectInsightResponse(template: TemplateRef<any>,projectInsightResponseList: ProjectInsightQuestion[],projectInsightQuestion:any) {
    for (let milestoneIndex = 0; milestoneIndex < projectInsightResponseList.length; milestoneIndex++) {
      let milestone = projectInsightResponseList[milestoneIndex];
  
      if (milestone.projectQuestion?.length) {
        if (!this.validateQuestionAndResponse(milestone.projectQuestion, milestoneIndex, template, 'Milestone')) {
          return false;
        }
      }
  
      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];
  
        if (module.projectQuestion?.length) {
          if (!this.validateQuestionAndResponse(module.projectQuestion, moduleIndex, template, 'Milestone - ' + (milestoneIndex+1) +  ' Module')) {
            return false; 
          }
        }
  
        for (let submoduleIndex = 0; submoduleIndex < (module.subModuleList?.length || 0); submoduleIndex++) {
          let submodule = module.subModuleList[submoduleIndex];
  
          if (submodule.projectQuestion?.length) {
            if (!this.validateQuestionAndResponse(submodule.projectQuestion, submoduleIndex, template, 'Milestone - ' + milestoneIndex+1 +  ' Module - '  + (moduleIndex+1)+ 'Sub-module')) {
              return false; 
            }
          }
        }
      }
    }
    return true; 
  }
  
  validateQuestionAndResponse(questionList: ProjectQuestion[],parentIndex: number,template: TemplateRef<any>,parentType: string) {
    for (let index = 0; index < questionList.length; index++) {
      let question = questionList[index];
  
      if (question.required) {
        let isInvalid =
          question.optionType === 'checkbox'
            ? !Array.isArray(question.responseList) || question.responseList.length === 0
            : this.validationService.validateNullUndefinedEmptyString(question.response) === false;
  
        if (isInvalid) {
          this.alertMessage = `Please provide response for ${parentType}-${parentIndex + 1} Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      }
    }
    return true;
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

      if(proj.projectQuestion != null && proj.projectQuestion.length != 0){
        proj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }

      if(proj.moduleList != null && proj.moduleList.length != 0){
        proj.moduleList.forEach((moduleObj: any) => {

          if(moduleObj.projectQuestion != null && moduleObj.projectQuestion.length != 0){
            moduleObj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
              questionObj.options = JSON.stringify(questionObj.optionsList);
            });
          }

          if(moduleObj.subModuleList != null && moduleObj.subModuleList.length != 0){
            moduleObj.subModuleList.forEach((submoduleObj: any) => {
    
              if(submoduleObj.projectQuestion != null && submoduleObj.projectQuestion.length != 0){
                submoduleObj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
                  questionObj.options = JSON.stringify(questionObj.optionsList);
                });
              }
            });
          }
        });
      }
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

    let insightObj = {
      employeeRole:this.currentUser.employeeRole,
      empId:this.currentUser.empId
    };

    this.projectInsightService.getAllProjectInsightList(insightObj).pipe(first()).subscribe((response: any) => {
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
      projObj.updatedBy = this.currentUser.empId;
  
      projObj.projectInsightQuestionList.forEach((proj:ProjectInsightQuestion) => {

        if(proj.projectQuestion != null && proj.projectQuestion.length != 0){
          proj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
            questionObj.options = JSON.stringify(questionObj.optionsList);
          });
        }
  
        if(proj.moduleList != null && proj.moduleList.length != 0){
          proj.moduleList.forEach((moduleObj: any) => {
  
            if(moduleObj.projectQuestion != null && moduleObj.projectQuestion.length != 0){
              moduleObj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
                questionObj.options = JSON.stringify(questionObj.optionsList);
              });
            }
  
            if(moduleObj.subModuleList != null && moduleObj.subModuleList.length != 0){
              moduleObj.subModuleList.forEach((submoduleObj: any) => {
      
                if(submoduleObj.projectQuestion != null && submoduleObj.projectQuestion.length != 0){
                  submoduleObj.projectQuestion.forEach((questionObj: ProjectQuestion) => {
                    questionObj.options = JSON.stringify(questionObj.optionsList);
                  });
                }
              });
            }
          });
        }
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

  getEmployeeList() {
    this.employeeList = [];
    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeList = response.serviceResponse;
        this.employeeList = this.employeeList.filter(x => x.employmentstatus != 'InActive');
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  onImageSelect(event: any){
  }

// createTemplate(): string {
//   let projectInsightQueTemplate = '';

//   // Iterate through milestones
//   this.projectInsightQuestionList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
//     // Milestone Section
//     projectInsightQueTemplate += `
//       <div class="milestone-section" style="margin-bottom: 30px; background: white; border-radius: 8px; box-shadow: 0 2px 6px rgba(0,0,0,0.1);">
//         <div class="milestone-header" style="background: #3498db; color: white; padding: 15px; border-radius: 8px 8px 0 0;">
//           <h3 style="margin: 0;">Milestone ${mileIndex + 1}: ${milestone.milestone || ''}</h3>
//           <div style="font-size: 14px; margin-top: 5px;">${milestone.description || ''}</div>
//           <div style="font-size: 12px; margin-top: 5px;">Assigned To: ${milestone.assignedTo || ''}</div>
//         </div>
//         <div class="milestone-content" style="padding: 20px;">
//     `;

//     // Milestone Questions
//     if (milestone.projectQuestion && milestone.projectQuestion.length > 0) {
//       projectInsightQueTemplate += this.createQuestionsSection(milestone.projectQuestion, 'Milestone');
//     }

//     // Handle Modules
//     if (milestone.moduleList && milestone.moduleList.length > 0) {
//       milestone.moduleList.forEach((module, modIndex) => {
//         projectInsightQueTemplate += `
//           <div class="module-section" style="margin: 20px 0; background: #f5f6fa; border-radius: 6px; padding: 15px;">
//             <div class="module-header" style="background: #2ecc71; color: white; padding: 10px; border-radius: 6px; margin-bottom: 15px;">
//               <h4 style="margin: 0;">Module: ${module.module || ''}</h4>
//               <div style="font-size: 12px; margin-top: 5px;">Assigned To: ${module.assignedTo || ''}</div>
//             </div>
//         `;

//         // Module Questions
//         if (module.projectQuestion && module.projectQuestion.length > 0) {
//           projectInsightQueTemplate += this.createQuestionsSection(module.projectQuestion, 'Module');
//         }

//         // Handle SubModules
//         if (module.subModuleList && module.subModuleList.length > 0) {
//           module.subModuleList.forEach((subModule, subModIndex) => {
//             projectInsightQueTemplate += `
//               <div class="submodule-section" style="margin: 15px 0; background: white; border-radius: 6px; padding: 15px;">
//                 <div class="submodule-header" style="background: #9b59b6; color: white; padding: 8px; border-radius: 6px; margin-bottom: 15px;">
//                   <h5 style="margin: 0;">Sub-Module</h5>
//                   <div style="font-size: 12px; margin-top: 5px;">Assigned To: ${subModule.assignedTo || ''}</div>
//                 </div>
//             `;

//             // SubModule Questions
//             if (subModule.projectQuestion && subModule.projectQuestion.length > 0) {
//               projectInsightQueTemplate += this.createQuestionsSection(subModule.projectQuestion, 'SubModule');
//             }

//             projectInsightQueTemplate += `</div>`; // Close submodule-section
//           });
//         }

//         projectInsightQueTemplate += `</div>`; // Close module-section
//       });
//     }

//     projectInsightQueTemplate += `
//         </div>
//       </div>
//     `; // Close milestone-content and milestone-section
//   });

//   return projectInsightQueTemplate;
// }

// private createQuestionsSection(questions: any[], entityType: string): string {
//   let questionsTemplate = '';
  
//   questions.forEach((question, qIndex) => {
//     const isRequired = question.required === 'true' ? `<span style="color: #e74c3c; margin-left: 5px;">*</span>` : '';
//     const hasDocument = question.documentUpload === 'true' ? 
//       `<div style="margin-top: 10px;">
//         <input type="file" accept="image/*" class="form-control-file" style="font-size: 14px;">
//        </div>` : '';
    
//     questionsTemplate += `
//       <div class="question-container" style="background: white; padding: 15px; margin: 10px 0; border-radius: 6px; border: 1px solid #e0e0e0;">
//         <div class="question-header" style="margin-bottom: 10px;">
//           <h5 style="color: #34495e; margin: 0;">Q${qIndex + 1}. ${question.question}${isRequired}</h5>
//           ${question.description ? `<small style="color: #7f8c8d;">${question.description}</small>` : ''}
//         </div>
//     `;

//     // Handle different question types
//     if (question.optionType === "text") {
//       questionsTemplate += `
//         <input type="text" class="form-control" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;"
//                value="${question.response || ''}" placeholder="Enter your answer">
//       `;
//     } else if (question.optionType === "checkbox" || question.optionType === "radio") {
//       const options = JSON.parse(question.options || '[]');
//       options.forEach((option: any, optIndex: number) => {
//         const isChecked = question.response?.includes(option.optionValue) ? 'checked' : '';
//         questionsTemplate += `
//           <div class="option-container" style="margin: 8px 0;">
//             <input type="${question.optionType}" 
//                    id="${entityType}-${qIndex}-${question.optionType}-${optIndex}"
//                    name="${entityType}-question-${qIndex}"
//                    value="${option.optionValue}"
//                    ${isChecked}
//                    style="margin-right: 8px;">
//             <label for="${entityType}-${qIndex}-${question.optionType}-${optIndex}"
//                    style="color: #2c3e50; font-size: 14px;">
//               ${option.optionValue}
//             </label>
//           </div>
//         `;
//       });
//     }

//     questionsTemplate += `
//         ${hasDocument}
//       </div>
//     `;
//   });

//   return questionsTemplate;
// }


createTemplate(): string {
  let projectInsightQueTemplate = `
    <style>
      .milestone-section { transition: all 0.3s ease; }
      .milestone-section:hover { transform: translateY(-2px); }
      .question-container:hover { border-color: #3498db !important; }
      .custom-input:focus { box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2); }
      .custom-checkbox, .custom-radio { cursor: pointer; }
      .custom-checkbox:hover, .custom-radio:hover { background-color: #f8f9fa; }
      .file-upload { transition: all 0.3s ease; }
      .file-upload:hover { background-color: #f8f9fa; }
    </style>
  `;

  // Iterate through milestones
  this.projectInsightQuestionList.forEach((milestone: ProjectInsightQuestion, mileIndex) => {
    projectInsightQueTemplate += `
      <div class="milestone-section" style="
        margin-bottom: 30px;
        background: white;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.08);
        overflow: hidden;
        border: 1px solid #e1e8ed;
      ">
        <div class="milestone-header" style="
          background: linear-gradient(135deg, #3498db, #2980b9);
          color: white;
          padding: 20px;
          position: relative;
        ">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <h3 style="
              margin: 0;
              font-size: 1.5rem;
              font-weight: 600;
              letter-spacing: 0.5px;
            ">Milestone ${mileIndex + 1}: ${milestone.milestone || ''}</h3>
            <span style="
              background: rgba(255,255,255,0.2);
              padding: 4px 12px;
              border-radius: 20px;
              font-size: 0.8rem;
            ">Phase ${mileIndex + 1}</span>
          </div>
          <div style="margin-top: 10px; opacity: 0.9;">${milestone.description || ''}</div>
          <div style="
            margin-top: 15px;
            display: flex;
            align-items: center;
            gap: 8px;
          ">
            <svg style="width: 16px; height: 16px;" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
            </svg>
            <span style="font-size: 0.9rem;">Assigned To: ${milestone.assignedTo || ''}</span>
          </div>
        </div>
        <div class="milestone-content" style="padding: 25px;">
    `;

    // Milestone Questions
    if (milestone.projectQuestion && milestone.projectQuestion.length > 0) {
      projectInsightQueTemplate += this.createQuestionsSection(milestone.projectQuestion, 'Milestone');
    }

    // Handle Modules with enhanced styling
    if (milestone.moduleList && milestone.moduleList.length > 0) {
      milestone.moduleList.forEach((module, modIndex) => {
        projectInsightQueTemplate += `
          <div class="module-section" style="
            margin: 20px 0;
            background: #f8fafc;
            border-radius: 8px;
            padding: 20px;
            border: 1px solid #e2e8f0;
          ">
            <div class="module-header" style="
              background: linear-gradient(135deg, #2ecc71, #27ae60);
              color: white;
              padding: 15px;
              border-radius: 8px;
              margin-bottom: 20px;
              box-shadow: 0 2px 8px rgba(46, 204, 113, 0.2);
            ">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <h4 style="margin: 0; font-weight: 500;">Module: ${module.module || ''}</h4>
                <span style="
                  background: rgba(255,255,255,0.2);
                  padding: 3px 10px;
                  border-radius: 15px;
                  font-size: 0.8rem;
                ">Module ${modIndex + 1}</span>
              </div>
              <div style="
                margin-top: 10px;
                display: flex;
                align-items: center;
                gap: 8px;
                font-size: 0.9rem;
              ">
                <svg style="width: 14px; height: 14px;" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
                Assigned To: ${module.assignedTo || ''}
              </div>
            </div>
        `;

        // Module Questions
        if (module.projectQuestion && module.projectQuestion.length > 0) {
          projectInsightQueTemplate += this.createQuestionsSection(module.projectQuestion, 'Module');
        }

        // Handle SubModules with enhanced styling
        if (module.subModuleList && module.subModuleList.length > 0) {
          module.subModuleList.forEach((subModule, subModIndex) => {
            projectInsightQueTemplate += `
              <div class="submodule-section" style="
                margin: 15px 0;
                background: white;
                border-radius: 8px;
                padding: 20px;
                border: 1px solid #edf2f7;
              ">
                <div class="submodule-header" style="
                  background: linear-gradient(135deg, #9b59b6, #8e44ad);
                  color: white;
                  padding: 12px 16px;
                  border-radius: 6px;
                  margin-bottom: 15px;
                  box-shadow: 0 2px 8px rgba(155, 89, 182, 0.2);
                ">
                  <div style="display: flex; justify-content: space-between; align-items: center;">
                    <h5 style="margin: 0; font-weight: 500;">Sub-Module ${subModIndex + 1}</h5>
                    <span style="
                      background: rgba(255,255,255,0.2);
                      padding: 2px 8px;
                      border-radius: 12px;
                      font-size: 0.75rem;
                    ">Sub-Phase</span>
                  </div>
                  <div style="
                    margin-top: 8px;
                    display: flex;
                    align-items: center;
                    gap: 6px;
                    font-size: 0.85rem;
                  ">
                    <svg style="width: 12px; height: 12px;" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                    Assigned To: ${subModule.assignedTo || ''}
                  </div>
                </div>
            `;

            // SubModule Questions
            if (subModule.projectQuestion && subModule.projectQuestion.length > 0) {
              projectInsightQueTemplate += this.createQuestionsSection(subModule.projectQuestion, 'SubModule');
            }

            projectInsightQueTemplate += `</div>`;
          });
        }

        projectInsightQueTemplate += `</div>`;
      });
    }

    projectInsightQueTemplate += `
        </div>
      </div>
    `;
  });

  return projectInsightQueTemplate;
}

private createQuestionsSection(questions: any[], entityType: string): string {
  let questionsTemplate = '';
  
  questions.forEach((question, qIndex) => {
    const isRequired = question.required === 'true' 
      ? `<span style="color: #e74c3c; margin-left: 5px; font-size: 1.2em;">*</span>` 
      : '';
    const hasDocument = question.documentUpload === 'true' 
      ? `<div class="file-upload" style="
          margin-top: 12px;
          padding: 12px;
          border: 2px dashed #cbd5e0;
          border-radius: 6px;
          text-align: center;
        ">
          <label for="file-${entityType}-${qIndex}" style="
            display: block;
            cursor: pointer;
            color: #4a5568;
          ">
            <svg style="width: 24px; height: 24px; margin-bottom: 8px;" viewBox="0 0 24 24" fill="#4a5568">
              <path d="M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"/>
            </svg>
            <span style="display: block; font-size: 0.9rem;">Click to upload or drag and drop</span>
            <span style="display: block; font-size: 0.8rem; color: #718096;">Supported formats: Images</span>
          </label>
          <input 
            id="file-${entityType}-${qIndex}"
            type="file" 
            accept="image/*" 
            style="display: none;"
            class="form-control-file">
        </div>` 
      : '';
    
    questionsTemplate += `
      <div class="question-container" style="
        background: white;
        padding: 20px;
        margin: 15px 0;
        border-radius: 8px;
        border: 1px solid #e2e8f0;
        transition: all 0.3s ease;
      ">
        <div class="question-header" style="margin-bottom: 15px;">
          <div style="display: flex; align-items: center; gap: 8px;">
            <span style="
              background: #3498db;
              color: white;
              width: 24px;
              height: 24px;
              border-radius: 12px;
              display: flex;
              align-items: center;
              justify-content: center;
              font-size: 0.8rem;
              font-weight: 500;
            ">${qIndex + 1}</span>
            <h5 style="
              color: #2d3748;
              margin: 0;
              font-weight: 500;
              font-size: 1.1rem;
            ">${question.question}${isRequired}</h5>
          </div>
          ${question.description ? 
            `<div style="
              color: #718096;
              margin-top: 8px;
              font-size: 0.9rem;
              padding-left: 32px;
            ">${question.description}</div>` 
            : ''}
        </div>
    `;

    // Handle different question types with enhanced styling
    if (question.optionType === "text") {
      questionsTemplate += `
        <div style="padding-left: 32px;">
          <input 
            type="text" 
            class="form-control custom-input" 
            style="
              width: 100%;
              padding: 10px 12px;
              border: 1px solid #e2e8f0;
              border-radius: 6px;
              font-size: 0.95rem;
              transition: all 0.3s ease;
              outline: none;
            "
            value="${question.response || ''}" 
            placeholder="Enter your answer">
        </div>
      `;
    } else if (question.optionType === "checkbox" || question.optionType === "radio") {
      const options = JSON.parse(question.options || '[]');
      questionsTemplate += `<div style="padding-left: 32px;">`;
      options.forEach((option: any, optIndex: number) => {
        const isChecked = question.response?.includes(option.optionValue) ? 'checked' : '';
        questionsTemplate += `
          <div class="custom-${question.optionType}" style="
            margin: 10px 0;
            padding: 10px;
            border-radius: 6px;
            transition: all 0.2s ease;
          ">
            <label style="
              display: flex;
              align-items: center;
              gap: 10px;
              margin: 0;
              cursor: pointer;
            ">
              <input 
                type="${question.optionType}"
                id="${entityType}-${qIndex}-${question.optionType}-${optIndex}"
                name="${entityType}-question-${qIndex}"
                value="${option.optionValue}"
                ${isChecked}
                style="
                  width: 18px;
                  height: 18px;
                  cursor: pointer;
                ">
              <span style="
                color: #4a5568;
                font-size: 0.95rem;
              ">${option.optionValue}</span>
            </label>
          </div>
        `;
      });
      questionsTemplate += `</div>`;
    }

    questionsTemplate += `
        ${hasDocument}
      </div>
    `;
  });

  return questionsTemplate;
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
  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl' });
  }

  closeProjectInsightResponseModal(){
    this.projectResponseModalRef.hide();
  }

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

  removeUploadedFile(response:any){
    if(response?.uploadedFile){
      response.uploadedFile = null;
    }
    if(response?.uploadedFileName){
      response.uploadedFileName = null;
    }
  }

  onQuestionFileChange(event: any, question: any, alertTemplate: TemplateRef<any>, previewElementId: any, documentPreviewTemplate: TemplateRef<any>,response:any) {
    const file = event.target.files[0];
    if (file) {
      response.uploadedFile = file;
      const inputId = event.target.id;
      var fileExtension = file.name.split('.').pop().toLowerCase();
      response.uploadedFileName = inputId + '.' + fileExtension;
    }

    const MAX_SIZE = 5 * 1024 * 1024; 
    if (file) {
      if (file.size > MAX_SIZE) {
        this.alertMessage = "File size must be lesser than or equal to 1MB."
        this.openAlertMod(alertTemplate, this.alertMessage);
        response.uploadedFile = null;
        response.uploadedFileName = null;
        return false;
      }
    }
    this.previewUploadedFile(question, response.uploadedFile, response.uploadedFileName, previewElementId, documentPreviewTemplate, alertTemplate, false,response);
  }

  previewUploadedFile(question,uploadedFile: any, fileName: any, previewElementId: any, documentPreviewTemplate: TemplateRef<any>,alertTemplate: TemplateRef<any>, downloadFile:any,response:any) {
    
    if ((fileName != undefined && fileName != null)) {
      const MAX_SIZE = 5 * 1024 * 1024; //5 MB // 200KB in bytes
      const file = uploadedFile;

      if (uploadedFile != undefined && uploadedFile != null) {
        if (file.size > MAX_SIZE) {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'>File size must be lesser than or equal to 5MB. </span>";
          response.uploadedFile = null;
          response.uploadedFileName = null;
          return false;
        }

        if (file && file.type === 'application/pdf') {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<embed src="${pdfData}" type="application/pdf" width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else if (file && file.type.startsWith('image/')) {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          var fileName2 = file.name;
          var fileExtension = fileName2.split('.').pop().toLowerCase();
          var allowedExtensions = ['jpg', 'jpeg', 'png', 'jpg2'];
          if (allowedExtensions.indexOf(fileExtension) === -1) {
            previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'> Please select only image file (jpg, jpeg, png, jpg2) Or PDF </span>";
            response.uploadedFile = null;
            response.uploadedFileName = null;
            return;
          }
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<img src="${pdfData}" class="img-fluid"  width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else {
          if(downloadFile){
            this.downloadFile(file,fileName);
            return;
          }
        }
      } else {
        this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
        const previewContainer = document.getElementById(previewElementId);
        this.getUserUploadedFileForQuestion(question,fileName, previewContainer,alertTemplate);
      }
    } else {
      this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
      const previewContainer = document.getElementById(previewElementId);
      response.uploadedFile = null;
      response.uploadedFileName = null;
      previewContainer.innerHTML = `<h3 style='padding: 12px; display:inline-block;'>No Data Found.</h3>`;
    }
  }

  downloadFile(file: any, fileName: any) {
    const reader = new FileReader();
    reader.readAsArrayBuffer(file);
    reader.onload = () => {
      const fileBlob = new Blob([reader.result as ArrayBuffer], { type: file.type });
      if (fileBlob) {
        const url = window.URL.createObjectURL(fileBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      }
    }
  }
  
  getUserUploadedFileForQuestion(question:any,fileName:any, previewContainer:any,alertTemplate:TemplateRef<any>){
    let documentObj = new Document();
    documentObj.empId = this.currentUser.empId;
    documentObj.documentName = fileName;
    documentObj.typeId = question.entityId;
    documentObj.typeName = question.entityType;
      this.projectInsightService.getUserUploadedFileForQuestion(documentObj).subscribe((response: any) => {
        response=JSON.parse(response)
        if (response.status === 'Fail') {
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;' > Invalid file format.</span>";
        } else {
          const pdfData = response.fileData;
          if (response.contentType == 'application/pdf') {
            previewContainer.innerHTML = `<embed src="data:application/pdf;base64,${pdfData}" type="application/pdf" width="100%" height="100%"> `
          } else {
            previewContainer.innerHTML = `<img src="data:${response.contentType};base64,${pdfData}" type="${response.contentType}" class="img-fluid" >`;
          }
        }
      });
  }

  closeDocumentPreviewTemplate(){
    this.documentPreviewModalRef.hide();
  }

}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
