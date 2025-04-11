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
import { ProjectInsight } from 'src/app/models/projectInsightQuestion';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectModule } from 'src/app/models/projectModule';
import { ProjectSubModule } from 'src/app/models/projectSubModule';
import { EmployeeService } from 'src/app/services/employee.service';
import { Document } from 'src/app/models/document';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { ProjectMilestone } from 'src/app/models/projectMilestone';

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

  //search
  searchTerm: string = '';
  searchResultProjectList:any[] = [];

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
  isProjectInsightResponseList:boolean = false;
  isResponsePreview:boolean = true;
  isSearchEnabled:boolean = false;
  isFinalResponseSubmitted:boolean = false;

  projectInsight:ProjectInsight = new ProjectInsight();
  
  employeeList:any[] = [];
  allProjectList:any[] = [];
  allProjectInsightList: any[] = [];
  surveyColumns:any[] = ['surveyName','description','isActive','createdByName','createdOn'];
  projectInsightMilestoneList:ProjectMilestone[] = [new ProjectMilestone()];
  projectInsightResponseList:ProjectMilestone[] = [new ProjectMilestone()];

  page = 1;
  filters:any = {};

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
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.getEmployeeList();
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

  isSearchTabClick(){
    this.searchTabClick = true;

    this.projectInsightTabClick = false;
    this.isProjectInsightList = false;
    this.isQuestionForm = false;
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
  }

  showProjectInsight() {
    this.isProjectInsightList = true;
    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isProjectInsightResponseList = false;
    this.getAllProjectInsightList();
  }

  // --------------------------------- Search :: start-----------------------------

  onSearchTerm() {
    this.projectInsightService.onSearchTerm(this.searchTerm).pipe(first()).subscribe(
      (response: any) => {
        this.searchResultProjectList = response;
      },
      (error) => {
        console.error(error);
      }
    );
  }

  clearSearch() {
    this.searchTerm = '';
  }

// --------------------------------- Search :: end-----------------------------


  showProjectInsightUpdate(projectObj:any, template: TemplateRef<any>){
    this.isQuestionForm = true;
    this.isUpdation = true;
    this.isCreation = false;
    this.isProjectInsightResponseList = false;
    this.isProjectInsightList = false;
    this.projectInsight = new ProjectInsight();
    this.projectInsightMilestoneList = [];
    this.projectInsightService.getAllQuestionsByProjectId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsight = response.serviceResponse;
        this.projectInsightMilestoneList = this.projectInsight.projectInsightMilestoneList;

        this.projectInsight.questionList.forEach((question: ProjectQuestion) => {
          question.optionsList = JSON.parse(question.options);
        })
        if (this.projectInsightMilestoneList != null && this.projectInsightMilestoneList.length != 0) {
          this.projectInsightMilestoneList.forEach((projectMilestone: ProjectMilestone) => {
            projectMilestone.questionList.forEach((question: ProjectQuestion) => {
              question.optionsList = JSON.parse(question.options);
            });

            if (projectMilestone.moduleList != null && projectMilestone.moduleList.length != 0) {
              projectMilestone.moduleList.forEach((moduleObj: any) => {
                if (moduleObj.questionList != null && moduleObj.questionList.length != 0) {
                  moduleObj.questionList.forEach((question: ProjectQuestion) => {
                    question.optionsList = JSON.parse(question.options);
                  });
                }

                if (moduleObj.subModuleList != null && moduleObj.subModuleList.length != 0) {
                  this.convertStringToJSONSubModuleNodesOption(moduleObj.subModuleList);
                }
              });
            }
          });
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  convertStringToJSONSubModuleNodesOption(subModuleList){
    subModuleList.forEach((submoduleObj: any) => {
      if(submoduleObj.questionList != undefined && submoduleObj.questionList != null && submoduleObj.questionList.length != 0){
        submoduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.optionsList = JSON.parse(questionObj.options);
        });
      }
      if(submoduleObj.subSubModuleList != undefined && submoduleObj.subSubModuleList != null && submoduleObj.subSubModuleList.length != 0){
        this.convertStringToJSONSubModuleNodesOption(submoduleObj.subSubModuleList);
      }
    });
  }

  getAllProjects() {
    this.allProjectList = [];
    this.projectService.getAllProjects().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectList = response.serviceResponse;
        this.allProjectList.forEach(project => {
          project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          project.updatedOn = (project.updatedOn) ? moment(project.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        })
        this.allProjectList = this.allProjectList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.projectId === value.projectId
          ))
        );
        this.allProjectList = this.allProjectList.sort((a, b) => a.createdOn - b.createdOn);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getProjectManangerInfo(projectId: any) {
    this.allProjectList.forEach((object) => {
      if (object.projectId == projectId) {
        this.projectInsight.projectManagerName = object.employeeName;
        this.projectInsight.projectManagerId = object.empId;
        this.projectInsight.projectName = object.projectName;
      }
    });
  }

  // Add button config
  //Milestone
  addMilestone(mileIndex){
    this.projectInsightMilestoneList.splice(mileIndex+1,0,new ProjectMilestone());
  }

  addModule(type: any, index:any){
    this.projectInsightMilestoneList[index].moduleList.splice(this.projectInsightMilestoneList[index].moduleList.length+1,0,new ProjectModule());
  }

  addSubModule(type: any, milIndex:any, modIndex:any){
    this.projectInsightMilestoneList[milIndex].moduleList[modIndex].subModuleList.splice(this.projectInsightMilestoneList[milIndex].moduleList[modIndex].subModuleList.length+1,0,new ProjectSubModule());
  }
  
  addQuestion(type: any, mileIndex: any, modIndex?:any, subModIndex?: any,subSubModIndex?:any){
    if(type == 'project'){
      this.projectInsight.questionList.splice(this.projectInsight.questionList.length + 1, 0, new ProjectQuestion());
    }else if(type == 'milestone'){
      this.projectInsightMilestoneList[mileIndex].questionList.splice(this.projectInsightMilestoneList[mileIndex].questionList.length + 1, 0, new ProjectQuestion());
    }else if(type == 'module'){
      this.projectInsightMilestoneList[mileIndex].moduleList[modIndex].questionList.splice(this.projectInsightMilestoneList[mileIndex].moduleList[modIndex].questionList.length + 1, 0, new ProjectQuestion());
    }else if(type == 'submodule'){
      this.projectInsightMilestoneList[mileIndex].moduleList[modIndex].subModuleList[subModIndex].questionList.splice(this.projectInsightMilestoneList[mileIndex].moduleList[modIndex].subModuleList[subModIndex].questionList.length + 1, 0, new ProjectQuestion());
    }
  }

  removeMilestone(mileIndex){ 
    this.projectInsightMilestoneList.splice(mileIndex,1);
  }

  removeModule(mileIndex:any, modIndex:any){
    this.projectInsightMilestoneList[mileIndex].moduleList.splice(modIndex);
  }

  removeSubModule(mileIndex:any, modIndex:any, subModIndex:any){
    this.projectInsightMilestoneList[mileIndex].moduleList[modIndex].subModuleList.splice(subModIndex);
  }

  removeQuestion(index: any){
    this.projectInsight.questionList.splice(index, 1);
  }

  // Manage Options
  addOption(i, questionObj?:ProjectQuestion){
    let question = this.projectInsight.questionList.find(ques => ques == questionObj);
    question.optionsList.splice(i+1,0, new SurveyOption());
  }

  removeOption(i, questionObj?:ProjectQuestion){
    let question = this.projectInsight.questionList.find(ques => ques == questionObj);
    question.optionsList.splice(i,1);
  }

  setOption(questionObj:ProjectQuestion, projIndex:any){
    if(questionObj.optionType == "checkbox" || questionObj.optionType == "radio"){
      let question = this.projectInsight.questionList.find(ques => ques == questionObj);
      question.optionsList = [];
      question.optionsList.splice(1,0,new SurveyOption());
    }
  }

  addSubSubModule(parentSubModule: ProjectSubModule): void {
    if (!parentSubModule.subSubModuleList) {
      parentSubModule.subSubModuleList = []
    }
    parentSubModule.subSubModuleList.push(new ProjectSubModule());
  }

  removeSubSubModule(parentSubModule: ProjectSubModule, childIndex: number): void {
    if (parentSubModule.subSubModuleList) {
      parentSubModule.subSubModuleList.splice(childIndex, 1)
    }
  }

  addSubSubModuleQuestion(sub:ProjectSubModule){
    if (!sub.questionList) {
      sub.questionList = []
    }
    sub.questionList.push(new ProjectQuestion());
  }

  onPreiew(previewTemplate: TemplateRef<any>, template: TemplateRef<any>){
    let inputValidated: boolean = this.validateProjectInsight(template, this.projectInsight, this.projectInsightMilestoneList);
    if (!inputValidated) return;

    const questionTemplate:string = this.createTemplate();
    let previewObj = new ProjectInsight();
    previewObj.projectId = this.projectInsight.projectId;
    previewObj.projectManagerId = this.projectInsight.projectManagerId;
    previewObj.projectManagerName = this.projectInsight.projectManagerName;
    previewObj.projectInsightMilestoneList = this.projectInsightMilestoneList;
    previewObj.projectInsightQuestionTemplate = questionTemplate;
    this.openSurveyPreviewMod(previewTemplate,previewObj);
  }

  validateProjectInsight(template: TemplateRef<any>, projectInsight: ProjectInsight, projectInsightQuestionList: ProjectMilestone[]) {
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsight.projectId)) {
      this.alertMessage = "Please select Project name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(projectInsight.projectManagerName)) {
      this.alertMessage = "Please select Project Manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (projectInsight.questionList.length > 0) {
      projectInsight.questionList.forEach((question: ProjectQuestion, index) => {
        if (!this.validationService.validateNullUndefinedEmptyString(question.question)
          || !this.validationService.validateNullUndefinedEmptyString(question.optionType)
          || !this.validationService.validateNullUndefinedEmptyString(question.question)
        ) {
          if (!this.validationService.validateNullUndefinedEmptyString(question.question)) {
            this.alertMessage = `Please enter Question ${index + 1} !!`;
            this.openAlertMod(template, this.alertMessage);
            flag = false;
            return false;
          }

          if (!this.validationService.validateNullUndefinedEmptyString(question.optionType)) {
            this.alertMessage = `Please select Option Type ${index + 1} !!`;
            this.openAlertMod(template, this.alertMessage);
            flag = false;
            return false;
          }
          if (question.optionType == "radio" || question.optionType == "checkbox") {
            if (question.optionsList.length < 2) {
              this.alertMessage = `Please provide atleast 2 options for Question ${index + 1} !!`;
              this.openAlertMod(template, this.alertMessage);
              flag = false;
              return false;
            } else {
              question.optionsList.forEach((option: SurveyOption, opIndex) => {
                if (!this.validationService.validateNullUndefinedEmptyString(option.optionValue)) {
                  this.alertMessage = `Please enter option ${opIndex + 1} for Question ${index + 1} !!`;
                  this.openAlertMod(template, this.alertMessage);
                  flag = false;
                  return false;
                }
              });
            }
          }
        }
      })
    }

    let flag = true;
    projectInsightQuestionList.forEach((milestone: ProjectMilestone, mileIndex) => {
      if (!this.validationService.validateNullUndefinedEmptyString(milestone.milestone)) {
        this.alertMessage = `Please enter milestone ${mileIndex + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return false;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(milestone.assignedToUserId)) {
        this.alertMessage = `Please select assign user in milestone ${mileIndex + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return false;
      }

      if (milestone.questionList != null && milestone.questionList.length != 0) {
        flag = this.questionValidation(milestone.questionList, mileIndex, template, 'Milestone');
      }

      if (milestone.moduleList != null && milestone.moduleList.length != 0) {
        milestone.moduleList.forEach((module: any, modIndex) => {
          if (!this.validationService.validateNullUndefinedEmptyString(module.module)) {
            this.alertMessage = `Please enter module ${modIndex + 1} !!`;
            this.openAlertMod(template, this.alertMessage);
            flag = false;
            return false;
          }

          if (!this.validationService.validateNullUndefinedEmptyString(module.assignedToUserId)) {
            this.alertMessage = `Please select assign user in module ${modIndex + 1} !!`;
            this.openAlertMod(template, this.alertMessage);
            flag = false;
            return false;
          }

          if (module.questionList != null && module.questionList.length != 0) {
            flag = this.questionValidation(module.questionList, modIndex, template, 'Module');
          }

          if (module.subModuleList != null && module.subModuleList.length != 0) {
            module.subModuleList.forEach((submodule: any, submodIndex) => {
              if (!this.validationService.validateNullUndefinedEmptyString(submodule.subModule)) {
                this.alertMessage = `Please enter sub-module ${submodIndex + 1} !!`;
                this.openAlertMod(template, this.alertMessage);
                flag = false;
                return false;
              }

              if (!this.validationService.validateNullUndefinedEmptyString(submodule.assignedToUserId)) {
                this.alertMessage = `Please select assign user in sub-module ${submodIndex + 1} !!`;
                this.openAlertMod(template, this.alertMessage);
                flag = false;
                return false;
              }

              if (submodule.questionList != null && submodule.questionList.length != 0) {
                flag = this.questionValidation(submodule.questionList, submodIndex, template, 'sub-module');
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


  onSubmit(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateProjectInsight(template, this.projectInsight, this.projectInsightMilestoneList);
    if (!inputValidated) return;

    const questionTemplate:string = this.createTemplate();

    let projObj = new ProjectInsight();
    projObj.projectId = this.projectInsight.projectId;
    projObj.projectManagerId = this.projectInsight.projectManagerId;
    projObj.projectManagerName = this.projectInsight.projectManagerName;
    projObj.projectInsightMilestoneList = this.projectInsightMilestoneList;
    projObj.projectInsightQuestionTemplate = questionTemplate;
    projObj.createdBy = this.currentUser.empId;
    projObj.questionList = this.projectInsight.questionList;

    if(projObj.questionList != null && projObj.questionList.length != 0){
      projObj.questionList.forEach((questionObj: ProjectQuestion) => {
        questionObj.options = JSON.stringify(questionObj.optionsList);
      });
    }

    projObj.projectInsightMilestoneList.forEach((proj:ProjectMilestone) => {

      if(proj.questionList != null && proj.questionList.length != 0){
        proj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }

      if(proj.moduleList != null && proj.moduleList.length != 0){
        proj.moduleList.forEach((moduleObj: any) => {

          if(moduleObj.questionList != null && moduleObj.questionList.length != 0){
            moduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
              questionObj.options = JSON.stringify(questionObj.optionsList);
            });
          }

          if(moduleObj.subModuleList != null && moduleObj.subModuleList.length != 0){
            this.convertJSONToStringSubModuleNodesOption(moduleObj.subModuleList);
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

  convertJSONToStringSubModuleNodesOption(subModuleList:any){
    subModuleList.forEach((submoduleObj: any) => {
      if(submoduleObj.questionList != undefined && submoduleObj.questionList != null && submoduleObj.questionList.length != 0){
        submoduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }
      if(submoduleObj.subSubModuleList != undefined && submoduleObj.subSubModuleList != null && submoduleObj.subSubModuleList.length != 0){
        this.convertJSONToStringSubModuleNodesOption(submoduleObj.subSubModuleList);
      }
    });
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
    this.projectInsight = new ProjectInsight();
    this.projectInsightMilestoneList = [];

    this.projectInsightService.getAllQuestionsByProjectId(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsight = response.serviceResponse;
        this.projectInsightMilestoneList = this.projectInsight.projectInsightMilestoneList;

        const questionTemplate: string = this.createTemplate();
        let previewObj = new ProjectInsight();
        previewObj.projectId = this.projectInsight.projectId;
        previewObj.projectManagerId = this.projectInsight.projectManagerId;
        previewObj.projectManagerName = this.projectInsight.projectManagerName;
        previewObj.projectInsightMilestoneList = this.projectInsightMilestoneList;
        previewObj.projectInsightQuestionTemplate = questionTemplate;
        this.openSurveyPreviewMod(template, previewObj);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdate(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateProjectInsight(template, this.projectInsight, this.projectInsightMilestoneList);
    if (!inputValidated) return;

    let projObj = new ProjectInsight();
      projObj.projectId = this.projectInsight.projectId;
      projObj.projectManagerId = this.projectInsight.projectManagerId;
      projObj.projectManagerName = this.projectInsight.projectManagerName;
      projObj.projectInsightMilestoneList = this.projectInsightMilestoneList;
      projObj.updatedBy = this.currentUser.empId;
      projObj.questionList = this.projectInsight.questionList;

      if(projObj.questionList != null && projObj.questionList.length != 0){
        projObj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }
  
      projObj.projectInsightMilestoneList.forEach((proj:ProjectMilestone) => {
        if(proj.questionList != null && proj.questionList.length != 0){
          proj.questionList.forEach((questionObj: ProjectQuestion) => {
            questionObj.options = JSON.stringify(questionObj.optionsList);
          });
        }
  
        if(proj.moduleList != null && proj.moduleList.length != 0){
          proj.moduleList.forEach((moduleObj: any) => {
            if(moduleObj.questionList != null && moduleObj.questionList.length != 0){
              moduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
                questionObj.options = JSON.stringify(questionObj.optionsList);
              });
            }
  
            if(moduleObj.subModuleList != null && moduleObj.subModuleList.length != 0){
              this.convertJSONToStringSubModuleNodesOption(moduleObj.subModuleList);
            }
          });
        }
      });

    this.projectInsightService.updateProjectInsightQuestion(projObj).pipe(first()).subscribe((response: any) => {
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
  this.projectInsightMilestoneList.forEach((milestone: ProjectMilestone, mileIndex) => {
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
            <span style="font-size: 0.9rem;">Assigned To: ${milestone.assignedToUserId || ''}</span>
          </div>
        </div>
        <div class="milestone-content" style="padding: 25px;">
    `;

    // Milestone Questions
    if (milestone.questionList && milestone.questionList.length > 0) {
      projectInsightQueTemplate += this.createQuestionsSection(milestone.questionList, 'Milestone');
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
                Assigned To: ${module.assignedToUserId || ''}
              </div>
            </div>
        `;

        // Module Questions
        if (module.questionList && module.questionList.length > 0) {
          projectInsightQueTemplate += this.createQuestionsSection(module.questionList, 'Module');
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
                    Assigned To: ${subModule.assignedToUserId || ''}
                  </div>
                </div>
            `;

            // SubModule Questions
            if (subModule.questionList && subModule.questionList.length > 0) {
              projectInsightQueTemplate += this.createQuestionsSection(subModule.questionList, 'SubModule');
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

  //modals

  openSurveyPreviewMod(template: TemplateRef<any>, surveyObj:ProjectInsight) {
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
    let surveyContainer = document.getElementById("survey-container");
    surveyContainer.insertAdjacentHTML('beforeend', surveyObj.projectInsightQuestionTemplate);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

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
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData){
    this.filters = searchData;
  }


  getAllProjectInsightResponsesByProjectId(projectObj: any, alertTemplate: TemplateRef<any>, insightResponseTemplate: TemplateRef<any>, isPreview: any) {
    this.isResponsePreview = isPreview;
    this.projectInsightResponseList = [];
    this.isFinalResponseSubmitted = true;
    let projObj = new ProjectInsight();
    projObj.empId = this.currentUser.empId;
    projObj.projectId = projectObj.projectId;
    projObj.projectManagerId = projectObj.projectManagerId;
    projObj.projectManagerName = projectObj.projectManagerName;
    projObj.employeeRole = this.currentUser.employeeRole;
    projObj.performanceTabName = 'Performance Dashboard'

    this.projectInsightService.getAllProjectInsightResponsesByProjectId(projObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectInsight = response.serviceResponse;
        this.projectInsightResponseList = this.projectInsight.projectInsightMilestoneList;

        if (this.projectInsight.assignedToUserId != undefined && this.projectInsight.assignedToUserId != null && this.projectInsight.assignedToUserId?.length > 0) {
          this.projectInsight.toTagEmployeeList = this.employeeList.filter(emp => !this.projectInsight.assignedToUserId.includes(emp.empId));
        } else {
          this.projectInsight.toTagEmployeeList = this.employeeList;
        }

        if (this.projectInsight.questionList != null && this.projectInsight.questionList.length != 0) {
          this.projectInsight.questionList.forEach((question: ProjectQuestion, index) => {
            if (this.projectInsight.assignedToUserId != undefined && this.projectInsight.assignedToUserId != null && this.projectInsight.assignedToUserId?.length > 0) {
              question.toTagEmployeeList = this.employeeList.filter(emp => !this.projectInsight.assignedToUserId.includes(emp.empId));
            } else {
              question.toTagEmployeeList = this.employeeList;
            }
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
                if (response.isDraft == 'Y') {
                  this.isFinalResponseSubmitted = false;
                }
                if (response.isDraft == 'Y') {
                  response.showDocDiv = true;
                } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                  response.showDocDiv = true;
                } else {
                  response.showDocDiv = false;
                }
                if (this.isResponsePreview) {
                  response.showDocDiv = true;
                }
              });
            } else {
              let projectResponse = new ProjectResponse();
              projectResponse.optionsList = JSON.parse(question.options);
              if (question.optionType == 'checkbox') {
                projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
              }
              this.isFinalResponseSubmitted = false;
              projectResponse.showDocDiv = true;
              question.projectResponseList.push(projectResponse);
            }
          });
        }

        this.projectInsightResponseList.forEach((milestone: ProjectMilestone, mileIndex) => {
          milestone.isCollapsed = true;
          if (milestone.assignedToUserId != undefined && milestone.assignedToUserId != null && milestone.assignedToUserId?.length > 0) {
            milestone.toTagEmployeeList = this.employeeList.filter(emp => !milestone.assignedToUserId.includes(emp.empId));
          } else {
            milestone.toTagEmployeeList = this.employeeList;
          }
          if (milestone.questionList != null && milestone.questionList.length != 0) {
            milestone.questionList.forEach((question: ProjectQuestion, index) => {
              if (milestone.assignedToUserId != undefined && milestone.assignedToUserId != null && milestone.assignedToUserId?.length > 0) {
                question.toTagEmployeeList = this.employeeList.filter(emp => !milestone.assignedToUserId.includes(emp.empId));
              } else {
                question.toTagEmployeeList = this.employeeList;
              }
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
                  if (response.isDraft == 'Y') {
                    this.isFinalResponseSubmitted = false;
                  }
                  if (response.isDraft == 'Y') {
                    response.showDocDiv = true;
                  } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                    response.showDocDiv = true;
                  } else {
                    response.showDocDiv = false;
                  }
                  if (this.isResponsePreview) {
                    response.showDocDiv = true;
                  }
                });
              } else {
                let projectResponse = new ProjectResponse();
                projectResponse.optionsList = JSON.parse(question.options);
                if (question.optionType == 'checkbox') {
                  projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                }
                this.isFinalResponseSubmitted = false;
                projectResponse.showDocDiv = true;
                question.projectResponseList.push(projectResponse);
              }
            });
          }

          if (milestone.moduleList != null && milestone.moduleList.length != 0) {
            milestone.moduleList.forEach((module: any, modIndex) => {
              if (module.assignedToUserId != undefined && module.assignedToUserId != null && module.assignedToUserId?.length > 0) {
                module.toTagEmployeeList = this.employeeList.filter(emp => !milestone.assignedToUserId.includes(emp.empId));
              } else {
                module.toTagEmployeeList = this.employeeList;
              }
              if (module.questionList != null && module.questionList.length != 0) {
                module.questionList.forEach((question: ProjectQuestion, index) => {
                  if (module.assignedToUserId != undefined && module.assignedToUserId != null && module.assignedToUserId?.length > 0) {
                    question.toTagEmployeeList = this.employeeList.filter(emp => !milestone.assignedToUserId.includes(emp.empId));
                  } else {
                    question.toTagEmployeeList = this.employeeList;
                  }
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
                      if (response.isDraft == 'Y') {
                        this.isFinalResponseSubmitted = false;
                      }
                      if (response.isDraft == 'Y') {
                        response.showDocDiv = true;
                      } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                        response.showDocDiv = true;
                      } else {
                        response.showDocDiv = false;
                      }
                      if (this.isResponsePreview) {
                        response.showDocDiv = true;
                      }
                    });
                  } else {
                    let projectResponse = new ProjectResponse();
                    projectResponse.optionsList = JSON.parse(question.options);
                    if (question.optionType == 'checkbox') {
                      projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
                    }
                    this.isFinalResponseSubmitted = false;
                    projectResponse.showDocDiv = true;
                    question.projectResponseList.push(projectResponse);
                  }
                });
              }

              if (module.subModuleList != null && module.subModuleList.length != 0) {
                this.createSubModuleListObject(module.subModuleList);
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

  createSubModuleListObject(subModuleList: any) {
    subModuleList.forEach((submodule: any, submodIndex) => {
      if (submodule.questionList != null && submodule.questionList.length != 0) {
        if (submodule.assignedToUserId != undefined && submodule.assignedToUserId != null && submodule.assignedToUserId?.length > 0) {
          submodule.toTagEmployeeList = this.employeeList.filter(emp => !submodule.assignedToUserId.includes(emp.empId));
        } else {
          submodule.toTagEmployeeList = this.employeeList;
        }
        submodule.questionList.forEach((question: ProjectQuestion, index) => {
          if (submodule.assignedToUserId != undefined && submodule.assignedToUserId != null && submodule.assignedToUserId?.length > 0) {
            question.toTagEmployeeList = this.employeeList.filter(emp => !submodule.assignedToUserId.includes(emp.empId));
          } else {
            question.toTagEmployeeList = this.employeeList;
          }
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
              if (response.isDraft == 'Y') {
                this.isFinalResponseSubmitted = false;
              }
              if (response.isDraft == 'Y') {
                response.showDocDiv = true;
              } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                response.showDocDiv = true;
              } else {
                response.showDocDiv = false;
              }
              if (this.isResponsePreview) {
                response.showDocDiv = true;
              }
            });
          } else {
            let projectResponse = new ProjectResponse();
            projectResponse.optionsList = JSON.parse(question.options);
            if (question.optionType == 'checkbox') {
              projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
            }
            this.isFinalResponseSubmitted = false;
            projectResponse.showDocDiv = true;
            question.projectResponseList.push(projectResponse);
          }
        });
      }
      if (submodule.subSubModuleList != null && submodule.subSubModuleList.length != 0) {
        this.createSubModuleListObject(submodule.subSubModuleList);
      }
    });
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl',ignoreBackdropClick: true, keyboard: false  });
  }

  closeDocumentPreviewTemplate() {
    this.documentPreviewModalRef.hide();
  }

  closeProjectInsightResponseModal() {
    this.projectResponseModalRef.hide();
  }

  onCheckboxChange(event: any, value: string, question: any, option: any, response: any) {
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

  removeUploadedFile(response: any) {
    if (response?.uploadedFile) {
      response.uploadedFile = null;
    }
    if (response?.uploadedFileName) {
      response.uploadedFileName = null;
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

  base64ToBlob(base64: string, contentType: string, sliceSize = 512): Blob {
    const byteCharacters = atob(base64); // decode base64
    const byteArrays = [];

    for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      const slice = byteCharacters.slice(offset, offset + sliceSize);

      const byteNumbers = new Array(slice.length);
      for (let i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      byteArrays.push(byteArray);
    }
    return new Blob(byteArrays, { type: contentType });
  }

  onQuestionFileChange(event: any, question: any, alertTemplate: TemplateRef<any>, previewElementId: any, documentPreviewTemplate: TemplateRef<any>, response: any) {
    const file = event.target.files[0];
    if (file) {
      response.uploadedFile = file;
      const inputId = event.target.id;
      var fileExtension = file.name.split('.').pop().toLowerCase();
      response.uploadedFileName = this.currentUser.empId + '-' + inputId + '.' + fileExtension;
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
    this.previewUploadedFile(question, response.uploadedFile, response.uploadedFileName, previewElementId, documentPreviewTemplate, alertTemplate, false, response);
  }

  previewUploadedFile(question, uploadedFile: any, fileName: any, previewElementId: any, documentPreviewTemplate: TemplateRef<any>, alertTemplate: TemplateRef<any>, downloadFile: any, response: any) {

    if ((fileName != undefined && fileName != null)) {
      const MAX_SIZE = 5 * 1024 * 1024;
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
          if (downloadFile) {
            this.downloadFile(file, fileName);
            return;
          }
        }
      } else {
        this.getUserUploadedFileForQuestion(question, fileName, documentPreviewTemplate, previewElementId);
      }
    } else {
      this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
      const previewContainer = document.getElementById(previewElementId);
      response.uploadedFile = null;
      response.uploadedFileName = null;
      previewContainer.innerHTML = `<h3 style='padding: 12px; display:inline-block;'>No Data Found.</h3>`;
    }
  }

  getUserUploadedFileForQuestion(question: any, fileName: any, documentPreviewTemplate: TemplateRef<any>, previewElementId: any) {
    let documentObj = new Document();
    documentObj.empId = this.currentUser.empId;
    documentObj.documentName = fileName;
    documentObj.typeId = question.entityId;
    documentObj.typeName = question.entityType;
    this.projectInsightService.getUserUploadedFileForQuestion(documentObj).subscribe((response: any) => {
      this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
      const previewContainer = document.getElementById(previewElementId);

      if (response.serviceStatus === 'Fail') {
        previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;' > File Not Found.</span>";
      } else {
        const base64Data = response?.serviceResponse?.body;
        let contentTypeList = response?.serviceResponse?.headers["Content-Type"];
        let contentType = contentTypeList[0]
        if (contentType == 'application/pdf') {
          previewContainer.innerHTML = `<embed src="data:application/pdf;base64,${base64Data}" type="application/pdf" width="100%" height="800px" />`;
        }
        else if (contentType.startsWith('image/')) {
          previewContainer.innerHTML = `<img src="data:${contentType};base64,${base64Data}" class="img-fluid" style="max-height:800px;" />`;
        }
        else {
          let file = this.base64ToBlob(base64Data, contentType, 512);
          this.downloadFile(file, fileName);
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;' > Can't Open File.</span>";
          this.closeDocumentPreviewTemplate();
        }
      }
    });
  }

  saveProjectInsightResponse(template: TemplateRef<any>, finalSubmit: any) {
    // if(finalSubmit){
    //   let inputValidated: boolean = this.validateProjectInsightResponse(template,this.projectInsightResponseList,this.projectInsightQuestion);
    //   if (!inputValidated) return;
    // }
    let files: File[] = [];

    if (this.projectInsight.questionList?.length) {
      let applicationQuestionList = this.projectInsight.questionList;
      for (let index = 0; index < applicationQuestionList.length; index++) {
        let question = applicationQuestionList[index];
        for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
          let response = question.projectResponseList[responseIndex];
          response.responseByEmpId = this.currentUser.empId;
          response.isDraft = finalSubmit ? 'N' : 'Y';
          if (question.optionType == 'checkbox') {
            response.response = JSON.stringify(response.responseList);
            response.responseList = null;
          }
          if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
            const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
            files.push(renamedFile);
            response.uploadedFile = null;
          }
        }
      }
    }

    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightResponseList.length; milestoneIndex++) {
      let milestone = this.projectInsightResponseList[milestoneIndex];
      if (milestone.questionList?.length) {
        let questionList = milestone.questionList;
        for (let index = 0; index < questionList.length; index++) {
          let question = questionList[index];
          for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
            let response = question.projectResponseList[responseIndex];
            response.responseByEmpId = this.currentUser.empId;
            response.isDraft = finalSubmit ? 'N' : 'Y';
            if (question.optionType == 'checkbox') {
              response.response = JSON.stringify(response.responseList);
              response.responseList = null;
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
        if (module.questionList?.length) {
          let moduleQuestionList = module.questionList;
          for (let index = 0; index < moduleQuestionList.length; index++) {
            let question = moduleQuestionList[index];
            for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
              let response = question.projectResponseList[responseIndex];
              response.responseByEmpId = this.currentUser.empId;
              response.isDraft = finalSubmit ? 'N' : 'Y';
              if (question.optionType == 'checkbox') {
                response.response = JSON.stringify(response.responseList);
                response.responseList = null;
              }
              if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
                const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
                files.push(renamedFile);
                response.uploadedFile = null;
              }
            }
          }
        }

        this.convertResponseAndRenameFile(module.subModuleList, finalSubmit, files);
      }
    }

    let projObj = new ProjectInsight();
    projObj.taggedToUserId = this.projectInsight.taggedToUserId;
    projObj.questionList = this.projectInsight.questionList;
    projObj.projectId = this.projectInsight.projectId;
    projObj.projectManagerId = this.projectInsight.projectManagerId;
    projObj.projectManagerName = this.projectInsight.projectManagerName;
    projObj.projectInsightMilestoneList = this.projectInsightResponseList;
    projObj.empId = this.currentUser.empId;
    this.projectInsightService.saveProjectInsightResponse(projObj, files).pipe(first()).subscribe((response: any) => {
      this.closeProjectInsightResponseModal();
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsight();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  convertResponseAndRenameFile(subModuleList: any, finalSubmit: any, files: any) {
    for (let submoduleIndex = 0; submoduleIndex < (subModuleList?.length || 0); submoduleIndex++) {
      let submodule = subModuleList[submoduleIndex];
      if (submodule.questionList?.length) {
        let submoduleProjectQuestion = submodule.questionList;
        for (let index = 0; index < submodule.questionList.length; index++) {
          let question = submoduleProjectQuestion[index];
          for (let responseIndex = 0; responseIndex < question.projectResponseList.length; responseIndex++) {
            let response = question.projectResponseList[responseIndex];
            response.isDraft = finalSubmit ? 'N' : 'Y';
            response.responseByEmpId = this.currentUser.empId;
            if (question.optionType == 'checkbox') {
              response.response = JSON.stringify(response.responseList);
              response.responseList = null;
            }
            if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
              const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
              files.push(renamedFile);
              response.uploadedFile = null;
            }
          }
        }
      }

      if (submodule.subSubModuleList != undefined && submodule.subSubModuleList != null && submodule.subSubModuleList?.length != 0) {
        this.convertResponseAndRenameFile(submodule.subSubModuleList, finalSubmit, files)
      }
    }
  }

  validateProjectInsightResponse(template: TemplateRef<any>, projectInsightResponseList: ProjectMilestone[], projectInsightQuestion: any) {
    for (let milestoneIndex = 0; milestoneIndex < projectInsightResponseList.length; milestoneIndex++) {
      let milestone = projectInsightResponseList[milestoneIndex];

      if (milestone.questionList?.length) {
        if (!this.validateQuestionAndResponse(milestone.questionList, milestoneIndex, template, 'Milestone')) {
          return false;
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];

        if (module.questionList?.length) {
          if (!this.validateQuestionAndResponse(module.questionList, moduleIndex, template, 'Milestone - ' + (milestoneIndex + 1) + ' Module')) {
            return false;
          }
        }

        for (let submoduleIndex = 0; submoduleIndex < (module.subModuleList?.length || 0); submoduleIndex++) {
          let submodule = module.subModuleList[submoduleIndex];

          if (submodule.questionList?.length) {
            if (!this.validateQuestionAndResponse(submodule.questionList, submoduleIndex, template, 'Milestone - ' + milestoneIndex + 1 + ' Module - ' + (moduleIndex + 1) + 'Sub-module')) {
              return false;
            }
          }
        }
      }
    }
    return true;
  }

  validateQuestionAndResponse(questionList: ProjectQuestion[], parentIndex: number, template: TemplateRef<any>, parentType: string) {
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
}