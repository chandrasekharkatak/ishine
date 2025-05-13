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
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import * as XLSX from 'xlsx';
import { ProjectInsightImportExportService } from 'src/app/services/project-insight-import-export.service';
import { DomainService } from 'src/app/services/domain.service';

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
  @Input() isExcelUploaded: boolean;
  @Input() projectInsightExcelObj: ProjectInsight;
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
  tagList: any[] = [];
  allParadigmList:any[]=[];

  // Variables
  alertMessage: any = '';
  viewType: any = 'Project';
  selectAllValue = -1;
  file: any;
  fileName: any;

  showContextMenu = false;
  contextMenuX = 0;
  contextMenuY = 0;
  selectedText = '';
  newTag: string = '';

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
    placeholder: 'Enter Response here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    uploadWithCredentials: false,
    sanitize: false,
    toolbarPosition: 'top',
    fonts: [{ class: 'arial', name: 'Arial' }],
    toolbarHiddenButtons: [
      [
        'insertImage',
        'insertVideo'
      ]
    ]
  };

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private projectService: ProjectService,
    private employeeService: EmployeeService,
    private exportExcelService: ExportExcelService,
    private projectInsightImportExportService: ProjectInsightImportExportService,
    private domainService:DomainService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  async ngOnInit(): Promise<void> {
    this.isCurrentEmployeeRoleGreaterThanManager = this.rolesGreaterThanManager.includes(this.currentUser?.employeeRole);
    await this.getAllProjects();
    await this.getAllDomain();

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
      if (this.isExcelUploaded && this.projectInsightExcelObj) {
        this.projectInsightObj = this.projectInsightExcelObj;
        await this.getEmployeeListByProjectId(this.projectInsightExcelObj?.projectId);
      }
      this.addAllBreadCrumbsToList();
      temp = this.allBreadCrumbs.filter(entity => { if (entity.entityType === 'Project') return entity });
      if (!temp[0].entityName) {
        temp[0].entityName = 'Project Name';
      }
    }
    else if (this.actionType == 'Configuration' && this.subActionType == 'Updation') {
      if (this.isExcelUploaded && this.projectInsightExcelObj) {
        this.projectInsightObj = this.projectInsightExcelObj;
        await this.getEmployeeListByProjectId(this.projectInsightExcelObj?.projectId);
        this.addAllBreadCrumbsToList();
      } else {
        await this.getAllProjectQuestionsByProjectId(this.alertMessage);
      }
      temp = this.allBreadCrumbs.filter(entity => { if (entity.entityType === 'Project') return entity });
    }
    else if (this.actionType == 'Contribution' && (this.subActionType == 'Submit/View Response')) {
      if (this.isExcelUploaded && this.projectInsightExcelObj) {
        this.projectInsightObj = this.projectInsightExcelObj;
        this.mapEntityBadgeDetails();
        this.addAllBreadCrumbsToList();
      } else {
        await this.getAllProjectInsightResponsesByProjectId(this.projectId, this.currentUser.empId, this.alertModal, 'Performance Dashboard', false);
      }
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
        this.projectInsightObj.badgePathList = this.getBadgePathList('Project', this.projectInsightObj);
      }
      if (this.isExcelUploaded && this.projectInsightObj.badgePathList) {
        this.projectInsightObj.badgePathList.forEach((badge)=>{
          if(badge.name == 'Questions'){
            badge.isUpdatedFromExcelUpload = this.computeFlagsForQuestions(this.projectInsightObj?.questionList);
          } else if(badge.name == 'Milestones'){
            badge.isUpdatedFromExcelUpload =  this.computeFlagsForMilestones(this.projectInsightObj?.projectInsightMilestoneList);
          }
        })
      }
      this.projectInsightObj.currentActiveBadgeLevel = 'Details';
      if (this.projectInsightObj.questionList) {
        this.projectInsightObj.questionList.forEach((question: ProjectQuestion, index) => {
          question.badgePathList = this.getBadgePathList('Question', question);
          question.currentActiveBadgeLevel = 'Details';
        });
      }

      if (this.projectInsightObj.projectInsightMilestoneList != null && this.projectInsightObj.projectInsightMilestoneList?.length != 0) {
        this.projectInsightObj.projectInsightMilestoneList.forEach((projectMilestone: ProjectMilestone) => {
          this.allBreadCrumbs.push(this.createBreadCrumbObj('Milestone', projectMilestone.milestoneId, projectMilestone, projectMilestone.milestone));
          if (!projectMilestone.badgePathList) {
            projectMilestone.badgePathList = this.getBadgePathList('Milestone', projectMilestone);
            projectMilestone.currentActiveBadgeLevel = 'Details';
          }
          if (projectMilestone.questionList) {
            projectMilestone.questionList.forEach((question: ProjectQuestion, index) => {
              question.badgePathList = this.getBadgePathList('Question', question);
              question.currentActiveBadgeLevel = 'Details';
            });
          }
          if (projectMilestone.moduleList != null && projectMilestone.moduleList?.length != 0) {
            projectMilestone.moduleList.forEach((moduleObj: ProjectModule) => {
              this.allBreadCrumbs.push(this.createBreadCrumbObj('Module', moduleObj.moduleId, moduleObj, moduleObj.module));

              if (!moduleObj.badgePathList) {
                moduleObj.badgePathList = this.getBadgePathList('Module', moduleObj);
                moduleObj.currentActiveBadgeLevel = 'Details';
              }
              if (moduleObj.questionList) {
                moduleObj.questionList.forEach((question: ProjectQuestion, index) => {
                  question.badgePathList = this.getBadgePathList('Question', question);
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

  addAllSubModuleBreadCrumbs(subModuleList: ProjectSubModule[], subModuleType: any) {
    subModuleList.forEach((submoduleObj: ProjectSubModule) => {
      this.allBreadCrumbs.push(this.createBreadCrumbObj(subModuleType, submoduleObj.subModuleId, submoduleObj, submoduleObj.subModule));
      if (!submoduleObj.badgePathList) {
        submoduleObj.badgePathList = this.getBadgePathList(subModuleType, submoduleObj, subModuleType);
        submoduleObj.currentActiveBadgeLevel = 'Details';
      }
      if (submoduleObj.questionList) {
        submoduleObj.questionList.forEach((question: ProjectQuestion, index) => {
          question.badgePathList = this.getBadgePathList('Question', question);
          question.currentActiveBadgeLevel = 'Details';
        });
      }
      if (this.isValidList(submoduleObj.subSubModuleList)) {
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
    this.file = null;
    this.fileName = null;
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
    this.entityObj.projectInsightMilestoneList = milestoneList;
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
    this.entityObj.moduleList = moduleList;
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
    this.entityObj.subSubModuleList = subModuleList;
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


  getBadgePathList(entityType: any, entity: any, subModuleType?: any) {
    if (entityType == 'Project') {
      return [
        { name: 'Details', isUpdatedFromExcelUpload: 'No' },
        { name: 'Questions', isUpdatedFromExcelUpload: this.computeFlagsForQuestions(entity?.questionList) },
        { name: 'Milestones', isUpdatedFromExcelUpload: this.computeFlagsForMilestones(entity?.projectInsightMilestoneList) },
      ];
    }
    else if (entityType == 'Milestone') {
      return [
        { name: 'Details', isUpdatedFromExcelUpload: 'No' },
        { name: 'Questions', isUpdatedFromExcelUpload: this.computeFlagsForQuestions(entity?.questionList) },
        { name: 'Modules', isUpdatedFromExcelUpload: this.computeFlagsForModules(entity?.moduleList) },
      ];
    }
    else if (entityType == 'Module') {
      return [
        { name: 'Details', isUpdatedFromExcelUpload: 'No' },
        { name: 'Questions', isUpdatedFromExcelUpload: this.computeFlagsForQuestions(entity?.questionList) },
        { name: 'SubModules', isUpdatedFromExcelUpload: this.computeFlagsForSubModules(entity?.subModuleList) },
      ];
    }
    else if (entityType == 'SubModule' || entityType == 'Sub-SubModule') {
      if (!subModuleType) {
        subModuleType = 'SubModule'
      }
      return [
        { name: 'Details', isUpdatedFromExcelUpload: 'No' },
        { name: 'Questions', isUpdatedFromExcelUpload: this.computeFlagsForQuestions(entity?.questionList) },
        { name: subModuleType + 's', isUpdatedFromExcelUpload: this.computeFlagsForSubModules(entity?.subSubModuleList) },
      ];
    }
    else if (entityType == 'Question') {
      return [
        { name: 'Details', isUpdatedFromExcelUpload: 'No' }
      ];
    }
  }

  getProjectManangerInfo(projectId: any) {
    this.allProjectList.forEach((object) => {
      if (object.projectId == projectId) {
        this.projectInsightObj.projectManagerName = object.employeeName;
        this.projectInsightObj.projectManagerId = object.empId;
        this.projectInsightObj.projectName = object.projectName;
        if (this.breadCrumbs[0]) {
          this.breadCrumbs[0].entityId = this.projectInsightObj.projectId;
          this.breadCrumbs[0].entityName = this.projectInsightObj.projectName;
        }
      }
    });
  }

  convertJSONToStringSubModuleNodesOption(subModuleList: ProjectSubModule[]) {
    subModuleList.forEach((submoduleObj: any) => {
      submoduleObj.toTagEmployeeList = [];
      if (this.isValidList(submoduleObj.questionList)) {
        submoduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
          questionObj.toTagEmployeeList = [];
          questionObj.options = JSON.stringify(questionObj.optionsList);
        });
      }
      if (this.isValidList(submoduleObj.subSubModuleList)) {
        this.convertJSONToStringSubModuleNodesOption(submoduleObj.subSubModuleList);
      }
    });
  }

  convertStringToJSONSubModuleNodesOption(subModuleList: ProjectSubModule[]) {
    subModuleList.forEach((submoduleObj: any) => {
      this.parseOptionsOfQuestionToList(submoduleObj.questionList);
      if (this.isValidList(submoduleObj.subSubModuleList)) {
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

  mapQuestionsResponseList(question: ProjectQuestion, optionType: any, options: any, recommendedResponseId: any, entityType: any, assignedToUserIds: any[]) {
    question.currentActiveBadgeLevel = 'Details';
    question.badgePathList = this.getBadgePathList(entityType, question);
    question.toTagEmployeeList = this.getToTagEmployeeList(assignedToUserIds);
    let projectResponseList: ProjectResponse[] = question?.projectResponseList || [];
    if (this.isValidList(projectResponseList)) {
      projectResponseList.forEach((response: ProjectResponse, index) => {
        if (recommendedResponseId != undefined && recommendedResponseId != null && response.projectInsightResponseId == recommendedResponseId) {
          response.isRecommendedChecked = true;
        }
        if (this.isValidList(response.projectInsightResponsePointList)) {
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
          if (this.isValidList(response.optionsList)) {
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

  mapEntityBadgeDetails() {
    this.projectInsightObj.badgePathList = this.getBadgePathList('Project', this.projectInsightObj);
    this.projectInsightObj.currentActiveBadgeLevel = 'Details';
    this.projectInsightObj.toTagEmployeeList = this.getToTagEmployeeList(this.projectInsightObj.assignedToUserId);
    if (this.isValidList(this.projectInsightObj.questionList)) {
      this.projectInsightObj.questionList.forEach((question: ProjectQuestion, index) => {
        this.mapQuestionsResponseList(question, question.optionType, question.options, question.recommendedResponseId, 'Question', this.projectInsightObj.assignedToUserId);
      });
    }

    this.projectInsightObj.projectInsightMilestoneList.forEach((milestone: ProjectMilestone, mileIndex) => {
      milestone.badgePathList = this.getBadgePathList('Milestone', milestone);
      milestone.currentActiveBadgeLevel = 'Details';
      milestone.toTagEmployeeList = this.getToTagEmployeeList(milestone.assignedToUserId);
      if (this.isValidList(milestone.questionList)) {
        milestone.questionList.forEach((question: ProjectQuestion, index) => {
          this.mapQuestionsResponseList(question, question.optionType, question.options, question.recommendedResponseId, 'Question', milestone.assignedToUserId);
        });
      }

      if (this.isValidList(milestone.moduleList)) {
        milestone.moduleList.forEach((module: any, modIndex) => {
          module.badgePathList = this.getBadgePathList('Module', module);
          module.currentActiveBadgeLevel = 'Details';
          module.toTagEmployeeList = this.getToTagEmployeeList(module.assignedToUserId);
          if (this.isValidList(module.questionList)) {
            module.questionList.forEach((question: ProjectQuestion, index) => {
              this.mapQuestionsResponseList(question, question.optionType, question.options, question.recommendedResponseId, 'Question', module.assignedToUserId);
            });
          }

          if (this.isValidList(module.subModuleList)) {
            this.createSubModuleListObject(module.subModuleList, "SubModule");
          }
        });
      }
    });
  }

  convertSubModuleListResponseAndRenameFile(subModuleList: ProjectSubModule[], files: any) {
    for (let submoduleIndex = 0; submoduleIndex < (subModuleList?.length || 0); submoduleIndex++) {
      subModuleList[submoduleIndex].toTagEmployeeList = [];
      let submodule = subModuleList[submoduleIndex];
      if (this.isValidList(submodule.questionList)) {
        for (let index = 0; index < submodule.questionList?.length; index++) {
          let question = submodule.questionList[index];
          question.toTagEmployeeList = [];
          this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
        }
      }
      if (this.isValidList(submodule.subSubModuleList)) {
        this.convertSubModuleListResponseAndRenameFile(submodule.subSubModuleList, files)
      }
    }
  }

  createSubModuleListObject(subModuleList: ProjectSubModule[], subModuleType: any) {
    subModuleList.forEach((submodule: any, submodIndex) => {
      submodule.badgePathList = this.getBadgePathList(subModuleType, submodule, subModuleType);
      submodule.currentActiveBadgeLevel = 'Details';
      submodule.toTagEmployeeList = this.getToTagEmployeeList(submodule.assignedToUserId);
      if (this.isValidList(submodule.questionList)) {
        submodule.questionList.forEach((question: ProjectQuestion, index) => {
          this.mapQuestionsResponseList(question, question.optionType, question.options, question.recommendedResponseId, 'Question', submodule.assignedToUserId);
        });
      }
      if (this.isValidList(submodule.subSubModuleList)) {
        this.createSubModuleListObject(submodule.subSubModuleList, "Sub-SubModule");
      }
    });
  }

  parseOptionsOfQuestionToList(questionList: ProjectQuestion[]) {
    if (this.isValidList(questionList)) {
      questionList.forEach((questionObj: ProjectQuestion) => {
        if (questionObj.optionType == "checkbox" || questionObj.optionType == "radio") {
          questionObj.optionsList = JSON.parse(questionObj.options || '[]');
        }
      });
    }
  }

  getToTagEmployeeList(assignedToUserId: any[]) {
    if (this.isValidList(assignedToUserId)) {
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

  //context menu
  handleContextMenu(event: MouseEvent) {
    event.preventDefault();
    const selection = window.getSelection();
    if (selection && selection.toString().trim().length > 0) {
      this.selectedText = selection.toString();

      // Cast event.target to HTMLElement
      const element = event.target as HTMLElement;
      const rect = element.getBoundingClientRect();

      // Get click coordinates
      this.contextMenuX = event.clientX;
      this.contextMenuY = event.clientY;

      // Ensure menu stays within viewport
      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;
      const menuWidth = 200; // Approximate width of context menu
      const menuHeight = 160; // Approximate height of context menu

      // Adjust if menu would go outside viewport
      if (this.contextMenuX + menuWidth > viewportWidth) {
        this.contextMenuX = viewportWidth - menuWidth;
      }

      if (this.contextMenuY + menuHeight > viewportHeight) {
        this.contextMenuY = viewportHeight - menuHeight;
      }
      this.showContextMenu = true;
    }
  }

  addTag(response: ProjectResponse) {
    if (this.selectedText && this.selectedText.trim()) {
      if (!response.tags) {
        response.tags = [];
      }
      if (!response.tags.includes(this.selectedText.trim())) {
        response.tags.push(this.selectedText.trim());
      }
      this.showContextMenu = false;
    }
  }

  addManualTag(response: ProjectResponse) {
    if (response.newTag && response.newTag.trim()) {
      if (!response.tags) {
        response.tags = [];
      }
      if (!response.tags.includes(response.newTag.trim())) {
        response.tags.push(response.newTag.trim());
      }
      response.newTag = '';
    }
  }

  removeTag(response: ProjectResponse, index: number) {
    if (response.tags) {
      response.tags.splice(index, 1);
    }
  }

  closeContextMenu() {
    this.showContextMenu = false;
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
      for (let index = 0; index < projectInsight.questionList?.length; index++) {
        let question = projectInsight.questionList[index];
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
      }
    }

    if (projectInsight?.projectInsightMilestoneList?.length > 0) {
      for (let mileIndex = 0; mileIndex < projectInsight.projectInsightMilestoneList?.length; mileIndex++) {
        let milestone = projectInsight.projectInsightMilestoneList[mileIndex];

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

        if (this.isValidList(milestone.questionList)) {
          flag = this.questionValidation(milestone.questionList, mileIndex, template, 'Milestone');
        }

        if (milestone.moduleList != null && milestone.moduleList?.length != 0) {
          milestone.moduleList.forEach((module: any, modIndex) => {
            if (!this.validationService.validateNullUndefinedEmptyString(module.module)) {
              this.alertMessage = `Please enter module ${modIndex + 1} !!`;
              this.openAlertMod(template, this.alertMessage);
              flag = false;
              return;
            }

            if (!this.validationService.validateNullUndefinedEmptyString(module.assignedToUserId)) {
              this.alertMessage = `Please select assign user in module ${modIndex + 1} !!`;
              this.openAlertMod(template, this.alertMessage);
              flag = false;
              return;
            }

            if (this.isValidList(module.questionList)) {
              flag = this.questionValidation(module.questionList, modIndex, template, 'Module');
            }

            if (this.isValidList(module.subModuleList)) {
              module.subModuleList.forEach((submodule: any, submodIndex) => {
                if (!this.validationService.validateNullUndefinedEmptyString(submodule.subModule)) {
                  this.alertMessage = `Please enter sub-module ${submodIndex + 1} !!`;
                  this.openAlertMod(template, this.alertMessage);
                  flag = false;
                  return;
                }

                if (!this.validationService.validateNullUndefinedEmptyString(submodule.assignedToUserId)) {
                  this.alertMessage = `Please select assign user in sub-module ${submodIndex + 1} !!`;
                  this.openAlertMod(template, this.alertMessage);
                  flag = false;
                  return;
                }

                if (this.isValidList(submodule.questionList)) {
                  flag = this.questionValidation(submodule.questionList, submodIndex, template, 'sub-module');
                }
              });
            }
          });
        }
      }
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

  isExcelValidationSuccess(validationResult: any): boolean {
    return validationResult && validationResult === 'Success';
  }

  isValidList(list: any[]): boolean {
    return this.validationService.validateNullUndefinedEmptyList(list);
  }

  // APIs
  async getAllDomain(): Promise<any> {
    this.allParadigmList = [];
    return this.domainService.getAllDomain().pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allParadigmList = response.serviceResponse;
          this.allParadigmList.forEach((domain) => {
            domain.createdOn = (domain.createdOn) ? moment(domain.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
        } else {
          this.openAlertMod(this.alertModal, 'Something went wrong.');
        }
      })
      .catch(error => {
        console.log(error);
        this.openAlertMod(this.alertModal, 'Error fetching Domain List');
      });
    ;
  }

  getAllProjects() {
    this.allProjectList = [];
    return this.projectService.getAllProjects().pipe(first())
      .toPromise()
      .then((response: any) => {
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
      })
      .catch(error => {
        this.openAlertMod(this.alertModal, 'Error fetching Project List');
      });
    ;
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

    this.projectInsightObj.toTagEmployeeList = [];
    if (this.isValidList(this.projectInsightObj.questionList)) {
      this.projectInsightObj.questionList.forEach((questionObj: ProjectQuestion) => {
        questionObj.toTagEmployeeList = [];
        questionObj.options = JSON.stringify(questionObj.optionsList);
      });
    }
    if (this.isValidList(this.projectInsightObj.projectInsightMilestoneList)) {
      this.projectInsightObj.projectInsightMilestoneList.forEach((proj: ProjectMilestone) => {
        proj.toTagEmployeeList = [];
        if (proj.questionList != null && proj.questionList?.length != 0) {
          proj.questionList.forEach((questionObj: ProjectQuestion) => {
            questionObj.toTagEmployeeList = [];
            questionObj.options = JSON.stringify(questionObj.optionsList);
          });
        }

        if (proj.moduleList != null && proj.moduleList?.length != 0) {
          proj.moduleList.forEach((moduleObj: any) => {
            moduleObj.toTagEmployeeList = [];
            if (moduleObj.questionList != null && moduleObj.questionList?.length != 0) {
              moduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
                questionObj.toTagEmployeeList = [];
                questionObj.options = JSON.stringify(questionObj.optionsList);
              });
            }

            if (moduleObj.subModuleList != null && moduleObj.subModuleList?.length != 0) {
              this.convertJSONToStringSubModuleNodesOption(moduleObj.subModuleList);
            }
          });
        }
      });
    }

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

    if (this.isValidList(this.projectInsightObj.questionList)) {
      this.projectInsightObj.questionList.forEach((questionObj: ProjectQuestion) => {
        questionObj.options = JSON.stringify(questionObj.optionsList);
      });
    }

    if (this.isValidList(this.projectInsightObj.projectInsightMilestoneList)) {
      this.projectInsightObj.projectInsightMilestoneList.forEach((proj: ProjectMilestone) => {
        if (proj.questionList != null && proj.questionList?.length != 0) {
          proj.questionList.forEach((questionObj: ProjectQuestion) => {
            questionObj.options = JSON.stringify(questionObj.optionsList);
          });
        }

        if (this.isValidList(proj.moduleList)) {
          proj.moduleList.forEach((moduleObj: any) => {
            if (moduleObj.questionList != null && moduleObj.questionList?.length != 0) {
              moduleObj.questionList.forEach((questionObj: ProjectQuestion) => {
                questionObj.options = JSON.stringify(questionObj.optionsList);
              });
            }

            if (this.isValidList(moduleObj.subModuleList)) {
              this.convertJSONToStringSubModuleNodesOption(moduleObj.subModuleList);
            }
          });
        }
      });
    }

    this.projectInsightObj.updatedBy = this.currentUser.empId;
    this.projectInsightObj.isExcelUploaded = this.isExcelUploaded;
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
          if (this.isValidList(this.projectInsightObj.projectInsightMilestoneList)) {
            this.projectInsightObj.projectInsightMilestoneList.forEach((proj: ProjectMilestone) => {
              this.parseOptionsOfQuestionToList(proj.questionList);

              if (this.isValidList(proj.moduleList)) {
                proj.moduleList.forEach((moduleObj: any) => {
                  this.parseOptionsOfQuestionToList(moduleObj.questionList);

                  if (this.isValidList(moduleObj.subModuleList)) {
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

    if (this.isValidList(this.projectInsightObj.questionList)) {
      let applicationQuestionList = this.projectInsightObj.questionList;
      for (let index = 0; index < applicationQuestionList?.length; index++) {
        let question = applicationQuestionList[index];
        this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
      }
    }

    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightObj.projectInsightMilestoneList?.length; milestoneIndex++) {
      let milestone = this.projectInsightObj.projectInsightMilestoneList[milestoneIndex];
      if (this.isValidList(milestone.questionList)) {
        let questionList = milestone.questionList;
        for (let index = 0; index < questionList?.length; index++) {
          let question = questionList[index];
          this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        let module = milestone.moduleList[moduleIndex];
        if (this.isValidList(module.questionList)) {
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
          this.mapEntityBadgeDetails();
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

    this.projectInsightObj.toTagEmployeeList = [];
    if (this.projectInsightObj.questionList?.length) {
      for (let index = 0; index < this.projectInsightObj.questionList?.length; index++) {
        let question = this.projectInsightObj.questionList[index];
        question.toTagEmployeeList = [];
        this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
      }
    }

    for (let milestoneIndex = 0; milestoneIndex < this.projectInsightObj.projectInsightMilestoneList?.length; milestoneIndex++) {
      this.projectInsightObj.projectInsightMilestoneList[milestoneIndex].toTagEmployeeList = [];
      let milestone = this.projectInsightObj.projectInsightMilestoneList[milestoneIndex];
      if (milestone.questionList?.length) {
        for (let index = 0; index < milestone.questionList?.length; index++) {
          let question = milestone.questionList[index];
          question.toTagEmployeeList = [];
          this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
        }
      }

      for (let moduleIndex = 0; moduleIndex < (milestone.moduleList?.length || 0); moduleIndex++) {
        milestone.moduleList[moduleIndex].toTagEmployeeList = [];
        let moduleObj = milestone.moduleList[moduleIndex];
        if (moduleObj.questionList?.length) {
          for (let index = 0; index < moduleObj.questionList?.length; index++) {
            let question = moduleObj.questionList[index];
            question.toTagEmployeeList = [];
            this.mapQuestionResponseAndFile(question.projectResponseList, question.optionType, files);
          }
        }
        this.convertSubModuleListResponseAndRenameFile(moduleObj.subModuleList, files);
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

  exportProjectToExcel(entity: any, entityType: any, name: any, parentId: any, currentActiveBadgeLevel?: any) {
    let parentType = entityType
    if (currentActiveBadgeLevel && currentActiveBadgeLevel == 'Details') {
      name += '_Details';
      entityType = 'Details';
      this.projectInsightImportExportService.exportEntityDetailOrQuestionsToExcel(entity, entityType, name, parentId, parentType);
    }
    else if (currentActiveBadgeLevel && currentActiveBadgeLevel == 'Questions') {
      name += '_Question';
      entityType = 'Question';
      entity = entity?.questionList;
      this.projectInsightImportExportService.exportEntityDetailOrQuestionsToExcel(entity, entityType, name, parentId, parentType);
    }
    else {
      this.projectInsightImportExportService.exportProjectInsightToExcel(entity, entityType, name, parentId);
    }
  }

  clearFileInput(): void {
    this.file = null;
    this.fileName = null;
    this.projectInsightExcelObj = new ProjectInsight();
    const fileInput = document.getElementById('project-data-input-file') as HTMLInputElement;
    if (fileInput)
      fileInput.value = '';
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.file = file;
      this.fileName = file.name;
      const fileExtension = this.fileName.split(".").pop();
      let elem = document.getElementById('project-data-input-file') as HTMLInputElement;
      if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
        this.alertMessage = "Only .xlsx file is allowed.";
        this.bsModalRef = this.modalService.show(this.alertModal, { class: 'modal-sm' });
        this.clearFileInput();
        return false;
      }
    } else {
      this.clearFileInput();
    }
  }

  async uploadXcelData(entityType2: any, entity: any, entityList?: any): Promise<any> {
    try {
      let entitType = await this.projectInsightImportExportService.getExcelSheetNames(this.file);
      if (!this.validationService.validateNullUndefinedEmptyString(entitType)) {
        entitType = entityType2;
      }

      await this.validateExcelFile(this.file, entitType, entity, entityList);
      if (entitType == 'Question') {
        let excelQuestionList = await this.projectInsightImportExportService.convertJsonDataToEntityQuestionList(this.file);
        if (excelQuestionList) {
          entity.questionList = await this.mergeQuestions(entity.questionList, excelQuestionList);
        }
      }
      if (entitType == 'Milestone') {
        let projectInsightExcelObj = await this.projectInsightImportExportService.convertJsonDataToProjectInsightObj(this.file);
        if (projectInsightExcelObj) {
          const newEntityList = await this.mergeMilestones(entityList, projectInsightExcelObj?.projectInsightMilestoneList);
          this.projectInsightObj.projectInsightMilestoneList = newEntityList;
        }
      }

      if (entitType == 'Module') {
        let projectInsightModuleList = await this.projectInsightImportExportService.convertJsonDataToModuleList(this.file);
        if (projectInsightModuleList) {
          if (this.isValidList(projectInsightModuleList) && this.isValidList(this.projectInsightObj.projectInsightMilestoneList)) {
            for (let milestone of this.projectInsightObj.projectInsightMilestoneList) {
              let filteredModuleList = projectInsightModuleList.filter(moduleObj => moduleObj.milestoneId == milestone.milestoneId);
              milestone.moduleList = await this.mergeModules(milestone?.moduleList, filteredModuleList);
            }
          }
        }
      }

      if (entitType == 'SubModule' || entitType == 'Sub-SubModule') {
        if (entitType == 'SubModule') {
          let projectInsightSubModuleList = await this.projectInsightImportExportService.convertJsonDataToSubModuleList(this.file);
          if (projectInsightSubModuleList) {
            if (this.isValidList(projectInsightSubModuleList) && this.isValidList(this.projectInsightObj.projectInsightMilestoneList)) {
              for (let milestone of this.projectInsightObj.projectInsightMilestoneList) {
                if (this.isValidList(milestone.moduleList)) {
                  for (let moduleObj of milestone.moduleList) {
                    let filteredSubModuleList = projectInsightSubModuleList.filter(subModule => subModule.moduleId == moduleObj.moduleId);
                    moduleObj.subModuleList = await this.mergeSubModules(moduleObj?.subModuleList, filteredSubModuleList, "SubModule");
                  }
                }
              }
            }
          }
        } else if (entitType == 'Sub-SubModule') {
          let projectInsightSubSubModuleList = await this.projectInsightImportExportService.convertJsonDataToSubSubModuleList(this.file);
          if (projectInsightSubSubModuleList) {
            if (this.isValidList(projectInsightSubSubModuleList) && this.isValidList(this.projectInsightObj.projectInsightMilestoneList)) {
              this.getSubSubModuleList(projectInsightSubSubModuleList, this.projectInsightObj.projectInsightMilestoneList);
            }
          }
        }
      }

      if (entitType == 'Module' || entitType == 'SubModule' || entitType == 'Sub-SubModule') {
        let entity = this.projectInsightObj?.projectInsightMilestoneList[0];
        if (entity) {
          entity.currentActiveBadgeLevel = 'Details';
          const existingIndex = this.breadCrumbs.findIndex(b => (b.entityId == entity?.milestoneId));
          if (existingIndex !== -1) {
            this.breadCrumbs = this.breadCrumbs.slice(0, existingIndex + 1);
          }
        }
      }
      this.clearFileInput();
    } catch (error) {
      console.log(error);
      this.openAlertMod(this.alertModal, "Failed to process Excel file");
      return false;
    }
  }

  async validateExcelFile(file: any, entitType: any, entity: any, entityList?: any): Promise<any> {
    let excelValidated: any;
    if (entitType == 'Question') {
      let entityId: any = null;
      if (this.validationService.validateNullUndefinedEmptyList(entity?.questionList)) {
        entityId = entity?.questionList[0]?.entityId;
      }
      excelValidated = await this.projectInsightImportExportService.validateProjectInsightEntityImportFromExcel(file, entitType, entityId);
    }

    if (entitType == 'Milestone') {
      let entityId: any = null;
      if (this.validationService.validateNullUndefinedEmptyList(entityList)) {
        entityId = entityList[0]?.projectId;
      }
      excelValidated = await this.projectInsightImportExportService.validateProjectInsightEntityImportFromExcel(file, entitType, entityId);
    }

    if (entitType == 'Module') {
      excelValidated = await this.projectInsightImportExportService.validateProjectInsightEntityImportFromExcel(file, entitType);
    }

    if (entitType == 'SubModule' || entitType == 'Sub-SubModule') {
      excelValidated = await this.projectInsightImportExportService.validateProjectInsightEntityImportFromExcel(file, entitType);
    }

    if (!excelValidated || excelValidated != 'Success') {
      let alertMessage = 'Something went wrong';
      if (excelValidated) {
        alertMessage = excelValidated;
      }
      this.openAlertMod(this.alertModal, alertMessage);
      return false;
    }
  }

  async mergeMilestones(dbList: any[], excelList: any[]): Promise<any> {
    try {
      if (!this.isValidList(dbList)) {
        return this.isValidList(excelList) ? excelList : [];
      }
      if (!this.isValidList(excelList)) {
        return dbList;
      }

      for (const excelMilestone of excelList) {
        const dbMilestone = dbList.find(milestone => milestone.milestoneId === excelMilestone.milestoneId && excelMilestone.actionType != 'Add');
        if (dbMilestone) {
          dbMilestone.milestone = excelMilestone.milestone;
          dbMilestone.description = excelMilestone.description;
          dbMilestone.projectId = excelMilestone.projectId;
          dbMilestone.assignedToUserId = excelMilestone.assignedToUserId;
          dbMilestone.redmineId = excelMilestone.redmineId;
          dbMilestone.actionType = excelMilestone.actionType;
          if (!this.isValidList(excelMilestone?.questionList)) {
            excelMilestone.questionList = [];
          }
          dbMilestone.questionList = await this.mergeQuestions(dbMilestone?.questionList, excelMilestone?.questionList);
          dbMilestone.moduleList = await this.mergeModules(dbMilestone?.moduleList, excelMilestone?.moduleList);
        } else {
          dbList.push(excelMilestone);
        }
      }
      return dbList;
    } catch (error) {
      throw error;
    }
  }

  async mergeModules(dbList: any[], excelList: any[]): Promise<any> {
    try {
      if (!this.isValidList(dbList)) {
        return this.isValidList(excelList) ? excelList : [];
      }
      if (!this.isValidList(excelList)) {
        return dbList;
      }

      for (const excelModule of excelList) {
        const dbModule = dbList.find(module => module.moduleId === excelModule.moduleId && excelModule.actionType != 'Add');
        if (dbModule) {
          dbModule.module = excelModule.module;
          dbModule.description = excelModule.description;
          dbModule.milestoneId = excelModule.milestoneId;
          dbModule.assignedToUserId = excelModule.assignedToUserId;
          dbModule.redmineId = excelModule.redmineId;
          dbModule.actionType = excelModule.actionType;
          if (!this.isValidList(excelModule?.questionList)) {
            excelModule.questionList = [];
          }
          dbModule.questionList = await this.mergeQuestions(dbModule?.questionList, excelModule?.questionList);
          dbModule.subModuleList = await this.mergeSubModules(dbModule?.subModuleList, excelModule?.subModuleList, 'SubModule');
        } else {
          dbList.push(excelModule);
        }
      }
      return dbList;
    } catch (error) {
      throw error;
    }
  }

  async mergeSubModules(dbList: any[], excelList: any[], subModuleType: any): Promise<any> {
    try {
      if (!this.isValidList(dbList)) {
        return this.isValidList(excelList) ? excelList : [];
      }
      if (!this.isValidList(excelList)) {
        return dbList;
      }

      for (const excelSubModule of excelList) {
        const dbSubModule = dbList.find(subModule => subModule.subModuleId === excelSubModule.subModuleId && excelSubModule.actionType != 'Add');
        if (dbSubModule) {
          dbSubModule.subModule = excelSubModule.subModule;
          dbSubModule.description = excelSubModule.description;
          dbSubModule.moduleId = excelSubModule.moduleId;
          dbSubModule.assignedToUserId = excelSubModule.assignedToUserId;
          dbSubModule.redmineId = excelSubModule.redmineId;
          excelSubModule.subModuleId = excelSubModule.subModuleId;
          dbSubModule.actionType = excelSubModule.actionType;
          if (!this.isValidList(excelSubModule?.questionList)) {
            excelSubModule.questionList = [];
          }
          dbSubModule.questionList = await this.mergeQuestions(dbSubModule?.questionList, excelSubModule?.questionList);

          if (this.isValidList(dbSubModule?.subSubModuleList) || this.isValidList(excelSubModule?.subSubModuleList)) {
            dbSubModule.subSubModuleList = await this.mergeSubSubModules(dbSubModule?.subSubModuleList, excelSubModule?.subSubModuleList, 'Sub-SubModule');
          }
        } else {
          dbList.push(excelSubModule);
        }
      }
      return dbList;
    } catch (error) {
      throw error;
    }
  }

  async getSubSubModuleList(projectInsightSubSubModuleList: any[], projectInsightMilestoneList: any[]): Promise<any> {
    for (const milestone of projectInsightMilestoneList) {
      if (this.validationService.validateNullUndefinedEmptyList(milestone?.moduleList)) {
        for (const module of milestone?.moduleList) {
          if (this.validationService.validateNullUndefinedEmptyList(module?.subModuleList)) {
            for (const subModule of module?.subModuleList) {
              if (this.validationService.validateNullUndefinedEmptyList(subModule?.subSubModuleList)) {
                subModule.subSubModuleList = await this.traverseAndMergeSubSubModules(subModule.subSubModuleList, projectInsightSubSubModuleList, subModule.subModuleId);
              }
            }
          }
        }
      }
    }
  }

  async traverseAndMergeSubSubModules(dbSubSubModuleList: any, projectInsightSubSubModuleList: any[], moduleIdObj: any): Promise<any> {
    // if (this.validationService.validateNullUndefinedEmptyList(dbSubSubModuleList)) {
    //   for (const subSubModule of dbSubSubModuleList) {
    //     subSubModule.subSubModuleList = await this.traverseAndMergeSubSubModules(subSubModule?.subSubModuleList, projectInsightSubSubModuleList, subSubModule.moduleId);
    //   }
    // }
    const filteredModuleList = projectInsightSubSubModuleList.filter(subModuleObj => moduleIdObj == subModuleObj.moduleId);
    dbSubSubModuleList = await this.mergeSubSubModules(dbSubSubModuleList, filteredModuleList, "Sub-SubModule");
    return dbSubSubModuleList;
  }

  async mergeSubSubModules(dbList: any[], excelList: any[], subModuleType: any): Promise<any[]> {
    try {
      if (!this.isValidList(dbList)) {
        return this.isValidList(excelList) ? excelList : [];
      }
      if (!this.isValidList(excelList)) {
        return dbList;
      }

      for (const excelSubSubModule of excelList) {
        const dbSubSubModule = dbList.find(subSubModule => subSubModule.subSubModuleId === excelSubSubModule.subSubModuleId && excelSubSubModule.actionType != 'Add');
        if (dbSubSubModule) {
          dbSubSubModule.actionType = excelSubSubModule.actionType;
          dbSubSubModule.subModule = excelSubSubModule.subModule;
          dbSubSubModule.description = excelSubSubModule.description;
          dbSubSubModule.moduleId = excelSubSubModule.moduleId;
          dbSubSubModule.assignedToUserId = excelSubSubModule.assignedToUserId;
          dbSubSubModule.redmineId = excelSubSubModule.redmineId;
          if (!this.isValidList(excelSubSubModule?.questionList)) {
            excelSubSubModule.questionList = [];
          }
          dbSubSubModule.questionList = await this.mergeQuestions(dbSubSubModule?.questionList, excelSubSubModule?.questionList);
          if (this.isValidList(dbSubSubModule?.subSubModuleList) || this.isValidList(excelSubSubModule?.subSubModuleList)) {
            dbSubSubModule.subSubModuleList = await this.mergeSubSubModules(dbSubSubModule?.subSubModuleList, excelSubSubModule?.subSubModuleList, 'Sub-SubModule');
          }
        } else {
          dbList.push(excelSubSubModule);
        }
      }
      return dbList;
    } catch (error) {
      throw error;
    }
  }

  async mergeQuestions(dbList: any[], excelList: any[]): Promise<any> {
    if (!this.isValidList(dbList)) {
      return this.isValidList(excelList) ? excelList : [];
    }
    if (!this.isValidList(excelList)) {
      return dbList;
    }
    for (const excelQ of excelList) {
      const dbQ = dbList.find(q => q.questionId === excelQ.questionId);
      if (dbQ) {
        dbQ.question = excelQ.question;
        dbQ.description = excelQ.description;
        dbQ.optionType = excelQ.optionType;
        dbQ.options = excelQ.options;
        dbQ.entityId = excelQ.entityId;
        dbQ.entityType = excelQ.entityType;
        dbQ.actionType = excelQ.ActionType;
      } else {
        dbList.push(excelQ);
      }
    }
    return dbList;
  }

  exportProjectInsightEntityQuestionsToExcel(entity: any, entityType: any, name: any, parentId: any, currentActiveBadgeLevel?: any) {
    if (currentActiveBadgeLevel && currentActiveBadgeLevel == 'Questions') {
      entityType = 'Question';
      entity = entity?.questionList;
      this.projectInsightImportExportService.exportProjectInsightEntityQuestionsToExcel(entity, entityType, this.projectInsightObj?.projectId, this.projectInsightObj?.projectName);
    }
    else {
      this.projectInsightImportExportService.exportProjectInsightEntityQuestionsToExcel(entity, entityType, this.projectInsightObj?.projectId, this.projectInsightObj?.projectName);
    }
  }

  computeFlagsForSubModules(subModuleList: ProjectSubModule[]) {
    let hasFlag = 'No';
    if (this.isValidList(subModuleList)) {
      subModuleList.forEach(subModule => {
        const questionFlag = subModule.questionList.some(q => q.isUpdatedFromExcelUpload);
        let subModuleFlag = 'No';
        if (this.isValidList(subModule.subSubModuleList)) {
          subModuleFlag = this.computeFlagsForSubModules(subModule.subSubModuleList);
        }
        if (questionFlag || subModuleFlag == 'Yes' || subModule?.isUpdatedFromExcelUpload == 'Yes') {
          subModule.isUpdatedFromExcelUpload = 'Yes';
          hasFlag = 'Yes';
        }
      });
    }
    return hasFlag;
  }

  computeFlagsForModules(moduleList: any[]) {
    let hasFlag = 'No';
    if (this.isValidList(moduleList)) {
      moduleList.forEach(module => {
        const questionFlag = module.questionList.some(q => q.isUpdatedFromExcelUpload);
        const subModuleFlag = this.computeFlagsForSubModules(module.subModuleList);
        if (questionFlag || subModuleFlag == 'Yes' || module?.isUpdatedFromExcelUpload == 'Yes') {
          module.isUpdatedFromExcelUpload = 'Yes';
          hasFlag = 'Yes';
        }
      });
    }
    return hasFlag;
  }

  computeFlagsForMilestones(milestones: any[]) {
    let hasFlag = 'No';
    if (this.isValidList(milestones)) {
      milestones.forEach(milestone => {
        const questionFlag = milestone.questionList.some(q => q.isUpdatedFromExcelUpload);
        const moduleFlag = this.computeFlagsForModules(milestone.moduleList);
        if (questionFlag || moduleFlag == 'Yes' || milestone?.isUpdatedFromExcelUpload == 'Yes') {
          milestone.isUpdatedFromExcelUpload = 'Yes';
          hasFlag = 'Yes';
        }
      });
    }
    return hasFlag;
  }

  computeFlagsForQuestions(questionList: any[]) {
    let hasFlag = 'No';
    if (this.isValidList(questionList)) {
      const questionFlag = questionList.some(q => q.isUpdatedFromExcelUpload == 'Yes');
      if (questionFlag) {
        hasFlag = 'Yes';
      }
    }
    return hasFlag;
  }
} 
