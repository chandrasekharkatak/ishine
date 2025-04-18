import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
  @ViewChild('alert_message') alertMessageTempalte:TemplateRef<any>;
  @ViewChild('insight_response_template') insightResponseTemplate:TemplateRef<any>;

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
  filterHeading:any[] = ['Department','Team Size','Client Type','Project Duration'];
  subfilterHeading:any[] = []

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

  projectInsight:ProjectInsight = new ProjectInsight();
  
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

    this.searchTabClick = false;
  }

  showProjectInsightForm(){
    this.isQuestionForm = true;
    this.isCreation = true;
    this.subActionType='Creation';
    this.isProjectInsightList = false;
    this.isProjectInsightResponseList = false;
    this.projectInsight = new ProjectInsight(); 
  }

  showProjectInsight() {
    this.isProjectInsightList = true;
    this.isQuestionForm = false;
    this.isCreation = false;
    this.subActionType='Creation';
    this.isProjectInsightResponseList = false;
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

  getAllProjectInsightResponsesByProjectId(projectObj: any, alertTemplate: TemplateRef<any>, insightResponseTemplate: TemplateRef<any>, isPreview: any, type?: any) {
    this.projectId = projectObj.projectId;
    this.subActionType = 'Review Response';
    this.responseByEmpId = this.currentUser.empId;
    this.openProjectInsightResponeMod(insightResponseTemplate);
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl', ignoreBackdropClick: true, keyboard: false });
  }
}