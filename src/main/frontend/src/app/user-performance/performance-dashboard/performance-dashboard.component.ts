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
import { DomainService } from 'src/app/services/domain.service';
import { Domain } from 'src/app/models/domain';
// import { CanvasJSAngularChartsModule } from '@canvasjs/angular-charts';npm
// import { Chart ,registerables} from 'chart.js';
import * as Highcharts from 'highcharts';



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
    date: string;
    remarkText: string;
    id?: number;
  }[];
}

interface remarks{
  remarkBy: number;
  date: string;
  remarkText: string;
  id?: number;
}

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: number;
  questionnaireScore: number;
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
  managerRemark: any;
  managerRating: any;
  remark: any;
  response:any;
  id:number;
  description: string;
}

interface awards{
  id: number;
  reward_type_name: string;
  remark: string;
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
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: QuestionDTO[] = [];
  kpiList:kpiList[] = [];
  selectedgoalProgress: any;
  awards: awards[] = [];
  isLoading = false;
  error: string | null = null;
  chart:any = [] ;

  stats: Stats = {
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: 0,
    questionnaireScore: 0,
  };
  minRating = 3;
  goals: Goal[] = [];

  summary: AppraisalSummary = {
    finalRating: 0,        
    finalRemarks: '',      
    appraisalScore: 0      
  };

  goalRemarks: remarks[] =[{
    remarkBy: 0,
    date: '' ,
    remarkText: '',
    id: 0
  }];

  currentEmployeeInfo: Employee = new Employee();
  selectedGoal?: Goal;
  modalRef?: BsModalRef;
  modalRef1?: BsModalRef;
  errorMessage: string;
  currentQuestionnaireId: any;

  alertMessage:any;
domainSpecializationList: any;
  isViewOnly: boolean;

  constructor(
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private domainService:DomainService,
    // Chart: Chart,

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    this.onGetEmployeeInfo();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.loadAwards();
    this.domainSpecializationList = [];
    
    // this.renderchart(); 
    // const goalsCompleted = this.stats.goalsCompleted; // Replace with actual value
    // const goalsRemaining = this.stats.goalsRemaining;
    // this.renderPieSummaryChart(
    //   'Goal Progress',
    //   'goalChart',
    //   goalsCompleted,
    //   goalsRemaining,
    //   'Goals',
    //   (name) => console.log(`Clicked on ${name}`))
    
  

    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });


  }
  
  // ngAfterViewInit(): void {
  //   this.renderchart();
  // }

  setChart():void{

    // this.goals.forEach((goal:Goal) => {
    //   if (goal.goalStatus == 'Completed') {
    //     this.stats.goalsCompleted++;
    //   } else {
    //     goalsRemaining++;
    //   }
    // });
    this.renderPieSummaryChart("Goal Progress", "goalChart", this.stats.goalsCompleted, this.stats.goalsRemaining, "Goals", (name) => console.log(`Clicked on ${name}`));
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


    let domainObj = new Domain();
        domainObj.empId = this.currentUser.empId;
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

  
  renderPieSummaryChart(chartName: any, chartId: any, goalsCompleted: number, goalsRemaining: number, labelName: any, openMod: any) {
    let colors = ['#DDDF00', '#64E572', '#ED561B', '#FFBF00'];
  
    if (chartId === 'goalChart') {
      colors = ['#DDDF00', '#64E572'];
    }
  
    // Prepare chart data
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
          this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          
          // console.log('list of quarters:', this.quarterCyclesList);
          
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
        console.log('STATS::: ',this.stats);
        this.setChart();
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    console.log('Active tab = ',this.activeTab);
    
  }

  // renderchart():void{
  //   Chart.register(...registerables);
  //   console.log('STATS::: ',this.stats);
  //   this.chart = new Chart("canvas", {
  //     type: 'pie',
  //     data: {
  //       labels: ['Red', 'Blue'],
  //       datasets: [{
  //         label: '# of Votes',
  //         data: [this.stats.goalsCompleted, this.stats.goalsRemaining],
  //         backgroundColor: [
  //           'rgba(255, 99, 132, 0.2)',
  //           'rgba(54, 162, 235, 0.2)',
  //           'rgba(255, 206, 86, 0.2)'
  //         ],
  //         borderColor: [
  //           'rgba(255, 99, 132, 1)',
  //           'rgba(54, 162, 235, 1)',
  //           'rgba(255, 206, 86, 1)'
  //         ],
  //         borderWidth: 1
  //       }]
  //     },
  //     options: {
  //       responsive: true,
  //       plugins: {
  //         legend: {
  //           position: 'top' as const,
  //         },
  //         title: {
  //           display: true,
  //           // text: 'Chart.js Pie Chart'
  //         }
  //       }
  //     }
  //   });
  // }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.selectedgoalProgress = goal.goalProgress
    this.goalRemarks = this.selectedGoal.remarks;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }



initializeQuestions(): void {
  // Initialize questionnaire questions
  if (this.questionnaireQuestions) {
    this.questionnaireQuestions.forEach(question => {
      if (question.response === undefined || question.response === null) {
        question.response = 0; // Set default value to 0
      }
      if (!question.remark) {
        question.remark = ''; // Set empty remark
      }
    });
  }
  
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

// loadQuestionnaireQuestions(): void {
  
//   const quarterId = this.selectedQuarter1;
//   const departmentId = this.currentEmployeeInfo.departmentId;

//   this.questionnaireQuestions = [];
//   this.currentQuestionnaireId = null;
  
//   if (!quarterId || !departmentId) return;

//     this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
//       next: (response: any) => {
//         if (response.serviceStatus === 'Success') {
//           this.questionnaireQuestions = response.serviceResponse[0].questions;
//           console.log('Questionnaire response:',this.questionnaireQuestions);

//         } else {
//           console.error('Failed to load questionnaire questions:', response.serviceMessage);
//         }
//       },
//       error: (error) => {
//         console.error('Error fetching questionnaire questions:', error);
//       }
//     });
// }

loadQuestionnaireQuestions(): void {
  const quarterId = this.selectedQuarter;
  const departmentId = this.currentEmployeeInfo.departmentId;
  const empId = this.currentUser.empId;

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
        this.performanceService.getQuestionnaireResponses(empId, quarterId).subscribe({
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
                  question.response = savedResponse.response || 0;
                  question.remark = savedResponse.managerRemark || '';
                  console.log(`Found saved response for question ${question.id}:`, question.response);
                } else {
                  // No saved response found, initialize
                  question.response = 0;
                  question.remark = '';
                }
              });
            } else {
              // No responses found, initialize all questions
              this.initializeQuestions();
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



// loadKpiList(): void {
//   const quarterId = this.selectedQuarter1;
//   const departmentId = this.currentEmployeeInfo.departmentId;
//   const employeeRole = this.currentUser.employeeRole;
  
//   this.kpiList = [];
  
//   if (!quarterId || !departmentId) return;
  
//     this.performanceService.getKraKpi(departmentId, quarterId,employeeRole).subscribe({
//       next: (response: any) => {
//         if (response.serviceStatus === 'Success') {
//           this.kpiList = response.serviceResponse[0].kpis;

//         } else {
//           console.error('Failed to load questionnaire questions:', response.serviceMessage);
//         }
//       },
//       error: (error) => {
//         console.error('Error fetching questionnaire questions:', error);
//       }
//     });

// }
loadKpiList(): void {
  const quarterId = this.selectedQuarter;
  const departmentId = this.currentEmployeeInfo.departmentId;
  const employeeRole = this.currentUser.employeeRole;
  const empId = this.currentUser.empId;
  
  this.kpiList = [];
  
  if (!quarterId || !departmentId) return;
  
  this.performanceService.getKraKpi(departmentId, quarterId, employeeRole).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === 'Success') {
        // Store the KPI template
        this.kpiList = response.serviceResponse[0].kpis;
        console.log('KPI list loaded:', this.kpiList);
        
        // Now fetch the saved KPI responses
        this.performanceService.showresponse(empId, quarterId).subscribe({
          next: (responseData: any) => {
            console.log('Saved KPI responses:', responseData);
            
            if (responseData && Array.isArray(responseData) && responseData.length > 0) {
              // Map the saved responses to the KPIs
              this.kpiList.forEach(kpi => {
                const savedResponse = responseData.find((resp: any) => 
                  resp.kpiId === kpi.id
                );
                
                if (savedResponse) {
                  kpi.response = savedResponse.response || 0;
                  kpi.description = savedResponse.description || '';
                  kpi.managerRating = savedResponse.managerRating;
                  kpi.managerRemark = savedResponse.managerRemark;
                  console.log(`Found saved response for KPI ${kpi.id}:`, kpi.response);
                } else {
                  // No saved response found, initialize
                  kpi.response = 0;
                  kpi.description = '';
                  kpi.managerRating = null;
                  kpi.managerRemark = null;
                }
              });
            } else {
              // No responses found, initialize all KPIs
              this.initializeQuestions();
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
  
  console.log("Response ======> "+ JSON.stringify(this.kpiList));

  const response = this.kpiList;
  const empId = this.currentEmployeeInfo.empId;
  const quarterId = this.selectedQuarter;

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
  this.loadAppraisalSummary();
}


saveUpdates(template: TemplateRef<any>) {
  const payload = {
    goalProgress: this.selectedgoalProgress,
    employeeRemark: this.selectedGoal.employeeRemark,
  };

  this.goalService.updateGoal(this.selectedGoal.goalId, this.currentUser.empId, payload).subscribe(
    (response) => {
      if (response.serviceStatus === 'Success') {
        this.alertMessage = "Updates saved successfully!";
        
        // Always close the modal after successful save
        if (this.modalRef) {
          this.modalRef.hide();
        }
        
        this.openAlertMod(template, this.alertMessage);
        
        // Refresh the goals list to reflect the updated progress
        this.fetchGoals();
      }
    },
    (error) => {
      console.error('Error saving updates:', error);
      this.alertMessage = "Error saving updates";
      this.openAlertMod(template, this.alertMessage);
    }
  );
}

  saveQuestionnaireResponses(template: TemplateRef<any>): void {
 
    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter;

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
    this.loadAppraisalSummary();
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }




}