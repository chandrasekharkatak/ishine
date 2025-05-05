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
import { ProjectInsightImportExportService } from 'src/app/services/project-insight-import-export.service';
@Component({
  selector: 'app-project-insights-config',
  templateUrl: './project-insights-config.component.html',
  styleUrls: ['./project-insights-config.component.css']
})

export class ProjectInsightsConfigComponent implements OnInit {
  @ViewChild('alert_message') alertMessageTempalte: TemplateRef<any>;
  @ViewChild('insight_response_template') insightResponseTemplate: TemplateRef<any>;
  @ViewChild('fileInput') fileInput: ElementRef;

  feature = "Survey Config";
  currentUser: User;
  userMapping: any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  accordionState = {
    project: {},
    milestone: {},
    module: {},
    subModule: {},
    subSubModule: {}
  };

  //search
  searchTerm: string = '';
  backupsearchTerm: string = '';
  searchResults: any[] = [];
  tagList: any[] = [];
  treeData: any[] = [];
  selectedObject: any = null;
  userContributionList: any[] = [];
  isLeftPanelOpen = true;
  collapsedProjects: { [projectId: string]: boolean } = {};
  searchOptionList: any[] = [];
  allFilterList: any[] = [];
  allFilterOptionList: any[] = [];
  finalFilterList: any[] = [];

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

  isQuestionForm: boolean = false;
  isCreation: boolean = false;

  isProjectInsightList: boolean = false;
  isProjectInsightResponseList: boolean = false;
  isResponsePreview: boolean = true;
  isSearchEnabled: boolean = false;
  isFinalResponseSubmitted: boolean = false;
  blockUpdateButton: boolean = true;
  isExcelUploaded: boolean = false;

  projectInsight: ProjectInsight = new ProjectInsight();
  projectInsightObj: ProjectInsight = new ProjectInsight();
  projectInsightExcelObj: ProjectInsight = new ProjectInsight();

  employeeList: any[] = [];
  allProjectList: any[] = [];
  allProjectInsightList: any[] = [];
  surveyColumns: any[] = ['surveyName', 'description', 'isActive', 'createdByName', 'createdOn'];
  projectInsightMilestoneList: ProjectMilestone[] = [new ProjectMilestone()];
  projectInsightResponseList: ProjectMilestone[] = [new ProjectMilestone()];

  page = 1;
  filters: any = {};

  projectId: any;
  actionType: any = 'Configuration';
  subActionType: any = 'Creation';
  responseByEmpId: any;

  file: any;
  fileName: any;

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private surveyService: SurveyService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private clipboardService: ClipboardService,
    private router: Router,
    private utilityService: UtilityService,
    private projectService: ProjectService,
    private projectInsightService: ProjectInsightService,
    private projectInsightImportExportService: ProjectInsightImportExportService,
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

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    this.getEmployeeList();
  }

  isSearchTabClick() {
    this.searchTabClick = true;

    this.projectInsightTabClick = false;
    this.isProjectInsightList = false;
    this.isQuestionForm = false;

    this.getFilterList();
  }

  isProjectInsightTabClick() {
    this.projectInsightTabClick = true;
    this.showProjectInsight();
    this.getAllProjects();

    this.searchTabClick = false;
  }

  showProjectInsightForm() {
    this.projectInsightExcelObj = null;
    this.isQuestionForm = true;
    this.isCreation = true;
    this.subActionType = 'Creation';
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
    this.subActionType = 'Creation';
    this.isProjectInsightResponseList = false;
    this.clearFileInput();
    this.getAllProjectInsightList();
  }

  // --------------------------------- Search :: start-----------------------------

  goBack() {
    this.isSearchPreviewClicked = false;
    if (this.searchTerm == undefined || this.searchTerm == null) {
      this.searchTerm = this.backupsearchTerm;
    }
    this.onSearchTerm();
  }

  isOptionsExpanded = false;

  toggleOptions() {
    this.isOptionsExpanded = !this.isOptionsExpanded;
  }

  getFilterList() {
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

  onFilterCheckBoxClick() {
    this.allFilterOptionList = [];
    this.allFilterOptionList = [];
    if (this.allFilterList.length > 0) {
      this.allFilterList.forEach((object) => {
        if (object.isSelected != undefined && object.isSelected != null && object.isSelected == true) {
          object.optionList.forEach(element => {
            element.filterName = object.filterName;
            this.allFilterOptionList.push(element);
          });

          console.log(this.allFilterOptionList);
        }
      });
    }
  }

  onFilterOptionCheckBoxClick(optionObject: any) {
    if (optionObject?.isSelected == true) {
      this.finalFilterList.push(optionObject);
    } else {
      let findOptionObj = this.finalFilterList.findIndex(x => x.optionId == optionObject.optionId);
      if (findOptionObj >= 0) {
        this.finalFilterList.splice(findOptionObj, 1);
      }
    }
    console.log(this.finalFilterList);
  }

  suggestSearchOption(event: any) {
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

  selectSuggestion(suggestedTerm: any) {
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
        this.tagList = response.tagList;
        this.userContributionList = response.userContributionList;
        this.buildTaggedTreeData();
      },
      (error) => {
        console.error(error);
      }
    );
  }

  toggleAccordion(level: string, id: string | number) {
    this.accordionState[level][id] = !this.accordionState[level][id];
    if (this.accordionState[level][id]) {
      // Find the object by traversing treeData (implement a helper if needed)
      this.selectedObject = this.findObjectByIdAndLevel(level, id, this.treeData);
    } else {
      this.selectedObject = null;
    }
  }

  isAccordionOpen(level: string, id: string | number): boolean {
    return !!this.accordionState[level][id];
  }

  isSelectedObject(obj: any): boolean {
    return this.selectedObject === obj;
  }

  findObjectByIdAndLevel(level: string, id: string | number, nodes: any[]): any {
    for (const node of nodes) {
      if (node.type.toLowerCase() === level.toLowerCase() && node.id === id) {
        return node.object;
      }
      if (node.children?.length) {
        const found = this.findObjectByIdAndLevel(level, id, node.children);
        if (found) return found;
      }
    }
    return null;
  }


  buildTaggedTreeData() {
    this.treeData = (this.searchResults || [])
      .map(project => {
        const projectTags = (this.tagList || []).filter(tag => tag.projectId === project.projectId);

        // Recursive for submodules
        const buildTaggedSubModules = (subModules) => {
          if (!subModules) return [];
          return subModules
            .map(subModule => {
              const subModuleTag = projectTags.find(tag => tag.entityType === 'SubModule' && tag.entityId === subModule.subModuleId);
              const taggedSubSubModules = buildTaggedSubModules(subModule.subSubModuleList);
              if (subModuleTag || taggedSubSubModules.length) {
                return {
                  type: 'SubModule',
                  id: subModule.subModuleId,
                  label: subModule.subModule,
                  object: subModule,
                  tags: subModuleTag ? [subModuleTag] : [],
                  children: taggedSubSubModules
                };
              }
              return null;
            })
            .filter(Boolean);
        };

        // Recursive for modules
        const buildTaggedModules = (modules) => {
          if (!modules) return [];
          return modules
            .map(module => {
              const moduleTag = projectTags.find(tag => tag.entityType === 'Module' && tag.entityId === module.moduleId);
              const taggedSubModules = buildTaggedSubModules(module.subModuleList);
              if (moduleTag || taggedSubModules.length) {
                return {
                  type: 'Module',
                  id: module.moduleId,
                  label: module.module,
                  object: module,
                  tags: moduleTag ? [moduleTag] : [],
                  children: taggedSubModules
                };
              }
              return null;
            })
            .filter(Boolean);
        };

        // Recursive for milestones
        const buildTaggedMilestones = (milestones) => {
          console.log(milestones, " --milestones");

          if (!milestones) return [];
          return milestones
            .map(milestone => {
              const milestoneTag = projectTags.find(tag => tag.entityType === 'Milestone' && tag.entityId === milestone.milestoneId);
              const taggedModules = buildTaggedModules(milestone.moduleList);
              if (milestoneTag || taggedModules.length) {
                return {
                  type: 'Milestone',
                  id: milestone.milestoneId,
                  label: milestone.milestone,
                  object: milestone,
                  tags: milestoneTag ? [milestoneTag] : [],
                  children: taggedModules
                };
              }
              return null;
            })
            .filter(Boolean);
        };

        const projectTag = projectTags.find(tag => tag.entityType === 'Project' && tag.entityId === project.projectId);
        const taggedMilestones = buildTaggedMilestones(project.projectInsightMilestoneList);

        if (projectTag || taggedMilestones.length) {
          return {
            type: 'Project',
            id: project.projectId,
            label: project.projectName,
            object: project,
            tags: projectTag ? [projectTag] : [],
            children: taggedMilestones
          };
        }
        return null;
      })
      .filter(Boolean);
  }

  onSelectNode(node: any) {
    this.selectedObject = node.object;
  }

  searchTagTerm(tag: any) {
    this.searchTerm = tag;
    this.onSearchTerm();
  }

  clearSearch() {
    this.searchTerm = '';
  }

  onProjectClick(projectObj: any) {
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

  getAllProjectInsightList() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.allProjectInsightList = [];
    let insightObj = {
      employeeRole: this.currentUser.employeeRole,
      empId: this.currentUser.empId
    };

    this.projectInsightService.getAllProjectInsightList(insightObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjectInsightList = response.serviceResponse;
        this.allProjectInsightList.forEach(project => {
          project.createdOn = (project.createdOn) ? moment(project.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
      } else {
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

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData) {
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

  async exportProjectInsightToExcelByProjectId(projectId: any, template: TemplateRef<any>, downloadExcel: boolean): Promise<any> {
    this.projectInsightObj = await this.projectInsightImportExportService.getAllQuestionsByProjectId(projectId);
    if (this.projectInsightObj) {
      this.projectInsightImportExportService.exportProjectInsightToExcel(this.projectInsightObj, 'Project', this.projectInsightObj.projectName, null);
    } else {
      this.openAlertMod(this.alertMessageTempalte, 'Something went wrong');
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
      let excelValidated: any = await this.projectInsightImportExportService.validateProjectInsightImportFromExcel(this.file);
      if (!excelValidated || excelValidated != 'Success') {
        let alertMessage = 'Something went wrong';
        if (excelValidated) {
          alertMessage = excelValidated;
        }
        this.openAlertMod(this.alertMessageTempalte, alertMessage);
        return false;
      }

      this.isExcelUploaded = true;
      this.isCreation = true;
      this.subActionType = 'Creation';
      this.projectInsightExcelObj = await this.projectInsightImportExportService.convertJsonDataToProjectInsightObj(this.file);
      if (this.projectInsightExcelObj) {
        this.projectInsightObj = await this.projectInsightImportExportService.getAllQuestionsByProjectId(this.projectInsightExcelObj.projectId);
        if (this.projectInsightObj) {
          this.isCreation = false;
          this.subActionType = 'Updation';
          this.projectInsightExcelObj = await this.mergeProjectInsightExcelObjectWithProjectInsightDBObject(this.projectInsightObj, this.projectInsightExcelObj);
        }
      }

      this.isQuestionForm = true;
      this.isProjectInsightList = false;
      this.isProjectInsightResponseList = false;
    } catch (error) {
      console.log(error);
      this.openAlertMod(this.alertMessageTempalte, "Failed to process Excel file");
      return false;
    }
  }

  async mergeProjectInsightExcelObjectWithProjectInsightDBObject(projectInsightObj: any, projectInsightExcelObj: any): Promise<any> {
    try {
      projectInsightExcelObj.projectId = projectInsightObj.projectId;
      projectInsightExcelObj.projectName = projectInsightObj.projectName;
      projectInsightExcelObj.projectManagerId = projectInsightObj.projectManagerId;
      projectInsightExcelObj.projectManagerName = projectInsightObj.projectManagerName;
      projectInsightExcelObj.questionList = await this.mergeQuestions(projectInsightObj?.questionList, projectInsightExcelObj?.questionList);
      projectInsightExcelObj.projectInsightMilestoneList = await this.mergeMilestones(projectInsightObj?.projectInsightMilestoneList, projectInsightExcelObj.projectInsightMilestoneList);
      return projectInsightExcelObj;
    } catch (error) {
      console.log(error);
      throw error;
    }
  }

  isValidList(list: any[]): boolean {
    return this.validationService.validateNullUndefinedEmptyList(list);
  }

  isValidString(value: any): boolean {
    return this.validationService.validateNullUndefinedEmptyString(value);
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
          if (dbMilestone.milestone != excelMilestone.milestone && this.isValidString(dbMilestone.milestone) ||
            dbMilestone.description != excelMilestone.description && this.isValidString(dbMilestone.description)) {
            dbMilestone.isUpdatedFromExcelUpload = 'Yes';
          }
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
          excelMilestone.isUpdatedFromExcelUpload = 'Yes';
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
          if (dbModule.module != excelModule.module && this.isValidString(dbModule.module) ||
            dbModule.description != excelModule.description && this.isValidString(dbModule.description)) {
            dbModule.isUpdatedFromExcelUpload = 'Yes';
          }
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
          excelModule.isUpdatedFromExcelUpload = 'Yes';
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
          if (dbSubModule.subModule != excelSubModule.subModule && this.isValidString(dbSubModule.subModule) ||
            dbSubModule.description != excelSubModule.description && this.isValidString(dbSubModule.description)) {
            dbSubModule.isUpdatedFromExcelUpload = 'Yes';
          }
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
          excelSubModule.isUpdatedFromExcelUpload = 'Yes';
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
    if (this.validationService.validateNullUndefinedEmptyList(dbSubSubModuleList)) {
      for (const subSubModule of dbSubSubModuleList) {
        subSubModule.subSubModuleList = await this.traverseAndMergeSubSubModules(subSubModule?.subSubModuleList, projectInsightSubSubModuleList, subSubModule.moduleId);
      }
    }
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
          if (dbSubSubModule.subModule != excelSubSubModule.subModule && this.isValidString(dbSubSubModule.subModule) ||
            dbSubSubModule.description != excelSubSubModule.description && this.isValidString(dbSubSubModule.description)) {
            dbSubSubModule.isUpdatedFromExcelUpload = 'Yes';
          }
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
          excelSubSubModule.isUpdatedFromExcelUpload = 'Yes';
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
      const dbQ = dbList.find(q => q.questionId === excelQ.questionId && excelQ.actionType != 'Add');
      if (dbQ) {
        if (dbQ.question != excelQ.question && this.isValidString(dbQ.question) ||
          dbQ.description != excelQ.description && this.isValidString(dbQ.description) ||
          dbQ.optionType != excelQ.optionType && this.isValidString(dbQ.optionType) ||
          dbQ.options != excelQ.options && this.isValidString(dbQ.options) ||
          dbQ.entityId != excelQ.entityId && this.isValidString(dbQ.entityId) ||
          dbQ.entityType != excelQ.entityType && this.isValidString(dbQ.entityType)
        ) {
          dbQ.isUpdatedFromExcelUpload = 'Yes';
        }
        dbQ.question = excelQ.question;
        dbQ.description = excelQ.description;
        dbQ.optionType = excelQ.optionType;
        dbQ.options = excelQ.options;
        dbQ.entityId = excelQ.entityId;
        dbQ.entityType = excelQ.entityType;
        dbQ.actionType = excelQ.ActionType;
        if((excelQ.optionType == 'radio' || excelQ.optionType == 'checkbox')){
          dbQ.optionsList = excelQ?.optionType != 'text' ? JSON.parse(excelQ?.options) : [];
        }
      } else {
        excelQ.isUpdatedFromExcelUpload = 'Yes';
        dbList.push(excelQ);
      }
    }
    return dbList;
  }

}