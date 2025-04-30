import { LocationStrategy } from '@angular/common';
import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { ProjectInsight } from 'src/app/models/projectInsight';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectModule } from 'src/app/models/projectModule';
import { ProjectSubModule } from 'src/app/models/projectSubModule';
import { EmployeeService } from 'src/app/services/employee.service';
import { Document } from 'src/app/models/document';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { ProjectMilestone } from 'src/app/models/projectMilestone';
import * as XLSX from 'xlsx';
@Component({
  selector: 'app-project-insights-config',
  templateUrl: './project-insights-config.component.html',
  styleUrls: ['./project-insights-config.component.css']
})

export class ProjectInsightsConfigComponent implements OnInit {
  @ViewChild('alert_message') alertMessageTempalte:TemplateRef<any>;
  @ViewChild('insight_response_template') insightResponseTemplate:TemplateRef<any>;
  @ViewChild('fileInput') fileInput: ElementRef;
  
  feature = "Survey Config";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //search
  searchTerm: string = '';
  backupsearchTerm: string = '';
  searchResults:any[] = [];
  searchOptionList:any[] = [];
  allFilterList:any[] = [];
  allFilterOptionList:any[] = [];
  finalFilterList:any[] = [];

  // tab clicked
  projectInsightTabClick: boolean = false;
  prospectiveProjectTabClick: boolean = false;
  searchTabClick: boolean = false;
  dashboardTabClick: boolean = false;
  isSearchPreviewClicked: boolean = false;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();
  
  isQuestionForm:boolean = false;
  isCreation:boolean = false;

  isProjectInsightList:boolean = false;
  isProjectInsightResponseList:boolean = false;
  isResponsePreview:boolean = true;
  isSearchEnabled:boolean = false;
  isFinalResponseSubmitted:boolean = false;
  blockUpdateButton: boolean = true;
  isExcelUploaded:boolean = false;

  projectInsight:ProjectInsight = new ProjectInsight();
  projectInsightObj: ProjectInsight = new ProjectInsight();
  projectInsightExcelObj: ProjectInsight = new ProjectInsight();

  employeeList:any[] = [];
  allProjectList:any[] = [];
  allProjectInsightList: any[] = [];
  surveyColumns:any[] = ['surveyName','description','isActive','createdByName','createdOn'];
  projectInsightMilestoneList:ProjectMilestone[] = [new ProjectMilestone()];
  projectInsightResponseList:ProjectMilestone[] = [new ProjectMilestone()];

  page = 1;
  filters:any = {};

  projectId:any;
  actionType:any='Configuration';
  subActionType:any='Creation';
  responseByEmpId:any;

  file: any;
  fileName:any;

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

    this.getFilterList();
  }

  isProjectInsightTabClick(){
    this.projectInsightTabClick = true;
    this.showProjectInsight();
    this.getAllProjects();

    this.searchTabClick = false;
  }

  showProjectInsightForm(){
    this.projectInsightExcelObj = null;
    this.isQuestionForm = true;
    this.isCreation = true;
    this.subActionType='Creation';
    this.isExcelUploaded = false;
    this.isProjectInsightList = false;
    this.isProjectInsightResponseList = false;
    this.projectInsight = new ProjectInsight(); 
  }

  showProjectInsight() {
    this.isProjectInsightList = true;
    this.isQuestionForm = false;
    this.isCreation = false;
    this.isExcelUploaded = false;
    this.subActionType='Creation';
    this.isProjectInsightResponseList = false;
    this.clearFileInput();
    this.getAllProjectInsightList();
  }

  // --------------------------------- Search :: start-----------------------------

  goBack(){
    this.isSearchPreviewClicked = false;
    if(this.searchTerm == undefined || this.searchTerm == null){
      this.searchTerm = this.backupsearchTerm;
    }
    this.onSearchTerm();
  }

  isOptionsExpanded = false;

  toggleOptions() {
    this.isOptionsExpanded = !this.isOptionsExpanded;
  }

  getFilterList(){
    this.searchOptionList = [];
    this.projectInsightService.getFilterList().pipe(first()).subscribe(
      (response: any) => {
        this.allFilterList = response;
      },
      (error) => {
        console.error(error);
      }
    );
  }

  onFilterCheckBoxClick(){
    this.allFilterOptionList = [];
    this.allFilterOptionList = [];
    if(this.allFilterList.length > 0){
      this.allFilterList.forEach((object) => {
        if(object.isSelected != undefined && object.isSelected != null && object.isSelected == true){
          object.optionList.forEach(element => {
            element.filterName = object.filterName;
            this.allFilterOptionList.push(element);
          });

          console.log(this.allFilterOptionList);
        }
      });
    }
  }

  onFilterOptionCheckBoxClick(optionObject: any){
    if(optionObject?.isSelected == true){
      this.finalFilterList.push(optionObject);
    }else{
      let findOptionObj = this.finalFilterList.findIndex(x => x.optionId == optionObject.optionId);
            if (findOptionObj >= 0) {
              this.finalFilterList.splice(findOptionObj, 1);
            }
    }
    console.log(this.finalFilterList);
  }

  suggestSearchOption(event:any){
    this.searchOptionList = [];
    this.projectInsightService.suggestSearchOption(this.searchTerm).pipe(first()).subscribe(
      (response: any) => {
        this.searchOptionList = response;
      },
      (error) => {
        console.error(error);
      }
    );
  }

  selectSuggestion(suggestedTerm: any){
    this.searchTerm = suggestedTerm;
    this.searchOptionList = [];
    this.onSearchTerm();
  }

  onSearchTerm() {
    this.searchResults = [];
    this.isSearchPreviewClicked = false;
    this.projectInsightService.onSearchTerm(this.searchTerm).pipe(first()).subscribe(
      (response: any) => {
        this.searchResults = response.projectList;
      },
      (error) => {
        console.error(error);
      }
    );
  }

  searchTagTerm(tag:any){
    this.searchTerm = tag;
    this.onSearchTerm();
  }

  clearSearch() {
    this.searchTerm = '';
  }

  onProjectClick(projectObj:any){
    this.projectId = projectObj.projectId;
    this.subActionType = 'Search';
    this.responseByEmpId = this.currentUser.empId;
    this.isSearchPreviewClicked = true;
    this.backupsearchTerm = this.searchTerm;
  }

// --------------------------------- Search :: end-----------------------------


  showProjectInsightUpdate(projectObj: any, template: TemplateRef<any>) {
    this.projectId = projectObj.projectId;
    this.subActionType = 'Updation';
    this.isQuestionForm = true;
    this.isCreation = false;
    this.isProjectInsightList = false;
    this.isProjectInsightResponseList = false;
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

  //modals
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

  getAllProjectInsightResponsesByProjectId(projectObj: any, alertTemplate: TemplateRef<any>, insightResponseTemplate: TemplateRef<any>, isPreview: any, type?: any) {
    this.projectId = projectObj.projectId;
    this.subActionType = 'Preview';
    this.responseByEmpId = this.currentUser.empId;
    this.openProjectInsightResponeMod(insightResponseTemplate);
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl', ignoreBackdropClick: true, keyboard: false });
  }
  
  closeProjectInsightResponseModal() {
    this.projectResponseModalRef.hide();
  }

  clearFileInput(): void {
    this.file = null;
    this.fileName = null;
    this.projectInsightExcelObj = new ProjectInsight();
    const fileInput = document.getElementById('project-data-input-file') as HTMLInputElement;
    if (fileInput)
      fileInput.value = '';
  }

  uploadFile(): void {
    const fileInput = this.fileInput.nativeElement;
    fileInput.click();
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
        this.modalRef = this.modalService.show(this.alertMessageTempalte, { class: 'modal-sm' });
        this.clearFileInput();
        return false;
      }
    } else {
      this.clearFileInput();
    }
  }

  async uploadXcelData() {
    this.projectInsightObj = new ProjectInsight();
    try {
      let excelValidated: any = await this.exportExcelService.validateProjectInsightImportExcel(this.file);
      if (!excelValidated || excelValidated != 'Success') {
        let alertMessage = 'Something went wrong';
        if (excelValidated) {
          alertMessage = excelValidated;
        }
        this.openAlertMod(this.alertMessageTempalte, alertMessage);
        return false;
      }

      this.isExcelUploaded = true;
      this.subActionType = 'Creation';
      this.projectInsightExcelObj = await this.exportExcelService.convertJsonDataToProjectInsightObj(this.file);
      if (this.projectInsightExcelObj) {
        this.projectInsightObj = null;
        await this.getAllQuestionsByProjectId(this.projectInsightExcelObj.projectId, this.alertMessageTempalte, false);
        if (this.projectInsightObj) {
          this.subActionType = 'Updation';
          this.projectInsightExcelObj = await this.mergeProjectInsightExcelObjectWithProjectInsightDBObject(this.projectInsightObj, this.projectInsightExcelObj);
        }
      }

      this.isQuestionForm = true;
      this.isCreation = true;
      this.isProjectInsightList = false;
      this.isProjectInsightResponseList = false;
    } catch (error) {
      this.openAlertMod(this.alertMessageTempalte, "Failed to process Excel file");
      return false;
    }
  }

  getAllQuestionsByProjectId(projectId: any, template: TemplateRef<any>, downloadExcel: boolean): Promise<any> {
    let projectInsight: ProjectInsight = new ProjectInsight();
    projectInsight.projectId = projectId;
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
          if (downloadExcel) {
            this.exportExcelService.exportProjectToExcel(this.projectInsightObj, 'Project', this.projectInsightObj.projectName, null);
          }
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      })
      .catch(error => {
        this.openAlertMod(template, 'Error fetching project questions');
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

  convertStringToJSONSubModuleNodesOption(subModuleList: ProjectSubModule[]) {
    subModuleList.forEach((submoduleObj: any) => {
      this.parseOptionsOfQuestionToList(submoduleObj.questionList);
      if (submoduleObj.subSubModuleList != undefined && submoduleObj.subSubModuleList != null && submoduleObj.subSubModuleList.length != 0) {
        this.convertStringToJSONSubModuleNodesOption(submoduleObj.subSubModuleList);
      }
    });
  }

  mergeProjectInsightExcelObjectWithProjectInsightDBObject(projectInsightObj: any, projectInsightExcelObj: any): Promise<any> {
    return new Promise((resolve, reject) => {
      try {
        projectInsightExcelObj.projectId = projectInsightObj.projectId;
        projectInsightExcelObj.projectName = projectInsightObj.projectName;
        projectInsightExcelObj.projectManagerId = projectInsightObj.projectManagerId;
        projectInsightExcelObj.projectManagerName = projectInsightObj.projectManagerName;
        projectInsightExcelObj.questionList = this.mergeQuestions(projectInsightObj?.questionList, projectInsightExcelObj?.questionList);
        projectInsightExcelObj.projectInsightMilestoneList = this.mergerMilestones(projectInsightObj?.projectInsightMilestoneList, projectInsightExcelObj.projectInsightMilestoneList);

        resolve(projectInsightExcelObj);
      } catch (error) {
        reject(error);
      }
    });
  }

  mergeQuestions(dbQuestionsList: any[], excelQuestionsList: any[]) {
    if (this.validationService.validateNullUndefinedEmptyList(dbQuestionsList) && this.validationService.validateNullUndefinedEmptyList(excelQuestionsList)) {
      excelQuestionsList = excelQuestionsList.map(item => Object.assign({}, item));
      dbQuestionsList = dbQuestionsList.map(item => Object.assign({}, item));
      for (let dbQuestionIndex = 0; dbQuestionIndex < dbQuestionsList.length; dbQuestionIndex++) {
        const dbQuestion = dbQuestionsList[dbQuestionIndex];
        const matchIndex = excelQuestionsList.findIndex(excelQ => excelQ.questionId === dbQuestion.questionId);
        if (matchIndex !== -1) {
          excelQuestionsList[matchIndex] = { ...dbQuestion, ...excelQuestionsList[matchIndex] };
        } else {
          excelQuestionsList.push(dbQuestion);
        }
      }
      return excelQuestionsList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbQuestionsList) && this.validationService.validateNullUndefinedEmptyList(excelQuestionsList)) {
      return excelQuestionsList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbQuestionsList) && !this.validationService.validateNullUndefinedEmptyList(excelQuestionsList)) {
      return [];
    }
  }

  mergerMilestones(dbProjectInsightMilestoneList: any, excelProjectInsightMilestoneList: any) {
    if (this.validationService.validateNullUndefinedEmptyList(dbProjectInsightMilestoneList) && this.validationService.validateNullUndefinedEmptyList(excelProjectInsightMilestoneList)) {
      excelProjectInsightMilestoneList = excelProjectInsightMilestoneList.map(item => Object.assign({}, item));
      dbProjectInsightMilestoneList = dbProjectInsightMilestoneList.map(item => Object.assign({}, item));
      for (let dbMilestoneIndex = 0; dbMilestoneIndex < dbProjectInsightMilestoneList.length; dbMilestoneIndex++) {
        const dbMilestone = dbProjectInsightMilestoneList[dbMilestoneIndex];
        const matchIndex = excelProjectInsightMilestoneList.findIndex(excelMilestone => excelMilestone.milestoneId === dbMilestone.milestoneId);
        if (matchIndex !== -1) {
          let excelMilestone = excelProjectInsightMilestoneList[matchIndex];
          dbMilestone.milestone = excelMilestone.milestone;
          dbMilestone.description = excelMilestone.description;
          dbMilestone.projectId = excelMilestone.projectId;
          dbMilestone.assignedToUserId = excelMilestone.assignedToUserId;
          dbMilestone.redmineId = excelMilestone.redmineId;
          dbMilestone.milestoneId = excelMilestone.milestoneId;
          dbMilestone.actionType = excelMilestone.actionType;

          if (!this.validationService.validateNullUndefinedEmptyList(excelMilestone?.questionList)) {
            excelMilestone.questionList = [];
          }
          dbMilestone.questionList = this.mergeQuestions(dbMilestone?.questionList, excelMilestone?.questionList);
          dbMilestone.moduleList = this.mergeModules(dbMilestone?.moduleList, excelMilestone?.moduleList);

          excelProjectInsightMilestoneList[matchIndex] = dbMilestone;
        } else {
          excelProjectInsightMilestoneList.push(dbMilestone);
        }
      }
      return excelProjectInsightMilestoneList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbProjectInsightMilestoneList) && this.validationService.validateNullUndefinedEmptyList(excelProjectInsightMilestoneList)) {
      return excelProjectInsightMilestoneList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbProjectInsightMilestoneList) && !this.validationService.validateNullUndefinedEmptyList(excelProjectInsightMilestoneList)) {
      return [];
    }
  }

  mergeModules(dbModuleList: any, excelModuleList: any) {
    if (this.validationService.validateNullUndefinedEmptyList(dbModuleList) && this.validationService.validateNullUndefinedEmptyList(excelModuleList)) {
      excelModuleList = excelModuleList.map(item => Object.assign({}, item));
      dbModuleList = dbModuleList.map(item => Object.assign({}, item));
      for (let dbModuleIndex = 0; dbModuleIndex < dbModuleList.length; dbModuleIndex++) {
        const dbModule = dbModuleList[dbModuleIndex];
        const matchIndex = excelModuleList.findIndex(excelModule => excelModule.moduleId === dbModule.moduleId);
        if (matchIndex !== -1) {
          let excelModule = excelModuleList[matchIndex];
          dbModule.module = excelModule.module;
          dbModule.description = excelModule.description;
          dbModule.milestoneId = excelModule.milestoneId;
          dbModule.assignedToUserId = excelModule.assignedToUserId;
          dbModule.redmineId = excelModule.redmineId;
          dbModule.moduleId = excelModule.moduleId;
          dbModule.actionType = excelModule.actionType;

          if (!this.validationService.validateNullUndefinedEmptyList(excelModule?.questionList)) {
            excelModule.questionList = [];
          }
          dbModule.questionList = this.mergeQuestions(dbModule?.questionList, excelModule?.questionList);
          dbModule.subModuleList = this.mergeSubModules(dbModule?.subModuleList, excelModule?.subModuleList, 'SubModule');

          excelModuleList[matchIndex] = dbModule;
        } else {
          excelModuleList.push(dbModule);
        }
      }
      return excelModuleList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbModuleList) && this.validationService.validateNullUndefinedEmptyList(excelModuleList)) {
      return excelModuleList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbModuleList) && !this.validationService.validateNullUndefinedEmptyList(excelModuleList)) {
      return [];
    }
  }

  mergeSubModules(dbSubModuleList: any, excelSubModuleList: any, subModuleType: any) {
    if (this.validationService.validateNullUndefinedEmptyList(dbSubModuleList) && this.validationService.validateNullUndefinedEmptyList(excelSubModuleList)) {
      excelSubModuleList = excelSubModuleList.map(item => Object.assign({}, item));
      dbSubModuleList = dbSubModuleList.map(item => Object.assign({}, item));
      for (let dbSubModuleIndex = 0; dbSubModuleIndex < dbSubModuleList.length; dbSubModuleIndex++) {
        const dbSubModule = dbSubModuleList[dbSubModuleIndex];
        const matchIndex = excelSubModuleList.findIndex(excelSubModule => excelSubModule.subModuleId === dbSubModule.submoduleId);
        if (matchIndex !== -1) {
          let excelSubModule = excelSubModuleList[matchIndex];
          dbSubModule.subModule = excelSubModule.subModule;
          dbSubModule.description = excelSubModule.description;
          dbSubModule.moduleId = excelSubModule.moduleId;
          dbSubModule.assignedToUserId = excelSubModule.assignedToUserId;
          dbSubModule.redmineId = excelSubModule.redmineId;
          excelSubModule.subModuleId = excelSubModule.subModuleId;
          excelSubModule.actionType = excelSubModule.actionType;

          if (!this.validationService.validateNullUndefinedEmptyList(excelSubModule?.questionList)) {
            excelSubModule.questionList = [];
          }
          dbSubModule.questionList = this.mergeQuestions(dbSubModule?.questionList, excelSubModule?.questionList);
          dbSubModule.subSubModuleList = this.mergeSubModules(dbSubModule?.subSubModuleList, excelSubModule?.subSubModuleList, 'Sub-SubModule');

          excelSubModuleList[matchIndex] = dbSubModule;
        } else {
          excelSubModuleList.push(dbSubModule);
        }
      }
      return excelSubModuleList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbSubModuleList) && this.validationService.validateNullUndefinedEmptyList(excelSubModuleList)) {
      return excelSubModuleList;
    }
    else if (!this.validationService.validateNullUndefinedEmptyList(dbSubModuleList) && !this.validationService.validateNullUndefinedEmptyList(excelSubModuleList)) {
      return [];
    }
  }

}