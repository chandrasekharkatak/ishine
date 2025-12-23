import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { UntypedFormBuilder, UntypedFormGroup } from '@angular/forms';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { GoalService } from 'src/app/services/goal.service';
import { Subscription } from 'rxjs';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { first } from 'rxjs/operators';
import * as Highcharts from 'highcharts';
// import { Domain } from 'domain';
import { DomainService } from 'src/app/services/domain.service';
import { Domain } from 'src/app/models/domain';
import { DatePipe } from '@angular/common';

import { ProjectInsight } from 'src/app/models/projectInsight';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ValidationService } from 'src/app/services/validation.service';
import { Document } from 'src/app/models/document';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { Sort } from '@angular/material/sort';
import { ProjectMilestone } from 'src/app/models/projectMilestone';
import { ProjectService } from 'src/app/services/project.service';
import { UserContribution } from 'src/app/models/userContribution';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Editor, Toolbar } from 'ngx-editor';


interface Goal {
  goalStatus: string;
  goalProgress: any;
  goalId: number;
  goalTitle: string;
  description: string;
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  quarter: string;
  expectedCompletionDate: string;
  createdDate: string;
  remarks:{
    remarkBy: number;
    remarkByName: string;
    date: any;
    remarkText: string;
    id?: number;
  }[];
}

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: string;
  questionnaireScore: string;
}
interface remarks{
  remarkBy: number;
  remarkByName: string;
  date: any;
  remarkText: string;
  id?: any;
}

interface AppraisalSummary {
  finalRating: number;
  finalRemarks: string;
  appraisalScore: number;
}

interface kpiList{
  remarks: any;
  isFixed: any;
  review: any;
  managerRemark: any;
  managerRating: any;
  progress: any;
  remark: any;
  response:any;
  id:number;
  description: string;
}

interface NewKRA {
  description: string;
  progress: number;
  isEnabled: boolean;
}
interface awards{
  id: number;
  reward_type_name: string;
  remark: string;
}
interface questions {
  id?: number;
  existingId?: number; 
  questionText: string;
  managerRating: number;
  managerRemark: string;
   
}
interface KpiItem {
  id: number;
  description: string;
  progress: number;
  response: number;
  remark: string;
  remarks?: KpiRemark[];
}

interface KpiRemark {
  id: number;
  remark: string;
  createdBy: string;  // This now contains the full name directly
  createdDate: string;
  kresponseId: number;
}


@Component({
  standalone: false,
  selector: 'app-view-performance',
  templateUrl: './view-performance.component.html',
  styleUrls: ['./view-performance.component.css']
})
export class ViewPerformanceComponent implements OnInit {
  @ViewChild('alert_message') alertMessageModal:TemplateRef<any>;

  currentUser:User;
  feature="view_performance";
  userMapping:any = {};
  log:Log;
  activeTab: string = 'kra-kpi';
  quarterCyclesList: any;
  selectedQuarter:any;
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: questions[] = [];
  currentQuestionnaireId: any;
  awards: awards[] = [];

  allQuestionMarksList: any[] = [];
  allProjectList:any[] = [];
  getUserContributionForReviewList:any[] = [];
  finalContributionList:any[] = [];

  toggleReviewView:boolean = false;
  showPreReviewerSelection:boolean = false;
  showValidationErrors: boolean = false;
  showPreviewDiv:boolean = false;

  validationErrors: string = '';
  selectedPreReviewer:any = '';

  kraKpiReviewForm: UntypedFormGroup;
  questionnaireReviewForm: UntypedFormGroup;

  stats: Stats ={
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };
  goalRemarks: remarks[] =[{
    remarkBy: 0,
    remarkByName: '',
    date: '',
    remarkText: '',
  }];
  newRemarkText: string = '';
  goals: Goal[] = [];
  summary?: AppraisalSummary;

  currentEmployeeInfo:Employee = new Employee();
  userContributionObj:UserContribution = new UserContribution();

  selectedGoal?: Goal;
  subscription!: Subscription;
  modalRef?: NgbModalRef;
  modalRef1?: NgbModalRef;
  modalRef2?: NgbModalRef;
  newKRAList: NewKRA[] = [];
  errorMessage: string;
  kpiList:kpiList[] = [];
  alertMessage: any;

  viewPerformanceEmpId: any;
  isLoading = false;
  error: string | null = null;
  // domainService: any;
  domainSpecializationList: any;

  // Project Insight
  isQuestionForm: boolean = false;
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isProjectInsightList: boolean = false;
  isSurveyResponseList: boolean = false;
  isProjectInsightResponseList: boolean = false;
  isResponsePreview: boolean = true;
  isSearchEnabled: boolean = false;
  isFinalResponseSubmitted: boolean = false;

  contributionFilters:any = {};
  isContributionSearchEnabled:boolean = false;
  myContributionPage = 1;
  filters: any = {};
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  projectInsight:ProjectInsight = new ProjectInsight();

  allProjectInsightList: any[] = [];
  projectInsightColumnColumns: any[] = ['projectName', 'description', 'isActive', 'createdByName', 'createdOn'];
  projectInsightContributionColumns:any[] = ['projectName','status','createdOn', 'blank']
  employeeList:any[] = [];

  projectResponseModalRef:NgbModalRef;
  documentPreviewModalRef:NgbModalRef;

  projectId:any;
  actionType:any='Contribution';
  subActionType:any='Review Response';
  responseByEmpId:any;

  //Text Editor
  editor: Editor;
  toolbar: Toolbar = [
    ['undo', 'redo'],
    ['bold', 'italic', 'underline', 'strike', 'superscript', 'subscript'],
    ['align_justify', 'align_left', 'align_center', 'align_right'],
    ['ordered_list', 'bullet_list'],
    ['indent', 'outdent'],
    [{ heading: ['h1', 'h2', 'h3'] }],
    ['text_color', 'background_color'],
    ['horizontal_rule'],
    ['format_clear'],
    ['code']
  ];

  constructor(
    private router: Router,
    private http: HttpClient, 
    private employeeService:EmployeeService,
    private modalService: NgbModal,
    private fb: UntypedFormBuilder,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private route: ActivatedRoute,
    private domainService:DomainService,
    private datePipe: DatePipe,
    private projectInsightService:ProjectInsightService,
    private validationService: ValidationService,
    private projectService: ProjectService

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.editor = new Editor();
    // let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });

    this.logService.updateLogInfo(this.log);
    this.isProjectInsightList = true;
    this.route.params.subscribe((params:Params) => {
      this.viewPerformanceEmpId = params['id'];
    });

    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();

    this.setActiveTab('kra-kpi');
    this.loadAwards();
    this.domainSpecializationList = [];
    

    this.getAllProjectInsightContributionList();
    this.getEmployeeList();
    this.getUserContributionForReview();
    this.getAllProjects();
  }

  ngOnDestroy(): void {
    this.editor.destroy();
  }
  
  setChart():void{
    // let goalsCompleted = 0;
    // let goalsRemaining = 0;

    // this.goals.forEach((goal:Goal) => {
    //   if (goal.goalStatus == 'Completed') {
    //     goalsCompleted++;
    //   } else {
    //     goalsRemaining++;
    //   }
    // });
    this.renderPieSummaryChart("Goal Progress", "goalChart", this.stats.goalsCompleted, this.stats.goalsRemaining, "Goals", (name) => console.log(`Clicked on ${name}`));
  }
  loadAwards(): void {
    this.isLoading = true;
    this.error = null;
    
    this.performanceService.getawards(this.currentUser.empId).subscribe({
      next: (data) => {
        this.awards = data;
        // this.isLoading = false;
        console.log('Awards:', this.awards);
      },
      error: (err) => {
        console.error('Error fetching awards:', err);
        this.error = 'Failed to load awards. Please try again.';
        // this.isLoading = false;
      }
    });
  }
  renderPieSummaryChart(chartName: any, chartId: any, goalsCompleted: number, goalsRemaining: number, labelName: any, openMod: any) {
      let colors = ['#DDDF00', '#64E572', '#ED561B', '#FFBF00'];
    
      if (chartId === 'goalChart') {
        colors = ['#DDDF00', '#64E572'];
      }
    
      const chartData = [
        { name: 'Goals Completed', y: goalsCompleted },
        { name: 'Goals Remaining', y: goalsRemaining }
      ];
    
      Highcharts.chart(chartId, {
        credits: {
          enabled: false
        },
        chart: {
          plotBackgroundColor: null,
          plotBorderWidth: null,
          plotShadow: false,
          type: 'pie'
        },
        title: {
          text: chartName,
          style: {
            fontWeight: 'bold',
            color: '#000000'
          }
        },
        tooltip: {
          pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
        },
        accessibility: {
          point: {
            valueSuffix: '%'
          }
        },
        exporting: {
          enabled: true,
          buttons: {
            contextButton: {
              menuItems: [
                "viewFullscreen",
                "downloadPNG",
                "downloadJPEG",
                "downloadCSV",
                "downloadXLS",
                "downloadPDFDocument"
              ]
            }
          }
        },
        plotOptions: {
          pie: {
            borderWidth: 0,
            allowPointSelect: true,
            cursor: 'pointer',
            events: {
              click(event) {
                if (chartId === 'goalChart') {
                  openMod(event.point.name);
                }
              }
            },
            dataLabels: {
              enabled: true,
              format: '<b>{point.name}</b>: {point.y:.1f}'
            },
            showInLegend: true
          }
        },
        legend: {
          enabled: true,
          labelFormatter() {
            const point = this as any;
            return this.name + ` : ${point.y}`;
          }
        },
        series: [{
          name: labelName,
          colorByPoint: true,
          type: undefined,
          data: chartData
        }],
        colors
      });
    }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          // this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          this.setDefaultQuarter();
          this.onQuarterChange();
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
    this.loadKpiList();
    this.loadQuestionnaireQuestions();
  }




  loadAppraisalSummary(): void { 
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter
    console.log('emp id: ',this.currentEmployeeInfo.empId);
    if (!empId) return;

    this.performanceService.getAppraisalSummary(empId,quarterId).subscribe({
      next: (response:any) => {
        this.summary = response.serviceResponse;
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
          console.log('list of goals',this.goals);

          

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
    const empId = this.viewPerformanceEmpId;
    const quarter = this.selectedQuarter;


    if (!empId || !quarter) return;

    this.performanceService.getPerformanceStats(empId, quarter).subscribe({
      next: (response: any) => {
        this.stats = response.serviceResponse;
        this.setChart();
        console.log('STATS::: ',this.stats);
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }
 

  async onGetEmployeeInfo(){
    let currentEmp = new Employee();
    currentEmp.empId = this.viewPerformanceEmpId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log(this.currentEmployeeInfo);
      this.fetchQuarters();
    } else {
      console.error(response.serviceResponse);
    }

    let domainObj = new Domain();
            domainObj.empId = currentEmp.empId;
            const domainResponse:any = await this.domainService.getDomainSpecializationByEmpId(domainObj).toPromise();
              if (domainResponse.serviceStatus == "Success") {
                
                this.domainSpecializationList = domainResponse.serviceResponse;
                this.domainSpecializationList.forEach((object:Domain) =>{
        
                  var letters = 'BCDEF'.split('');
                  var color = '#';
                  for (var i = 0; i < 6; i++) {
                    color += letters[Math.floor(Math.random() * letters.length)];
                  }
        
                  object.colorCode = color;
                });
        
                //console.log(this.domainSpecializationList, " : this.domainSpecializationList");
              } else {
                console.error(domainResponse.serviceResponse);
              }
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;

  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    

    this.goalService.getGoalRemarks(this.selectedGoal.goalId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.selectedGoal.remarks= response.serviceResponse;
          this.goalRemarks = this.selectedGoal.remarks;
        }
        else {
         this.errorMessage = response.serviceMessage || 'No goal remarks found for this employee.';
        }
      },
        error: (error) => {
          this.errorMessage = error.message || 'Failed to fetch employee goals remarks.';
        },
      
    })

    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
  }

  initializeQuestions(): void {
    // Initialize questionnaire questions
    // if (this.questionnaireQuestions) {
    //   this.questionnaireQuestions.forEach(question => {
    //     if (question.response === undefined || question.response === null) {
    //       question.response = 0; // Set default value to 0
    //     }
    //     if (!question.remark) {
    //       question.remark = ''; // Set empty remark
    //     }
    //   });
    // }
    
    // Initialize KPI list
    if (this.kpiList) {
      this.kpiList.forEach(kpi => {
        if (kpi.response === undefined || kpi.response === null) {
          kpi.response = 0; // Set default value to 0
        }
        if (!kpi.remark) {
          kpi.remark = ''; // Set empty remark
        }
      });
    }
  }
  initializeQuestionnaireData(): void {
    if (this.questionnaireQuestions && this.questionnaireQuestions.length > 0) {
      this.questionnaireQuestions.forEach(question => {
        // Ensure all required fields have valid default values
        if (question.managerRating === undefined || question.managerRating === null) {
          question.managerRating = 0;
        }
        
        // Important: Initialize manager remarks if missing
        if (question.managerRemark === undefined || question.managerRemark === null) {
          question.managerRemark = '';
        }
        
      });
    }
  }


  loadQuestionnaireQuestions(): void {
    const quarterId = this.selectedQuarter;
    const departmentId = this.currentEmployeeInfo.departmentId;
    // const empId = this.currentUser.empId;
    let currentEmp = new Employee();
    currentEmp.empId = this.viewPerformanceEmpId;

  
    this.questionnaireQuestions = [];
    this.currentQuestionnaireId = null;
    
    if (!quarterId || !departmentId) return;
  
    // First load the questionnaire template
    this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // Store the questions template
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          console.log('Questionnaire template loaded:', this.questionnaireQuestions);
          
          // Now fetch the saved responses
          this.performanceService.getQuestionnaireResponses(currentEmp.empId, quarterId).subscribe({
            next: (responseData: any) => {
              console.log('Saved responses:', responseData);
              
              if (responseData && Array.isArray(responseData) && responseData.length > 0) {
                // Map the saved responses to the questions
                this.questionnaireQuestions.forEach(question => {
                  // Look for the matching response using the question id
                  const savedResponse = responseData.find((resp: any) => 
                    resp.id === question.id
                  );
                  
                  if (savedResponse) {
                    // Update the question with saved response data
                    question.managerRemark = savedResponse.managerRemark || '';
                    question.managerRating = savedResponse.managerRating || 0;
                    // Store the database ID for later updates
                    question.existingId = savedResponse.id;
                  } else {
                    // No saved response found, initialize
                    question.managerRemark = '';
                    question.managerRating = 0;
                    question.existingId = null;
                  }
                });
              } else {
                // No responses found, initialize all questions
                this.initializeQuestionnaireData();
              }
            },
            error: (error) => {
              console.error('Error fetching saved responses:', error);
              this.initializeQuestions();
            }
          });
        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });
  }
  
  saveQuestionnaireResponses(template: TemplateRef<any>): void {
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;
  
    const preparedQuestions = this.questionnaireQuestions.map(question => ({
      id: question.id || question.existingId,
      questionText: question.questionText,
      managerRating: question.managerRating || 0,
      managerRemark: question.managerRemark || ''
    }));
  
    console.log('Submitting questionnaire data:', JSON.stringify(preparedQuestions));
    
    this.performanceService.submitQuestionnaireResponses(
      preparedQuestions,
      empId,
      quarterId
    ).subscribe({
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
    this.loadAppraisalSummary();
  }


  goBack() {
    const previous = this.performanceService.getPreviousRoute();
    if (previous) {
      this.router.navigateByUrl(previous);
    } else {
      return
    }
  }

  // loadKpiList(): void {
  //   const quarterId = this.selectedQuarter;
  //   const departmentId = this.currentEmployeeInfo.departmentId;
  //   // let currentEmp = new Employee();
  //   // currentEmp.empId = this.viewPerformanceEmpId;
    
  //   this.kpiList = [];
    
  //   if (!quarterId || !this.viewPerformanceEmpId) return;
    
  //   this.performanceService.getKraKpi(this.currentEmployeeInfo.empId,quarterId).subscribe({
  //     next: (response: any) => {
  //       if (response.serviceStatus === 'Success') {
  //         this.kpiList = response.serviceResponse.kpis.map(kpi => ({
  //           id: kpi.id, 
  //           description: kpi.description, 
  //           progress: kpi.progress || 0,
  //           response: 0,
  //           remark: '',
  //         }));
  //         console.log('KPI list loaded:', this.kpiList);
          
  //         this.performanceService.showresponse(this.currentEmployeeInfo.empId, quarterId).subscribe({
  //           next: (responseData: any) => {
  //             console.log('Saved KPI responses:', responseData);
              
  //             if (responseData && Array.isArray(responseData) && responseData.length > 0) {
  //               this.kpiList.forEach(kpi => {
  //                 const savedResponse = responseData.find((resp: any) => 
  //                   resp.description === kpi.description
  //                 );
                  
  //                 if (savedResponse) {                    
  //                   kpi.response = savedResponse.response || 0;
  //                   kpi.progress = savedResponse.progress || 0;
  //                   kpi.remark = savedResponse.remark;
                    
  //                   console.log(`Found saved response for KPI ${kpi.id}:`, kpi.response);
  //                 } 
  //               });
  //             } 
  //           },
  //           error: (error) => {
  //             console.error('Error fetching saved KPI responses:', error);
  //             this.initializeQuestions();
  //           }
  //         });
  //       } else {
  //         console.error('Failed to load KPI list:', response.serviceMessage);
  //       }
  //     },
  //     error: (error) => {
  //       console.error('Error fetching KPI list:', error);
  //     }
  //   });
  // }

  loadKpiList(): void {
    const quarterId = this.selectedQuarter;
    const departmentId = this.currentEmployeeInfo.departmentId;
    
    this.kpiList = [];
    
    if (!quarterId || !this.viewPerformanceEmpId) return;
    
    this.performanceService.getKraKpi(this.currentEmployeeInfo.empId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.kpiList = response.serviceResponse.kpis.map(kpi => ({
            id: kpi.id, 
            description: kpi.description, 
            progress: kpi.progress || 0,
            response: 0,
            remark: '',
            remarks: [] // Initialize empty remarks array
          }));
          console.log('KPI list loaded:', this.kpiList);
          
          this.performanceService.showresponse(this.currentEmployeeInfo.empId, quarterId).subscribe({
            next: (responseData: any) => {
              console.log('Saved KPI responses:', responseData);
              
              if (responseData && Array.isArray(responseData) && responseData.length > 0) {
                this.kpiList.forEach(kpi => {
                  const savedResponse = responseData.find((resp: any) => 
                    resp.description === kpi.description
                  );
                  
                  if (savedResponse) {                    
                    kpi.response = savedResponse.response || 0;
                    kpi.progress = savedResponse.progress || 0;
                    kpi.remark = savedResponse.remark;
                    
                    // Add remarks history if available
                    if (savedResponse.remarks && Array.isArray(savedResponse.remarks)) {
                      kpi.remarks = savedResponse.remarks.sort((a, b) => {
                        // Sort by date descending (newest first)
                        return new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime();
                      });
                    }
                    
                    console.log(`Found saved response for KPI ${kpi.id}:`, kpi.response);
                  } 
                });
              } 
            },
            error: (error) => {
              console.error('Error fetching saved KPI responses:', error);
              this.initializeQuestions();
            }
          });
        } else {
          console.error('Failed to load KPI list:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching KPI list:', error);
      }
    });
  }
  
  saveKpiResponses(template: TemplateRef<any>): void {
    console.log("Response ======> " + JSON.stringify(this.kpiList));
  
    let currentEmp = new Employee();
    currentEmp.empId = this.viewPerformanceEmpId;
    const quarterId = this.selectedQuarter;
  
    this.performanceService.submitKpiResponses(this.kpiList, currentEmp.empId, quarterId, this.currentUser.empId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = "KPI responses submitted successfully!";
          this.openAlertMod(template, this.alertMessage);
          this.loadPerformanceStats();
          this.loadKpiList();
        } else {
          this.alertMessage = `Failed to submit responses: ${response.serviceMessage}`;
          this.openAlertMod(template, this.alertMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting KPI responses:', error);
        this.alertMessage = `Error submitting KPI responses: ${error}`;
        this.openAlertMod(template, this.alertMessage);
      }
    });
  }

  saveUpdates(template: TemplateRef<any>) {
    const payload = {
      goalProgress: this.selectedGoal.goalProgress,
      goalTitle: this.selectedGoal.goalTitle,
      description: this.selectedGoal.description,
      expectedCompletionDate: this.selectedGoal.expectedCompletionDate,
      remarks: this.selectedGoal.remarks,
    };
  
    this.goalService.updateGoal(this.selectedGoal.goalId, this.currentUser.empId,payload).subscribe(
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

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  months: { full: string, short: string }[] = [
    { full: 'January', short: 'JAN' }, { full: 'February', short: 'FEB' }, { full: 'March', short: 'MAR' },
    { full: 'April', short: 'APR' }, { full: 'May', short: 'MAY' }, { full: 'June', short: 'JUN' },
    { full: 'July', short: 'JUL' }, { full: 'August', short: 'AUG' }, { full: 'September', short: 'SEP' },
    { full: 'October', short: 'OCT' }, { full: 'November', short: 'NOV' }, { full: 'December', short: 'DEC' }
  ];

setDefaultQuarter(): void {
  const today = new Date();
  const currentMonth = today.getMonth(); 
  
  const currentQuarter = this.quarterCyclesList.find(quarter => {
    const [startMonthShort, endMonthShort] = quarter.quarterCycle.split('-');
    
    const startMonthIndex = this.months.findIndex(m => m.short === startMonthShort);
    const endMonthIndex = this.months.findIndex(m => m.short === endMonthShort);
    
    if (startMonthIndex === -1 || endMonthIndex === -1) return false;
    
    if (endMonthIndex < startMonthIndex) {
      return currentMonth >= startMonthIndex || currentMonth <= endMonthIndex;
    } else {
      return currentMonth >= startMonthIndex && currentMonth <= endMonthIndex;
    }
  });
  
  if (currentQuarter) {
    this.selectedQuarter = currentQuarter.quarterId.toString();
  } else {
    this.selectedQuarter = this.quarterCyclesList.length > 0 ? 
      this.quarterCyclesList[0].quarterId.toString() : null;
  }
}

addRemark(): void {
  if (!this.newRemarkText.trim()) return;

  const newRemark: remarks = {
    remarkText: this.newRemarkText,
    remarkBy: this.currentUser.empId,
    remarkByName: this.currentUser.name,
    date: this.datePipe.transform(new Date(), 'yyyy-MM-dd'),
  };

  this.goalRemarks.push(newRemark);
  this.selectedGoal.remarks = this.goalRemarks;

  this.newRemarkText = '';
}

openAddKRAModal(template: TemplateRef<any>): void {
  this.newKRAList = [{
    description: '',
    progress: 0,
    isEnabled: false
  }];
  
  this.modalRef2 = this.modalService.open(template, { 
    modalDialogClass: 'modal-lg',
    backdrop: 'static',
    keyboard: false
  });
}

submitNewKRA(kra: NewKRA, template: TemplateRef<any>): void {
  const empId = this.currentEmployeeInfo.empId;
  const quarterId = this.selectedQuarter;
  
  if (!kra.description.trim()) {
    this.alertMessage = "Description cannot be empty";
    this.openAlertMod(template, this.alertMessage);
    return;
  }
  
  this.performanceService.addNewKRA(kra, empId, quarterId).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === 'Success') {
        this.alertMessage = "KRA added successfully!";
        this.openAlertMod(template, this.alertMessage);
        kra.isEnabled = true;
        this.loadKpiList();
      } else {
        this.alertMessage = `Failed to add KRA: ${response.serviceMessage}`;
        this.openAlertMod(template, this.alertMessage);
      }
    },
    error: (error) => {
      console.error('Error adding new KRA:', error);
      this.alertMessage = `Error adding new KRA: ${error.message || error}`;
      this.openAlertMod(template, this.alertMessage);
    }
  });
}
  // Project Insight

  toggleSearch(togleType: any) {
    if (togleType == 'projectInsight') {
      this.isSearchEnabled = !this.isSearchEnabled;
      if (!this.isSearchEnabled) {
        this.filters = {};
      }
    } else {
      this.isContributionSearchEnabled = !this.isContributionSearchEnabled;
      if (!this.isContributionSearchEnabled) {
        this.filters = {};
      }
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

  onSearch(searchData, type: any) {
    if (type == 'projectInsight') {
      this.filters = searchData; 
    }else{
      this.contributionFilters = searchData;
    }
  }
  
  handlePageChange(event, type: any) {
    if(type == 'projectInsight'){
      this.page = event;
    }else{
      this.myContributionPage = event;
    }
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
      empId: this.viewPerformanceEmpId,
      performanceTabName : 'Teams Dashboard'      
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

  deleteKRA(index: number): void {
    this.newKRAList.splice(index, 1);
  }

  toggleEnableKRA(index: number): void {
    this.newKRAList[index].isEnabled = !this.newKRAList[index].isEnabled;
  }

  addAnotherKRA(): void {
    this.newKRAList.push({
      description: '',
      progress: 0,
      isEnabled: false
    });
  }

  saveAllKRAs(template: TemplateRef<any>): void {
    const unenabled = this.newKRAList.filter(kra => !kra.isEnabled && kra.description.trim());
    
    if (unenabled.length > 0) {
      this.alertMessage = "You have unsaved KRAs. Please enable them or remove them before closing.";
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    
    if (this.modalRef2) {
      this.modalRef2.close();
    }
    
    this.loadKpiList();
  }

  getAllProjectInsightResponsesByProjectId(projectObj: any, alertTemplate: TemplateRef<any>, insightResponseTemplate: TemplateRef<any>, isPreview: any) {
    this.projectId = projectObj.projectId;
    this.subActionType = 'Review Response';
    this.responseByEmpId = this.viewPerformanceEmpId;
    this.openProjectInsightResponeMod(insightResponseTemplate);
  }

  openProjectInsightResponeMod(insightResponseTemplate: TemplateRef<any>) {
    this.projectResponseModalRef = this.modalService.open(insightResponseTemplate, { modalDialogClass: 'modal-xl', backdrop: 'static', keyboard: false });
  }

  closeProjectInsightResponseModal() {
    this.projectResponseModalRef.close();
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

  previewDocument(file: any, previewElementId: any) {
    let documentObj = new Document();
    documentObj.empId = this.currentUser.empId;
    documentObj.documentName = file.name;
    this.showPreviewDiv = true;

    this.projectInsightService.getUserUploadedFileForQuestion(documentObj)
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success' && response.serviceResponse?.body) {
            const previewContainer = document.getElementById(previewElementId);

            const base64Data = response?.serviceResponse?.body;
            let contentTypeList = response?.serviceResponse?.headers["Content-Type"];
            let contentType = contentTypeList[0]
            if (contentType == 'application/pdf') {
              previewContainer.innerHTML = `<embed src="data:application/pdf;base64,${base64Data}" type="application/pdf" width="100%" height="800px" />`;
            }
            else if (contentType.startsWith('image/')) {
              previewContainer.innerHTML = `<img src="data:${contentType};base64,${base64Data}" class="img-fluid" style="max-height:800px;" />`;
            }
          } else {
            this.alertMessage = "Failed to load document";
            this.modalRef = this.modalService.open(this.alertMessageModal, { modalDialogClass: 'modal-sm' });
          }
        },
        error: (error) => {
          this.alertMessage = "Error loading document";
          this.modalRef = this.modalService.open(this.alertMessageModal, { modalDialogClass: 'modal-sm' });
          console.error('Error loading document:', error);
        }
      });
  }

  processUserContribution(statusType: any, projectUSerContributionObj: any){

    if (statusType != 'preReviewer') {
      let errors: string[] = [];
      if (!projectUSerContributionObj.remark) {
        errors.push('Please enter remarks');
      }
      if (!projectUSerContributionObj.projectId) {
        errors.push('Please Tag Project');
      }

      if (errors.length > 0) {
        this.showValidationErrors = true;
        this.validationErrors = errors.join(', ');
        return;
      }
    }

    if (statusType === 'preReviewer') {
      if (!this.showPreReviewerSelection) {
        this.showPreReviewerSelection = true;
        return;
      } else if (!this.selectedPreReviewer) {
        this.alertMessage = "Please select a pre-reviewer";
        this.modalRef = this.modalService.open(this.alertMessageModal, { modalDialogClass: 'modal-sm' });
        return;
      }
      projectUSerContributionObj.assignTo = this.selectedPreReviewer;
    }else{
      projectUSerContributionObj.assignTo = this.currentUser.empId;
    }

    this.cancelRequest();

    let userContributionObj = new UserContribution();
    userContributionObj.userContributionId = projectUSerContributionObj.userContributionId;
    userContributionObj.processType = statusType;
    userContributionObj.assignTo = this.currentUser.empId;
    userContributionObj.status = statusType;
    userContributionObj.projectId = projectUSerContributionObj.projectId;
    userContributionObj.remark = projectUSerContributionObj.remark;

    this.projectInsightService.processUserContribution(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.open(this.alertMessageModal, { modalDialogClass: 'modal-sm' });

        this.getUserContributionForReview();
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.open(this.alertMessageModal, { modalDialogClass: 'modal-sm' });
      }
    });
  }

  getUserContributionForReview(){
    this.getUserContributionForReviewList = [];
    this.finalContributionList = [];

    let userContributionObj = new UserContribution();
    userContributionObj.assignTo = this.currentUser.empId;
    userContributionObj.empId = this.viewPerformanceEmpId;
    this.projectInsightService.getUserContributionForReview(userContributionObj).pipe(first()).subscribe({
      next: (response: any) => {
        this.getUserContributionForReviewList = response;
        this.finalContributionList = response;
      },
      error: (error) => {
        this.alertMessage = error.serviceMessage;
        this.modalRef = this.modalService.open(this.alertMessageModal, { modalDialogClass: 'modal-sm' });
      }
    });
  }

  openUserContributionModal(projectObj: any, contributionModal: TemplateRef<any>) {
    this.userContributionObj = projectObj;
    // this.editorConfig = {
    //   ...this.editorConfig,
    //   editable: false,
    //   showToolbar: false
    // };

    this.userContributionObj.attachments = [];
    if (projectObj.userDocument && projectObj.userDocument.length > 0) {
      projectObj.userDocument.forEach(doc => {
        this.userContributionObj.attachments.push({
          name: doc.documentName,
          documentId: doc.documentId,
          isExisting: true
        });
      });
    }

    this.showPreviewDiv = false;
    this.modalRef = this.modalService.open(contributionModal, { modalDialogClass: 'modal-xl' });
  }

  cancelRequestPreReviewer() {
    this.showPreReviewerSelection = false;
  }

  cancelRequest() {
    this.showPreReviewerSelection = false;
    this.modalRef.close();
    // this.modalService.hide();
  }

}