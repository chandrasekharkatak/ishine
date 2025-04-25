import { Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectInsightEntity } from 'src/app/models/projectInsightEntity';
import { ProjectInsight } from 'src/app/models/projectInsight';
import { ProjectMilestone } from 'src/app/models/projectMilestone';
import { ProjectModule } from 'src/app/models/projectModule';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ProjectSubModule } from 'src/app/models/projectSubModule';
import { SurveyOption } from 'src/app/models/sureyOption';
import { EmployeeService } from 'src/app/services/employee.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { ValidationService } from 'src/app/services/validation.service';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { Document } from 'src/app/models/document';
import { ProjectResponsePoint } from 'src/app/models/projectResponsePoint';

@Component({
  selector: 'app-project-insights',
  templateUrl: './project-insights.component.html',
  styleUrls: ['./project-insights.component.css']
})
export class ProjectInsightsComponent implements OnInit {

  @ViewChild("alert_message") alertModal: TemplateRef<any>;

  // Objects
  @Input() actionType: any = '';
  @Input() subActionType: any = '';
  @Input() projectId: any = '';
  @Input() responseByEmpId: any;
  @Output() showProjectInsightConfigListEvent: EventEmitter<any> = new EventEmitter();
  @Output() closeProjectInsightResponseModalEvent: EventEmitter<any> = new EventEmitter();
  @Output() closeSearchResponsePreviewEvent: EventEmitter<any> = new EventEmitter();

  isResponsePreview: boolean = false;
  projectInsightObj: ProjectInsight = new ProjectInsight();

  currentUser: User;
  bsModalRef: BsModalRef = new BsModalRef();
  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();
  entity: ProjectInsightEntity = new ProjectInsightEntity();
  entityObj: any;

  // Flags
  isResponsePointFinalSubmitted: boolean = false;
  isCurrentEmployeeRoleGreaterThanManager: boolean;

  // Lists
  allProjectList: any[] = [];
  allEmployeeList: any[] = [];
  employeeList: any[] = [];
  breadCrumbs: ProjectInsightEntity[] = [];
  allBreadCrumbs: ProjectInsightEntity[] = [];
  displayedResponseUserId: any[] = [];
  rolesGreaterThanManager: any[] = ['HOD', 'SuperAdmin', 'HR', 'RMG'];

  // Variables
  alertMessage: any = '';
  viewType: any = 'Project';
  selectAllValue = -1;

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private projectService: ProjectService,
    private employeeService: EmployeeService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  async ngOnInit(): Promise<void> {
    this.isCurrentEmployeeRoleGreaterThanManager = this.rolesGreaterThanManager.includes(this.currentUser?.employeeRole);
    await this.getAllProjects();

    if (this.validationService.validateNullUndefinedEmptyString(this.projectId)) {
      await this.getEmployeeListByProjectId(this.projectId);
    }

    if (this.actionType != 'Configuration') {
      await this.getEmployeeList();
    }

    let temp: any;
    this.entityObj = null;
    this.projectInsightObj = new ProjectInsight();
    this.projectInsightObj.deletedProjectInsightEntityList = [];
    this.displayedResponseUserId = [];

    if (this.actionType == 'Configuration' && this.subActionType == 'Creation') {
      this.addAllBreadCrumbsToList();
      temp = this.allBreadCrumbs.filter(entity => { if (entity.entityType === 'Project') return entity });
      if (!temp[0].entityName) {
        temp[0].entityName = 'Project Name';
      }
    }
    else if (this.actionType == 'Configuration' && this.subActionType == 'Updation') {
      await this.getAllProjectQuestionsByProjectId(this.alertMessage);
      temp = this.allBreadCrumbs.filter(entity => { if (entity.entityType === 'Project') return entity });
    }
    else if (this.actionType == 'Contribution' && (this.subActionType == 'Submit/View Response')) {
      await this.getAllProjectInsightResponsesByProjectId(this.projectId, this.currentUser.empId, this.alertModal, 'Performance Dashboard', false);
      temp = this.allBreadCrumbs.filter(entity => { if (entity.entityType === 'Project') return entity });
    }
    else if (this.subActionType == 'Review Response' || this.subActionType == 'Search' || this.subActionType == 'Preview') {
      if (this.subActionType == 'Review Response') {
        let userIdList = this.employeeList.map(employee => employee.empId);
        if (!userIdList.includes(this.responseByEmpId)) {
          let employeeList = this.allEmployeeList.filter(employee => employee.empId == this.responseByEmpId);
          this.employeeList?.push(employeeList[0]);
        }

      }
      await this.getAllProjectInsightResponsesByProjectId(this.projectId, this.responseByEmpId, this.alertModal, 'Teams Dashboard', false);
      temp = this.allBreadCrumbs.filter(entity => { if (entity.entityType === 'Project') return entity });
    }

    this.breadCrumbs.push(temp[0]);
    this.viewType = 'Project';
    this.entityObj = temp[0]?.entityObj;
    this.entityObj.currentActiveBadgeLevel = 'Details';
    if (this.actionType == 'Configuration' && this.subActionType != 'Creation') {
      this.breadCrumbs[0].entityId = this.projectInsightObj.projectId;
      this.breadCrumbs[0].entityName = this.projectInsightObj.projectName;
    }
    this.toggleSelectAll();
  }

  // BreadCrumbs Configuration
  addAllBreadCrumbsToList() {
    this.allBreadCrumbs = [];
    if (this.validationService.validateNullUndefinedEmptyString(this.projectInsightObj)) {
      this.allBreadCrumbs.push(this.createBreadCrumbObj('Project', this.projectInsightObj.projectId, this.projectInsightObj, this.projectInsightObj.projectName));
      if (!this.projectInsightObj.badgePathList) {
        this.projectInsightObj.badgePathList = this.getBadgePathList('Project');
      }
      this.projectInsightObj.currentActiveBadgeLevel = 'Details';
      if (this.projectInsightObj.questionList) {
        this.projectInsightObj.questionList.forEach((question: ProjectQuestion, index) => {
          question.badgePathList = this.getBadgePathList('Question');
          question.currentActiveBadgeLevel = 'Details';
        });
      }

      if (this.projectInsightObj.projectInsightMilestoneList != null && this.projectInsightObj.projectInsightMilestoneList?.length != 0) {
        this.projectInsightObj.projectInsightMilestoneList.forEach((projectMilestone: ProjectMilestone) => {
          this.allBreadCrumbs.push(this.createBreadCrumbObj('Milestone', projectMilestone.milestoneId, projectMilestone, projectMilestone.milestone));
          if (!projectMilestone.badgePathList) {
            projectMilestone.badgePathList = this.getBadgePathList('Milestone');
            projectMilestone.currentActiveBadgeLevel = 'Details';
          }
          if (projectMilestone.questionList) {
            projectMilestone.questionList.forEach((question: ProjectQuestion, index) => {
              question.badgePathList = this.getBadgePathList('Question');
              question.currentActiveBadgeLevel = 'Details';
            });
          }
          if (projectMilestone.moduleList != null && projectMilestone.moduleList?.length != 0) {
            projectMilestone.moduleList.forEach((moduleObj: ProjectModule) => {
              this.allBreadCrumbs.push(this.createBreadCrumbObj('Module', moduleObj.moduleId, moduleObj, moduleObj.module));

              if (!moduleObj.badgePathList) {
                moduleObj.badgePathList = this.getBadgePathList('Module');
                moduleObj.currentActiveBadgeLevel = 'Details';
              }
              if (moduleObj.questionList) {
                moduleObj.questionList.forEach((question: ProjectQuestion, index) => {
                  question.badgePathList = this.getBadgePathList('Question');
                  question.currentActiveBadgeLevel = 'Details';
                });
              }
              if (moduleObj.subModuleList != null && moduleObj.subModuleList?.length != 0) {
                this.addAllSubModuleBreadCrumbs(moduleObj.subModuleList, 'SubModule');
              }
            });
          }
        });
      }
    }
  }

  addAllSubModuleBreadCrumbs(subModuleList: ProjectSubModule[], subModuleType) {
    subModuleList.forEach((submoduleObj: ProjectSubModule) => {
      this.allBreadCrumbs.push(this.createBreadCrumbObj(subModuleType, submoduleObj.subModuleId, submoduleObj, submoduleObj.subModule));
      if (!submoduleObj.badgePathList) {
        submoduleObj.badgePathList = this.getBadgePathList('SubModule', subModuleType);
        submoduleObj.currentActiveBadgeLevel = 'Details';
      }
      if (submoduleObj.questionList) {
        submoduleObj.questionList.forEach((question: ProjectQuestion, index) => {
          question.badgePathList = this.getBadgePathList('Question');
          question.currentActiveBadgeLevel = 'Details';
        });
      }
      if (submoduleObj.subSubModuleList != undefined && submoduleObj.subSubModuleList != null && submoduleObj.subSubModuleList?.length != 0) {
        this.addAllSubModuleBreadCrumbs(submoduleObj.subSubModuleList, 'Sub-SubModule');
      }
    });
  }

  // Navigation
  navigateTo(entityObj: any, entityType: any, entityId?: any, entityName?: any, questionParentEntityId?: any) {
    this.entityObj = null;
    this.viewType = entityType;
    this.entityObj = entityObj;
    const existingIndex = this.breadCrumbs.findIndex(b => (b.entityObj == entityObj));
    if (existingIndex !== -1) {
      this.breadCrumbs = this.breadCrumbs.slice(0, existingIndex + 1);
    } else {
      this.breadCrumbs.push(this.createBreadCrumbObj(entityType, entityId, entityObj, !entityName ? entityType : entityName));
    }
  }

  // Badge Change
  changeCurrentActiveBadgeLevel(entityObj: any, currentActiveBadgeLevel: any, entityType: any, entityName: any, entityId: any) {
    entityObj.currentActiveBadgeLevel = currentActiveBadgeLevel;
    const existingIndex = this.breadCrumbs.findIndex(b => (b.entityObj == entityObj));
    if (existingIndex === -1) {
      this.breadCrumbs.push(this.createBreadCrumbObj(entityType, entityId, entityObj, !entityName ? entityType : entityName));
    }
  }

  // Milestone Configurations
  addMilestone(milestoneList: ProjectMilestone[]) {
    let milestone: ProjectMilestone = new ProjectMilestone();
    if (!milestoneList) {
      milestoneList = [];
    }
    milestoneList.push(milestone);
    this.entityObj = null;
    this.viewType = 'Milestone';
    this.entityObj = milestone;
  }

  deleteMilestone(milestoneList: ProjectMilestone[], milestoneIndex: any) {
    if (!this.projectInsightObj.deletedProjectInsightEntityList) {
      this.projectInsightObj.deletedProjectInsightEntityList = [];
    }
    milestoneList.forEach((milestone, index) => {
      if (index == milestoneIndex && this.validationService.validateNullUndefinedEmptyString(milestone.milestoneId)) {
        let projectInsightEntity = new ProjectInsightEntity();
        projectInsightEntity.entityId = milestone.milestoneId;
        projectInsightEntity.entityType = 'Milestone';
        this.projectInsightObj.deletedProjectInsightEntityList.push(projectInsightEntity);
      }
    });
    milestoneList?.splice(milestoneIndex, 1);
  }

  // Module Configurations
  addModuleToList(moduleList: ProjectModule[]) {
    let module: ProjectModule = new ProjectModule();
    if (!moduleList) {
      moduleList = [];
    }
    moduleList.push(module);
    this.entityObj = null;
    this.viewType = 'Module';
    this.entityObj = module;
  }

  deleteModule(moduleList: ProjectModule[], moduleIndex: any) {
    if (!this.projectInsightObj.deletedProjectInsightEntityList) {
      this.projectInsightObj.deletedProjectInsightEntityList = [];
    }
    moduleList.forEach((module, index) => {
      if (index == moduleIndex && this.validationService.validateNullUndefinedEmptyString(module.moduleId)) {
        let projectInsightEntity = new ProjectInsightEntity();
        projectInsightEntity.entityId = module.moduleId;
        projectInsightEntity.entityType = 'Module';
        this.projectInsightObj.deletedProjectInsightEntityList.push(projectInsightEntity);
      }
    });
    moduleList?.splice(moduleIndex, 1);
  }

  // SubModule Configurations
  addSubModuleToList(subModuleList: ProjectSubModule[], subModuleType: any) {
    let subModule: ProjectSubModule = new ProjectSubModule();
    if (!subModuleList) {
      subModuleList = [];
    }
    subModule.subSubModuleList = [];
    subModuleList.push(subModule);
    this.entityObj = null;
    this.viewType = subModuleType;
    this.entityObj = subModule;
  }

  deleteSubModule(subModuleList: ProjectSubModule[], subModuleIndex: any, subModuleType: any) {
    if (!this.projectInsightObj.deletedProjectInsightEntityList) {
      this.projectInsightObj.deletedProjectInsightEntityList = [];
    }
    subModuleList.forEach((subModule, index) => {
      if (index == subModuleIndex && this.validationService.validateNullUndefinedEmptyString(subModule.subModuleId)) {
        let projectInsightEntity = new ProjectInsightEntity();
        projectInsightEntity.entityId = subModule.subModuleId;
        projectInsightEntity.entityType = subModuleType;
        this.projectInsightObj.deletedProjectInsightEntityList.push(projectInsightEntity);
      }
    });
    subModuleList?.splice(subModuleIndex, 1);
  }

  // Question Configurations
  addQuestion(entity: any) {
    let question: ProjectQuestion = new ProjectQuestion();
    if (!entity?.questionList) {
      entity.questionList = [];
    }
    entity?.questionList.push(question);
    this.entityObj = null;
    this.viewType = 'Question';
    this.entityObj = question;
  }

  deleteQuestion(questionList: ProjectQuestion[], questionIndex: any) {
    if (!this.projectInsightObj.deletedProjectInsightEntityList) {
      this.projectInsightObj.deletedProjectInsightEntityList = [];
    }
    questionList.forEach((question, index) => {
      if (index == questionIndex && this.validationService.validateNullUndefinedEmptyString(question.questionId)) {
        let projectInsightEntity = new ProjectInsightEntity();
        projectInsightEntity.entityId = question.questionId;
        projectInsightEntity.entityType = 'Question';
        this.projectInsightObj.deletedProjectInsightEntityList.push(projectInsightEntity);
      }
    });
    questionList?.splice(questionIndex, 1);
  }

  // Option Configurations
  addOption(i, questionObj: ProjectQuestion) {
    questionObj?.optionsList.splice(i + 1, 0, new SurveyOption());
  }

  removeOption(i, questionObj: ProjectQuestion) {
    questionObj?.optionsList.splice(i, 1);
  }

  setOption(questionObj: ProjectQuestion) {
    if (questionObj.optionType == "checkbox" || questionObj.optionType == "radio") {
      questionObj.optionsList = [];
      questionObj.optionsList.splice(1, 0, new SurveyOption());
    }
  }

  // Helpers
  createBreadCrumbObj(entityType: any, entityId: any, entityObj: any, entityName: any) {
    let crumb: ProjectInsightEntity = new ProjectInsightEntity();
    crumb.entityId = entityId;
    crumb.entityType = entityType;
    crumb.entityName = entityName;
    crumb.entityObj = entityObj;
    return crumb;
  }

  getBadgePathList(entityType: any, subModuleType?: any) {
    if (entityType == 'Project') {
      return [
        { name: 'Details' },
        { name: 'Questions' },
        { name: 'Milestones' },
      ];
    }
    else if (entityType == 'Milestone') {
      return [
        { name: 'Details' },
        { name: 'Questions' },
        { name: 'Modules' },
      ];
    }
    else if (entityType == 'Module') {
      return [
        { name: 'Details' },
        { name: 'Questions' },
        { name: 'SubModules' },
      ];
    }
    else if (entityType == 'SubModule') {
      return [
        { name: 'Details' },
        { name: 'Questions' },
        { name: subModuleType + 's' },
      ];
    }
    else if (entityType == 'Question') {
      return [
        { name: 'Details' }
      ];
    }

  }

  getProjectManangerInfo(projectId: any) {
    this.allProjectList.forEach((object) => {
      if (object.projectId == projectId) {
        this.projectInsightObj.projectManagerName = object.employeeName;
        this.projectInsightObj.projectManagerId = object.empId;
        this.projectInsightObj.projectName = object.projectName;
        this.breadCrumbs[0].entityId = object.projectId;
        this.breadCrumbs[0].entityName = object.projectName;
      }
    });
  }

  convertJSONToStringSubModuleNodesOption(subModuleList: ProjectSubModule[]) {
    subModuleList.forEach((submoduleObj: any) => {
      if (submoduleObj.questionList != undefined && submoduleObj.questionList != null && submoduleObj.questionList?.length != 0) {
        submoduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }
      if (submoduleObj.subSubModuleList != undefined && submoduleObj.subSubModuleList != null && submoduleObj.subSubModuleList?.length != 0) {
        this.convertJSONToStringSubModuleNodesOption(submoduleObj.subSubModuleList);
      }
    });
  }

  convertStringToJSONSubModuleNodesOption(subModuleList: ProjectSubModule[]) {
    subModuleList.forEach((submoduleObj: any) => {
      this.parseOptionsOfQuestionToList(submoduleObj.questionList);
      if (submoduleObj.subSubModuleList != undefined && submoduleObj.subSubModuleList != null && submoduleObj.subSubModuleList.length != 0) {
        this.convertStringToJSONSubModuleNodesOption(submoduleObj.subSubModuleList);
      }
    });
  }

  mapQuestionResponseAndFile(projectResponseList: ProjectResponse[], optionType: any, files: any) {
    for (let responseIndex = 0; responseIndex < projectResponseList?.length; responseIndex++) {
      let response = projectResponseList[responseIndex];
      if (optionType == 'checkbox') {
        response.response = JSON.stringify(response.responseList);
        response.responseList = null;
      }
      if (response?.document != undefined && response?.document != null) {
        const renamedFile = new File([response.document], response?.documentFileName, { type: response.document.type });
        files.push(renamedFile);
        response.document = null;
      }
    }
  }

  mapQuestionsResponseList(projectResponseList: ProjectResponse[], optionType: any, options: any, recommendedResponseId: any) {
    if (projectResponseList != null && projectResponseList?.length != 0) {
      projectResponseList.forEach((response: ProjectResponse, index) => {
        if (recommendedResponseId != undefined && recommendedResponseId != null && response.projectInsightResponseId == recommendedResponseId) {
          response.isRecommendedChecked = true;
        }
        if (response.projectInsightResponsePointList != undefined && response.projectInsightResponsePointList != null && response.projectInsightResponsePointList?.length > 0) {
          let responsePointsByUserIdList = response?.projectInsightResponsePointList.map(responsePoint => responsePoint.pointsBy);
          if (!responsePointsByUserIdList.includes(this.currentUser.empId)) {
            this.isResponsePointFinalSubmitted = false;
            let tempPointObj: ProjectResponsePoint = new ProjectResponsePoint();
            response?.projectInsightResponsePointList.push(tempPointObj);
          }
          let responsePointsDraftedList = response?.projectInsightResponsePointList.map(responsePoint => responsePoint.isPointsDrafted);
          if (responsePointsDraftedList?.includes('Y')) {
            this.isResponsePointFinalSubmitted = false;
          }
        } else {
          this.isResponsePointFinalSubmitted = false;
          if (!response.projectInsightResponsePointList) {
            response.projectInsightResponsePointList = [];
          }
          let tempPointObj: ProjectResponsePoint = new ProjectResponsePoint();
          tempPointObj.isPointsDrafted = 'Y';
          response?.projectInsightResponsePointList.push(tempPointObj);
        }

        response.optionsList = JSON.parse(response.options);
        if (optionType == 'checkbox') {
          response.responseList = JSON.parse(response.response || '[]');
          if (response.optionsList?.length) {
            response.optionsList.forEach((option, index) => {
              if (response?.responseList.includes(option?.optionValue)) {
                option.isChecked = true
              }
            });
          }
        }
        if (this.projectInsightObj.isFinalSubmitted == 'N' || this.isResponsePreview) {
          response.showDocDiv = true;
        } else if (this.projectInsightObj.isFinalSubmitted == 'Y' && this.validationService.validateNullUndefinedEmptyString(response.documentFileName)) {
          response.showDocDiv = true;
        } else {
          response.showDocDiv = false;
        }


      });
    } else {
      if (this.projectInsightObj.isFinalSubmitted == 'N') {
        let projectResponse = new ProjectResponse();
        projectResponse.optionsList = JSON.parse(options);
        if (optionType == 'checkbox') {
          projectResponse.responseList = JSON.parse(projectResponse.response || '[]');
        }
        projectResponse.showDocDiv = true;
        projectResponseList.push(projectResponse);
      }
    }
  }

  convertSubModuleListResponseAndRenameFile(subModuleList: ProjectSubModule[], files: any) {
    for (let submoduleIndex = 0; submoduleIndex < (subModuleList?.length || 0); submoduleIndex++) {
      let submodule = subModuleList[submoduleIndex];
      if (submodule.questionList?.length) {
        let submoduleProjectQuestion = submodule.questionList;
        for (let index = 0; index < submodule.questionList?.length; index++) {
          let question = submoduleProjectQuestion[index];
          this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
        }
      }
      if (submodule.subSubModuleList != undefined && submodule.subSubModuleList != null && submodule.subSubModuleList?.length != 0) {
        this.convertSubModuleListResponseAndRenameFile(submodule.subSubModuleList, files)
      }
    }
  }

  createSubModuleListObject(subModuleList: ProjectSubModule[], subModuleType: any) {
    subModuleList.forEach((submodule: any, submodIndex) => {
      submodule.badgePathList = this.getBadgePathList('SubModule', subModuleType);
      submodule.currentActiveBadgeLevel = 'Details';
      submodule.toTagEmployeeList = this.getToTagEmployeeList(submodule.assignedToUserId);
      if (submodule.questionList != null && submodule.questionList?.length != 0) {
        submodule.questionList.forEach((question: ProjectQuestion, index) => {
          question.badgePathList = this.getBadgePathList('Question');
          question.currentActiveBadgeLevel = 'Details';
          question.toTagEmployeeList = this.getToTagEmployeeList(submodule.assignedToUserId);
          this.mapQuestionsResponseList(question.projectResponseList, question.optionType, question.options, question.recommendedResponseId);
        });
      }
      if (submodule.subSubModuleList != null && submodule.subSubModuleList?.length != 0) {
        this.createSubModuleListObject(submodule.subSubModuleList, "Sub-SubModule");
      }
    });
  }

  parseOptionsOfQuestionToList(questionList: ProjectQuestion[]) {
    if (questionList != null && questionList?.length != 0) {
      questionList.forEach((questionObj: ProjectQuestion) => {
        if (questionObj.optionType == "checkbox" || questionObj.optionType == "radio") {
          questionObj.optionsList = JSON.parse(questionObj.options || '[]');
        }
      });
    }
  }

  getToTagEmployeeList(assignedToUserId: any[]) {
    if (assignedToUserId != undefined && assignedToUserId != null && assignedToUserId?.length > 0) {
      return this.allEmployeeList.filter(emp => !assignedToUserId.includes(emp.empId));
    } else {
      return this.allEmployeeList;
    }
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
    if (response?.document) {
      response.document = null;
    }
    if (response?.documentFileName) {
      response.documentFileName = null;
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

    for (let offset = 0; offset < byteCharacters?.length; offset += sliceSize) {
      const slice = byteCharacters.slice(offset, offset + sliceSize);

      const byteNumbers = new Array(slice?.length);
      for (let i = 0; i < slice?.length; i++) {
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
      response.document = file;
      const inputId = event.target.id;
      var fileExtension = file.name.split('.').pop().toLowerCase();
      response.documentFileName = this.currentUser.empId + '-' + inputId + '.' + fileExtension;
    }

    const MAX_SIZE = 5 * 1024 * 1024;
    if (file) {
      if (file.size > MAX_SIZE) {
        this.alertMessage = "File size must be lesser than or equal to 1MB."
        this.openAlertMod(alertTemplate, this.alertMessage);
        response.document = null;
        response.documentFileName = null;
        return false;
      }
    }
    this.previewUploadedFile(question, response.document, response.documentFileName, previewElementId, documentPreviewTemplate, alertTemplate, false, response);
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
          response.document = null;
          response.documentFileName = null;
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
            response.document = null;
            response.documentFileName = null;
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
      response.document = null;
      response.documentFileName = null;
      previewContainer.innerHTML = `<h3 style='padding: 12px; display:inline-block;'>No Data Found.</h3>`;
    }
  }

  showProjectInsightsConfigList() {
    this.showProjectInsightConfigListEvent.emit();
  }

  // Validations
  validateProjectInsight(template: TemplateRef<any>, projectInsight: ProjectInsight) {
    let flag = true;
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

    if ((projectInsight.questionList == undefined || projectInsight.questionList == null || projectInsight.questionList?.length == 0) &&
      (projectInsight.projectInsightMilestoneList == undefined || projectInsight.projectInsightMilestoneList == null || projectInsight?.projectInsightMilestoneList?.length == 0)) {
      this.alertMessage = `Please add at least one Project Question or Milestone to continue!!`;
      this.openAlertMod(template, this.alertMessage);
      flag = false;
      return false;
    }

    if (projectInsight.questionList?.length > 0) {
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
            if (!question.optionsList) {
              this.alertMessage = `Please provide options for Question ${index + 1} !!`;
              this.openAlertMod(template, this.alertMessage);
              flag = false;
              return false;
            } else if (question.optionsList?.length < 2) {
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
      });
    }

    if (projectInsight?.projectInsightMilestoneList?.length > 0) {
      projectInsight.projectInsightMilestoneList.forEach((milestone: ProjectMilestone, mileIndex) => {
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

        if (milestone.questionList != null && milestone.questionList?.length != 0) {
          flag = this.questionValidation(milestone.questionList, mileIndex, template, 'Milestone');
        }

        if (milestone.moduleList != null && milestone.moduleList?.length != 0) {
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

            if (module.questionList != null && module.questionList?.length != 0) {
              flag = this.questionValidation(module.questionList, modIndex, template, 'Module');
            }

            if (module.subModuleList != null && module.subModuleList?.length != 0) {
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

                if (submodule.questionList != null && submodule.questionList?.length != 0) {
                  flag = this.questionValidation(submodule.questionList, submodIndex, template, 'sub-module');
                }
              });
            }
          });
        }
      });
    }
    return flag;
  }

  questionValidation(questionList: ProjectQuestion[], parentIndex: any, template: TemplateRef<any>, parentType: any) {
    let flag = true;
    questionList.forEach((question: ProjectQuestion, index) => {
      if (!this.validationService.validateNullUndefinedEmptyString(question.question)) {
        this.alertMessage = `Please enter ${parentType}-${parentIndex + 1} Question ${index + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if (!this.validationService.validateNullUndefinedEmptyString(question.optionType)) {
        this.alertMessage = `Please select ${parentType}-${parentIndex + 1} Option Type ${index + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }

      if (question.optionType == "radio" || question.optionType == "checkbox") {
        if (!question.optionsList) {
          this.alertMessage = `Please provide options for ${parentType}-${parentIndex + 1} Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          flag = false;
          return false;
        } else if (question.optionsList?.length < 2) {
          this.alertMessage = `Please provide atleast 2 options for ${parentType}-${parentIndex + 1} Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          flag = false;
          return;
        } else {
          question.optionsList.forEach((option: SurveyOption, opIndex) => {
            if (!this.validationService.validateNullUndefinedEmptyString(option.optionValue)) {
              this.alertMessage = `Please enter option ${opIndex + 1} for ${parentType}-${parentIndex + 1} Question ${index + 1} !!`;
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

  // APIs
  getAllProjects() {
    this.allProjectList = [];
    this.projectService.getAllProjects().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectList = response.serviceResponse;
        this.allProjectList.forEach(project => {
          project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          project.updatedOn = (project.updatedOn) ? moment(project.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
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

  getEmployeeListByProjectId(projectId: any): Promise<any> {
    this.employeeList = [];
    const formData = new FormData();
    this.projectId = projectId;
    formData.append("projectId", this.projectId);
    return this.employeeService.getAllEmployeesByProjectId(formData).pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus == "Success") {
          this.employeeList = response.serviceResponse;
          this.employeeList = this.employeeList.filter(x => x.employmentstatus != 'InActive');
          this.getProjectManangerInfo(this.projectId);
        } else {
          console.error(response.serviceResponse)
        }
      })
      .catch(error => {
        this.openAlertMod(this.alertModal, 'Error fetching Employee List');
      });
  }

  getEmployeeList(): Promise<any> {
    this.allEmployeeList = [];
    return this.employeeService.getAllEmployees().pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;
          this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
        } else {
          console.error(response.serviceResponse)
        }
      })
      .catch(error => {
        this.openAlertMod(this.alertModal, 'Error fetching Employee List');
      });
  }

  saveProjectInsightConfiguration(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateProjectInsight(template, this.projectInsightObj);
    if (!inputValidated) return;

    if (this.projectInsightObj.questionList != null && this.projectInsightObj.questionList?.length != 0) {
      this.projectInsightObj.questionList.forEach((questionObj: ProjectQuestion) => {
        questionObj.options = JSON.stringify(questionObj.optionsList);
      });
    }

    this.projectInsightObj.projectInsightMilestoneList.forEach((proj: ProjectMilestone) => {
      if (proj.questionList != null && proj.questionList?.length != 0) {
        proj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }

      if (proj.moduleList != null && proj.moduleList?.length != 0) {
        proj.moduleList.forEach((moduleObj: any) => {

          if (moduleObj.questionList != null && moduleObj.questionList?.length != 0) {
            moduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
              questionObj.options = JSON.stringify(questionObj.optionsList);
            });
          }

          if (moduleObj.subModuleList != null && moduleObj.subModuleList?.length != 0) {
            this.convertJSONToStringSubModuleNodesOption(moduleObj.subModuleList);
          }
        });
      }
    });

    this.projectInsightObj.createdBy = this.currentUser.empId;
    this.projectInsightService.createProjectInsightQuestion(this.projectInsightObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsightsConfigList();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  updateProjectInsightConfiguration(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateProjectInsight(template, this.projectInsightObj);
    if (!inputValidated) return;

    if (this.projectInsightObj.questionList != null && this.projectInsightObj.questionList?.length != 0) {
      this.projectInsightObj.questionList.forEach((questionObj: ProjectQuestion) => {
        questionObj.options = JSON.stringify(questionObj.optionsList);
      });
    }

    this.projectInsightObj.projectInsightMilestoneList.forEach((proj: ProjectMilestone) => {
      if (proj.questionList != null && proj.questionList?.length != 0) {
        proj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }

      if (proj.moduleList != null && proj.moduleList?.length != 0) {
        proj.moduleList.forEach((moduleObj: any) => {
          if (moduleObj.questionList != null && moduleObj.questionList?.length != 0) {
            moduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
              questionObj.options = JSON.stringify(questionObj.optionsList);
            });
          }

          if (moduleObj.subModuleList != null && moduleObj.subModuleList?.length != 0) {
            this.convertJSONToStringSubModuleNodesOption(moduleObj.subModuleList);
          }
        });
      }
    });

    this.projectInsightObj.updatedBy = this.currentUser.empId;
    this.projectInsightService.updateProjectInsightQuestion(this.projectInsightObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showProjectInsightsConfigList();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllProjectQuestionsByProjectId(template?: TemplateRef<any>): Promise<any> {
    let projectInsight: ProjectInsight = new ProjectInsight();
    projectInsight.projectId = this.projectId;
    return this.projectInsightService.getAllQuestionsByProjectId(projectInsight).pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.projectInsightObj = response.serviceResponse;
          this.projectInsightObj.deletedProjectInsightEntityList = [];
          this.parseOptionsOfQuestionToList(this.projectInsightObj.questionList);
          if (this.projectInsightObj.projectInsightMilestoneList != null && this.projectInsightObj.projectInsightMilestoneList?.length != 0) {
          this.projectInsightObj.projectInsightMilestoneList.forEach((proj: ProjectMilestone) => {
            this.parseOptionsOfQuestionToList(proj.questionList);

            if (proj.moduleList != null && proj.moduleList?.length != 0) {
              proj.moduleList.forEach((moduleObj: any) => {
                this.parseOptionsOfQuestionToList(moduleObj.questionList);

                if (moduleObj.subModuleList != null && moduleObj.subModuleList?.length != 0) {
                  this.convertStringToJSONSubModuleNodesOption(moduleObj.subModuleList);
                }
              });
            }
          });
        }

          this.addAllBreadCrumbsToList();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      })
      .catch(error => {
        this.openAlertMod(template, 'Error fetching project questions');
      });
  }

  saveProjectInsightResponse(template: TemplateRef<any>, finalSubmit: any) {
    let files: File[] = [];

    if (this.projectInsightObj.questionList?.length) {
      let applicationQuestionList = this.projectInsightObj.questionList;
      for (let index = 0; index < applicationQuestionList?.length; index++) {
        let question = applicationQuestionList[index];
        this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
      }
    }

    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightObj.projectInsightMilestoneList?.length; milestoneIndex++) {
      let milestone = this.projectInsightObj.projectInsightMilestoneList[milestoneIndex];
      if (milestone.questionList?.length) {
        let questionList = milestone.questionList;
        for (let index = 0; index < questionList?.length; index++) {
          let question = questionList[index];
          this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];
        if (module.questionList?.length) {
          let moduleQuestionList = module.questionList;
          for (let index = 0; index < moduleQuestionList?.length; index++) {
            let question = moduleQuestionList[index];
            this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
          }
        }
        this.convertSubModuleListResponseAndRenameFile(module.subModuleList, files);
      }
    }

    this.projectInsightObj.empId = this.currentUser.empId;
    this.projectInsightObj.responseBy = this.currentUser.empId;
    this.projectInsightObj.isFinalSubmitted = finalSubmit ? 'Y' : 'N';
    this.projectInsightService.saveProjectInsightResponse(this.projectInsightObj, files).pipe(first()).subscribe((response: any) => {
      this.closeProjectInsightResponseModal();
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllProjectInsightResponsesByProjectId(projectId: any, empId: any, alertTemplate: TemplateRef<any>, performanceTabName: any, isPreview: any, type?: any): Promise<void> {
    this.isResponsePreview = isPreview;
    this.isResponsePointFinalSubmitted = true;
    this.projectInsightObj = new ProjectInsight();
    let projObj = new ProjectInsight();
    projObj.empId = empId
    projObj.projectId = projectId;
    projObj.employeeRole = this.currentUser.employeeRole;
    projObj.performanceTabName = performanceTabName;

    return this.projectInsightService.getAllProjectInsightResponsesByProjectId(projObj).pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus == "Success") {
          this.projectInsightObj = response.serviceResponse;
          this.projectInsightObj.badgePathList = this.getBadgePathList('Project');
          this.projectInsightObj.currentActiveBadgeLevel = 'Details';
          this.projectInsightObj.toTagEmployeeList = this.getToTagEmployeeList(this.projectInsightObj.assignedToUserId);

          if (this.projectInsightObj.questionList != null && this.projectInsightObj.questionList?.length != 0) {
            this.projectInsightObj.questionList.forEach((question: ProjectQuestion, index) => {
              question.badgePathList = this.getBadgePathList('Question');
              question.currentActiveBadgeLevel = 'Details';
              question.toTagEmployeeList = this.getToTagEmployeeList(this.projectInsightObj.assignedToUserId);
              this.mapQuestionsResponseList(question.projectResponseList, question.optionType, question.options, question.recommendedResponseId);
            });
          }

          this.projectInsightObj.projectInsightMilestoneList.forEach((milestone: ProjectMilestone, mileIndex) => {
            milestone.badgePathList = this.getBadgePathList('Milestone');
            milestone.currentActiveBadgeLevel = 'Details';
            milestone.toTagEmployeeList = this.getToTagEmployeeList(milestone.assignedToUserId);
            if (milestone.questionList != null && milestone.questionList?.length != 0) {
              milestone.questionList.forEach((question: ProjectQuestion, index) => {
                question.badgePathList = this.getBadgePathList('Question');
                question.currentActiveBadgeLevel = 'Details';
                question.toTagEmployeeList = this.getToTagEmployeeList(milestone.assignedToUserId);
                this.mapQuestionsResponseList(question.projectResponseList, question.optionType, question.options, question.recommendedResponseId);
              });
            }

            if (milestone.moduleList != null && milestone.moduleList?.length != 0) {
              milestone.moduleList.forEach((module: any, modIndex) => {
                module.badgePathList = this.getBadgePathList('Module');
                module.currentActiveBadgeLevel = 'Details';
                module.toTagEmployeeList = this.getToTagEmployeeList(module.assignedToUserId);
                if (module.questionList != null && module.questionList?.length != 0) {
                  module.questionList.forEach((question: ProjectQuestion, index) => {
                    question.badgePathList = this.getBadgePathList('Question');
                    question.currentActiveBadgeLevel = 'Details';
                    question.toTagEmployeeList = this.getToTagEmployeeList(module.assignedToUserId);
                    this.mapQuestionsResponseList(question.projectResponseList, question.optionType, question.options, question.recommendedResponseId);
                  });
                }

                if (module.subModuleList != null && module.subModuleList?.length != 0) {
                  this.createSubModuleListObject(module.subModuleList, "SubModule");
                }
              });
            }
          });
        } else {
          this.openAlertMod(alertTemplate, response.serviceResponse);
        }
        this.addAllBreadCrumbsToList();
      })
      .catch(error => {
        this.openAlertMod(alertTemplate, 'Error fetching project questions');
      });
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

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.alertMessage = message;
    this.bsModalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  cancelRequest() {
    this.bsModalRef.hide();
  }

  closeProjectInsightResponseModal() {
    this.closeProjectInsightResponseModalEvent.emit();
  }

  closeSearchResponsePreview() {
    this.closeSearchResponsePreviewEvent.emit();
  }

  closeDocumentPreviewTemplate() {
    this.documentPreviewModalRef.hide();
  }

  checkIfUserIdIsPresentInDisplayResponseUserId(response: any): boolean {
    console.log(response)
    return this.displayedResponseUserId?.includes(response?.responseBy);
  }

  isAllSelected(): boolean {
    return this.displayedResponseUserId?.length === this.employeeList.length;
  }

  toggleSelectAll(): void {
    if (this.isAllSelected()) {
      this.displayedResponseUserId = [];
    } else {
      this.displayedResponseUserId = this.employeeList.map(emp => emp.empId);
    }
  }

  saveReviewRemarks(isDraft: any, transferToKnowledgeHub: any) {
    let files: File[] = [];

    if (this.projectInsightObj.questionList?.length) {
      let applicationQuestionList = this.projectInsightObj.questionList;
      for (let index = 0; index < applicationQuestionList?.length; index++) {
        let question = applicationQuestionList[index];
        this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
      }
    }

    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightObj.projectInsightMilestoneList?.length; milestoneIndex++) {
      let milestone = this.projectInsightObj.projectInsightMilestoneList[milestoneIndex];
      if (milestone.questionList?.length) {
        let questionList = milestone.questionList;
        for (let index = 0; index < questionList?.length; index++) {
          let question = questionList[index];
          this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];
        if (module.questionList?.length) {
          let moduleQuestionList = module.questionList;
          for (let index = 0; index < moduleQuestionList?.length; index++) {
            let question = moduleQuestionList[index];
            this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
          }
        }
        this.convertSubModuleListResponseAndRenameFile(module.subModuleList, files);
      }
    }

    this.projectInsightObj.pointsBy = this.currentUser.empId;
    this.projectInsightObj.isPointsDrafted = isDraft ? 'Y' : 'N';
    this.projectInsightObj.transferToKnowledgeHub = transferToKnowledgeHub ? 'Y' : 'N'
    this.projectInsightService.saveReviewPoints(this.projectInsightObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.closeProjectInsightResponseModal();
        this.openAlertMod(this.alertModal, response.serviceResponse);
      } else {
        this.openAlertMod(this.alertModal, response.serviceResponse);
      }
    });
  }

  onRecommendedResponseChange(event: any, response: any, entity: any) {
    if (event.target.checked) {
      response.isRecommendedChecked = true;
    }
    entity?.projectResponseList.forEach((responseObj) => {
      if (responseObj.projectInsightResponseId != response.projectInsightResponseId) {
        responseObj.isRecommendedChecked = false;
      }
    });
    entity.recommendedResponseId = response?.projectInsightResponseId;
  }
}
