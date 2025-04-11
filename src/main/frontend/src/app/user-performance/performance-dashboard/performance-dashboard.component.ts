import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { GoalService } from 'src/app/services/goal.service';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';
import { Sort } from '@angular/material/sort';
import { ProjectInsight } from 'src/app/models/projectInsightQuestion';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { first } from 'rxjs/operators';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ValidationService } from 'src/app/services/validation.service';
import { Document } from 'src/app/models/document';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { ProjectMilestone } from 'src/app/models/projectMilestone';

interface Goal {
  goalStatus: string;
  goalProgress: any;
  goalId: number;
  goalTitle: string;
  description: string;
  checkpoints: any[];
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  quarter: string;
  expectedCompletionDate: string;
  createdDate: string;
}

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: string;
  questionnaireScore: string;
}

interface AppraisalSummary {
  finalRating: number;
  finalRemarks: string;
  appraisalScore: number;
}

interface QuestionDTO {
remark: any;
response: any;
  id: number;
  questionText: string;
}

interface kpiList{
  remark: any;
  response:any;
  id:number;
  description: string;
}

@Component({
  selector: 'app-performance-dashboard',
  templateUrl: './performance-dashboard.component.html',
  styleUrls: ['./performance-dashboard.component.css']
})
export class PerformanceDashboardComponent implements OnInit {
  feature="performance_dashboard";
  userMapping:any = {};
  log:Log;
  responses = [];
  currentUser: User;
  activeTab: string = 'kra-kpi';
  selectedQuarter: number;
  quarterCyclesList: any;
  selectedQuarter1: any;
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: QuestionDTO[] = [];
  kpiList:kpiList[] = [];
  selectedgoalProgress: any;

  stats: Stats = {
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };
  minRating = 3;
  goals: Goal[] = [];
  summary: AppraisalSummary = {
    finalRating: 0,        
    finalRemarks: '',      
    appraisalScore: 0      
  };

  currentEmployeeInfo: Employee = new Employee();
  selectedGoal?: Goal;
  modalRef?: BsModalRef;
  errorMessage: string;
  currentQuestionnaireId: any;

  alertMessage:any;

  // Project Insight
  isQuestionForm:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;
  isProjectInsightList:boolean = false;
  isSurveyResponseList:boolean = false;
  isProjectInsightResponseList:boolean = false;
  isResponsePreview:boolean = true;
  isSearchEnabled:boolean = false;
  isFinalResponseSubmitted:boolean = false;

  filters:any = {};
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  projectInsight:ProjectInsight = new ProjectInsight();

  allProjectInsightList:any[] = [];
  projectInsightColumnColumns:any[] = ['projectName','description','isActive','createdByName','createdOn'];
  projectInsightMilestoneList:ProjectMilestone[] = [new ProjectMilestone()];
  projectInsightResponseList:ProjectMilestone[] = [new ProjectMilestone()];
  employeeList:any[] = [];

  projectResponseModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();

  constructor(
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private projectInsightService:ProjectInsightService,
    private validationService: ValidationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);
    this.isProjectInsightList = true;
    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.getAllProjectInsightContributionList();
    this.getEmployeeList();
    
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });


  }
  async onGetEmployeeInfo() {
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log('Employee info:', this.currentEmployeeInfo);
      this.fetchQuarters();
    } else {
      console.error(response.serviceResponse);
    }
  }


  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          this.selectedQuarter1 = this.quarterCyclesList[0].quarterId;
          // console.log('list of quarters:', this.quarterCyclesList);
          
          this.onQuarterChange();
          this.quarterChange2();
        } else {
          this.errorMessage = response.serviceMessage || 'Failed to load quarters.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Error fetching quarters.';
      }
    });
  }

  onQuarterChange(): void {
    if (!this.selectedQuarter) return;
    
    this.loadPerformanceStats(); 
    this.fetchGoals();
    this.loadAppraisalSummary();
    
  }




  loadAppraisalSummary(): void { 
    const empId = this.currentEmployeeInfo.empId;
    console.log('emp id: ',this.currentEmployeeInfo.empId);
    if (!empId) return;

    this.performanceService.getAppraisalSummary(empId).subscribe({
      next: (response) => {
        this.summary = response.serviceResponse[0];
        // console.log('appraisal summary:', this.summary);
      },
      error: (err) => {
        console.error('Error fetching Appraisal Summary:', err);
      },
    });
  }

  fetchGoals(): void {

    this.errorMessage = ''; 
    const empId = this.currentEmployeeInfo.empId;
    if (!empId) return;
    const quarter = Number(this.selectedQuarter);
    this.goalService.getGoalsByEmployeeAndQuarter(empId, quarter).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.goals = response.serviceResponse; 
          // console.log('list of goals',this.goals);
        } else {
          this.errorMessage = response.serviceMessage || 'No goals found for this employee.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to fetch employee goals.';
      },
    });
  }

  loadPerformanceStats(): void {
    const empId = this.currentUser?.empId;
    const quarter = this.selectedQuarter;


    if (!empId || !quarter) return;

    this.performanceService.getPerformanceStats(empId, quarter).subscribe({
      next: (response: any) => {
        this.stats = response.serviceResponse;
        // console.log('STATS::: ',this.stats);
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    
  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


quarterChange2(): void{
  this.loadKpiList();
  this.loadQuestionnaireQuestions();
}

loadQuestionnaireQuestions(): void {
  
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;

  this.questionnaireQuestions = [];
  this.currentQuestionnaireId = null;
  
  if (!quarterId || !departmentId) return;

    this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          console.log('Questionnaire response:',this.questionnaireQuestions);

        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });
}


loadKpiList(): void {
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;

  this.kpiList = [];
  
  if (!quarterId || !departmentId) return;
  
    this.performanceService.getKraKpi(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.kpiList = response.serviceResponse[0].kpis;

        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });

}



saveKpiResponses(template: TemplateRef<any>): void {
  
  console.log("Response ======> "+ JSON.stringify(this.kpiList));

  const response = this.kpiList;
  const empId = this.currentEmployeeInfo.empId;
  const quarterId = this.selectedQuarter1;

  this.performanceService.submitKpiResponses(response,empId,quarterId).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === 'Success') {
        this.alertMessage = "KPI responses submitted successfully!"
        this.openAlertMod(template, this.alertMessage);
      } else {
        this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`
        this.openAlertMod(template, this.alertMessage);
      }
    },
    error: (error) => {
      console.error('Error submitting KPI responses:', error);
      this.alertMessage = `Error submitting KPI responses: ${error}`
      this.openAlertMod(template, this.alertMessage);
    }
  });
}


  saveUpdates(template: TemplateRef<any>) {
    const payload = {
      goalProgress: this.selectedgoalProgress,
      employeeRemark: this.selectedGoal.employeeRemark,
    };
  
    this.goalService.updateGoal(this.selectedGoal.goalId, payload).subscribe(
      (response) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "Updates saved successfully!"
          this.openAlertMod(template, this.alertMessage);
        }
       
      },
      (error) => {
        console.error('Error saving updates:', error);
        this.alertMessage = "'Error saving updates"
        this.openAlertMod(template, this.alertMessage);
      }
    );
  }

  saveQuestionnaireResponses(template: TemplateRef<any>): void {
 
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter1;

    console.log('question response:', this.questionnaireQuestions);
  
  
    this.performanceService.submitQuestionnaireResponses(this.questionnaireQuestions,empId,quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "Questionnaire responses submitted successfully!"
          this.openAlertMod(template, this.alertMessage);
        } else {
          this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting questionnaire responses:', error);
        this.alertMessage = `An error occurred while submitting responses. Please try again.`
        this.openAlertMod(template, this.alertMessage);
      }
    });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  // Project Insight

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
  
  handlePageChange(event) {
    this.page = event;
  }

  showProjectInsight() {
    this.isProjectInsightList = true;
    this.isQuestionForm = false;
    this.isCreation = false;
    this.isUpdation = false;
    this.isProjectInsightResponseList = false;
    this.getAllProjectInsightContributionList();
  }

  getAllProjectInsightContributionList() {
    this.sortColumn = [];
    this.sortColumnType = [];
    this.sortDirection = '';
    this.allProjectInsightList = [];

    let insightObj = {
      employeeRole: this.currentUser.employeeRole,
      empId: this.currentUser.empId,
      performanceTabName : 'Performance Dashboard'      
    };

    this.projectInsightService.getAllProjectInsightContributionList(insightObj).pipe(first()).subscribe((response: any) => {
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

 getAllProjectInsightResponsesByProjectId(projectObj: any,alertTemplate:TemplateRef<any>,insightResponseTemplate: TemplateRef<any>,isPreview:any) {
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

        if(this.projectInsight.assignedToUserId != undefined && this.projectInsight.assignedToUserId != null && this.projectInsight.assignedToUserId?.length > 0){
          this.projectInsight.toTagEmployeeList = this.employeeList.filter(emp => !this.projectInsight.assignedToUserId.includes(emp.empId));
        } else {
          this.projectInsight.toTagEmployeeList = this.employeeList;
        }

        if (this.projectInsight.questionList != null && this.projectInsight.questionList.length != 0) {
          this.projectInsight.questionList.forEach((question: ProjectQuestion, index) => {
            if(this.projectInsight.assignedToUserId != undefined && this.projectInsight.assignedToUserId != null && this.projectInsight.assignedToUserId?.length > 0){
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
                if(response.isDraft == 'Y'){
                  this.isFinalResponseSubmitted = false;
                } 
                if (response.isDraft == 'Y') {
                  response.showDocDiv = true;
                } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                  response.showDocDiv = true;
                } else {
                  response.showDocDiv = false;
                }
                if(this.isResponsePreview){
                  response.showDocDiv = true;
                }
              });
            } else {
              let projectResponse= new ProjectResponse();
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
          if(milestone.assignedToUserId != undefined && milestone.assignedToUserId != null && milestone.assignedToUserId?.length > 0){
            milestone.toTagEmployeeList = this.employeeList.filter(emp => !milestone.assignedToUserId.includes(emp.empId));
          } else {
            milestone.toTagEmployeeList = this.employeeList;
          }
          if (milestone.questionList != null && milestone.questionList.length != 0) {
            milestone.questionList.forEach((question: ProjectQuestion, index) => {
              if(milestone.assignedToUserId != undefined && milestone.assignedToUserId != null && milestone.assignedToUserId?.length > 0){
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
                  if(response.isDraft == 'Y'){
                    this.isFinalResponseSubmitted = false;
                  } 
                  if (response.isDraft == 'Y') {
                    response.showDocDiv = true;
                  } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                    response.showDocDiv = true;
                  } else {
                    response.showDocDiv = false;
                  }
                  if(this.isResponsePreview){
                    response.showDocDiv = true;
                  }
                });
              } else {
                let projectResponse= new ProjectResponse();
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
              if(module.assignedToUserId != undefined && module.assignedToUserId != null && module.assignedToUserId?.length > 0){
                module.toTagEmployeeList = this.employeeList.filter(emp => !milestone.assignedToUserId.includes(emp.empId));
              } else {
                module.toTagEmployeeList = this.employeeList;
              }
              if (module.questionList != null && module.questionList.length != 0) {
                module.questionList.forEach((question: ProjectQuestion, index) => {
                  if(module.assignedToUserId != undefined && module.assignedToUserId != null && module.assignedToUserId?.length > 0){
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
                      if(response.isDraft == 'Y'){
                        this.isFinalResponseSubmitted = false;
                      } 
                      if (response.isDraft == 'Y') {
                        response.showDocDiv = true;
                      } else if (response.isDraft == 'N' && response.uploadedFileName != undefined && response.uploadedFileName != null && response.uploadedFileName != '') {
                        response.showDocDiv = true;
                      } else {
                        response.showDocDiv = false;
                      }
                      if(this.isResponsePreview){
                        response.showDocDiv = true;
                      }
                    });
                  } else {
                    let projectResponse= new ProjectResponse();
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
        if(submodule.assignedToUserId != undefined && submodule.assignedToUserId != null && submodule.assignedToUserId?.length > 0){
          submodule.toTagEmployeeList = this.employeeList.filter(emp => !submodule.assignedToUserId.includes(emp.empId));
        } else {
          submodule.toTagEmployeeList = this.employeeList;
        }
        submodule.questionList.forEach((question: ProjectQuestion, index) => {
          if(submodule.assignedToUserId != undefined && submodule.assignedToUserId != null && submodule.assignedToUserId?.length > 0){
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
    this.projectResponseModalRef = this.modalService.show(insightResponseTemplate, { class: 'modal-xl',ignoreBackdropClick: true, keyboard: false });
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

  base64ToBlob(base64: string, contentType: string , sliceSize = 512): Blob {
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
      response.uploadedFileName = this.currentUser.empId+'-'+inputId + '.' + fileExtension;
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
        this.getUserUploadedFileForQuestion(question, fileName, documentPreviewTemplate,previewElementId);
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

  saveProjectInsightResponse(template: TemplateRef<any>,finalSubmit:any) {
    // if(finalSubmit){
    //   let inputValidated: boolean = this.validateProjectInsightResponse(template,this.projectInsightResponseList,this.projectInsightQuestion);
    //   if (!inputValidated) return;
    // }
    let files:File[]=[];

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
            response.responseList= null;
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
          for(let responseIndex = 0;responseIndex < question.projectResponseList.length; responseIndex++){
            let response = question.projectResponseList[responseIndex];
            response.responseByEmpId = this.currentUser.empId;
            response.isDraft = finalSubmit ? 'N' : 'Y';
            if (question.optionType == 'checkbox') {
              response.response = JSON.stringify(response.responseList);
              response.responseList= null;
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
            for(let responseIndex = 0;responseIndex < question.projectResponseList.length; responseIndex++){
              let response = question.projectResponseList[responseIndex];
              response.responseByEmpId = this.currentUser.empId;
              response.isDraft = finalSubmit ? 'N' : 'Y';
              if (question.optionType == 'checkbox') {
                response.response = JSON.stringify(response.responseList);
                response.responseList= null;
              }
              if (response?.uploadedFile != undefined && response?.uploadedFile != null) {
                const renamedFile = new File([response.uploadedFile], response?.uploadedFileName, { type: response.uploadedFile.type });
                files.push(renamedFile);
                response.uploadedFile = null;
              }
            }
          }
        }
        
        this.convertResponseAndRenameFile(module.subModuleList,finalSubmit,files);
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

  convertResponseAndRenameFile(subModuleList:any,finalSubmit:any,files:any){
    for (let submoduleIndex = 0; submoduleIndex < (subModuleList?.length || 0); submoduleIndex++) {
      let submodule = subModuleList[submoduleIndex];
      if (submodule.questionList?.length) {
        let submoduleProjectQuestion = submodule.questionList;
        for (let index = 0; index < submodule.questionList.length; index++) {
          let question = submoduleProjectQuestion[index];
          for(let responseIndex = 0;responseIndex < question.projectResponseList.length; responseIndex++){
            let response = question.projectResponseList[responseIndex];
            response.isDraft = finalSubmit ? 'N' : 'Y';
            response.responseByEmpId = this.currentUser.empId;
            if (question.optionType == 'checkbox') {
              response.response = JSON.stringify(response.responseList);
              response.responseList= null;
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
        this.convertResponseAndRenameFile(submodule.subSubModuleList,finalSubmit,files)
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
}