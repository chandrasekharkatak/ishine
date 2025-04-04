import { Component, OnInit, TemplateRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { FormBuilder, FormGroup } from '@angular/forms';
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

interface kpiList{
  remark: any;
  response:any;
  id:number;
  description: string;
}

@Component({
  selector: 'app-view-performance',
  templateUrl: './view-performance.component.html',
  styleUrls: ['./view-performance.component.css']
})
export class ViewPerformanceComponent implements OnInit {

  currentUser:User;
  feature="view_performance";
  userMapping:any = {};
  log:Log;
  activeTab: string = 'kra-kpi';
  quarterCyclesList: any;
  selectedQuarter:any;
  selectedQuarter1:any;

  kraKpiMetrics: any[] = [];
  questionnaireQuestions: any[] = [];
  currentQuestionnaireId: any;

  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;

  stats: Stats ={
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };

  goals: Goal[] = [];
  summary?: AppraisalSummary;

  currentEmployeeInfo:Employee = new Employee();
  selectedGoal?: Goal;
  subscription!: Subscription;
  modalRef?: BsModalRef;
  errorMessage: string;
  kpiList:kpiList[] = [];
  alertMessage: any;

  viewPerformanceEmpId: any;

  constructor(
    private router: Router,
    private http: HttpClient, 
    private employeeService:EmployeeService,
    private modalService: BsModalService,
    private fb: FormBuilder,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService,
    private logService:LogService,
    private route: ActivatedRoute,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    this.route.params.subscribe((params:Params) => {
      this.viewPerformanceEmpId = params['id'];
    });

    this.onGetEmployeeInfo();
    this.setActiveTab('kra-kpi');

    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          this.selectedQuarter1 = this.quarterCyclesList[0].quarterId;
          
          this.onQuarterChange();
          this.onQuarterChange1();
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
  }

  onQuarterChange1(): void{
    if (!this.selectedQuarter1) return;

    this.loadKpiList();
    this.loadQuestionnaireQuestions();
  }



  loadAppraisalSummary(): void {
    const empId = this.currentEmployeeInfo.empId;
    if (!empId ) return;

    this.performanceService.getAppraisalSummary(empId).subscribe({
      next: (data) => {
        this.summary = data;
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
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;

  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }



  loadQuestionnaireQuestions(): void {
    const quarterId = this.selectedQuarter1;
    const departmentId = this.currentEmployeeInfo.departmentId;

    this.questionnaireQuestions = [];
    this.currentQuestionnaireId = null;    

    this.performanceService.loadQuestionnaireQuestions(departmentId, quarterId).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.questionnaireQuestions = response.serviceResponse[0].questions;
            this.currentQuestionnaireId = response.serviceResponse[0].questionId;
            // console.log('Questionnaire response:',this.questionnaireQuestions);
            console.log('length of questionnaire: ', this.questionnaireQuestions.length);
  
           
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


  navigateToteam(): void{
    this.router.navigate(['/user-performance/team-dashboard']);
  }

  loadKpiList(): void {
    const quarterId = this.selectedQuarter1;
    const departmentId = this.currentEmployeeInfo.departmentId;

    this.kpiList = [];

    
    if (!quarterId || !departmentId) return;
    this.performanceService.loadKpiList(quarterId, departmentId).subscribe({
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
      managerRemark: this.selectedGoal.managerRemark,
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

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  
}