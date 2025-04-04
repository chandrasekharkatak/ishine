import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { Sort } from '@angular/material/sort';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { TeamDashboardService } from 'src/app/services/team-dashboard.service';
import { environment } from 'src/environments/environment';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { Log } from 'src/app/models/log';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { DatePipe } from '@angular/common';
@Component({
  selector: 'app-team-dashboard',
  templateUrl: './team-dashboard.component.html',
  styleUrls: ['./team-dashboard.component.css'],
})
export class TeamDashboardComponent implements OnInit {
  @ViewChild('bulkAssignTemplate') bulkAssignTemplate: TemplateRef<any>;
  @ViewChild('singleAssignTemplate') singleAssignTemplate: TemplateRef<any>;
  @ViewChild('multiGoalTemplate') multiGoalTemplate: TemplateRef<any>;

  viewPerformanceEmpId: any;

  currentUser: User;
  feature = 'team_dashboard';
  userMapping: any = {};
  log: Log;
  viewTeamMemberList: any[] = [];
  teamMemberColumns: any[] = ['blank', 'blank', 'employeementId', 'name'];
  isSearchEnabled: boolean = false;
  filters: any = {};
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  hodId: number;
  selectedQuarter: any;
  selectedEmployees: any[] = [];
  assignGoalItem: any;
  goalTemplates: any[] = [];
  selectedGoalTemplate: string = '';
  expectedCompletionDate: string = '';
  selectedEmployee: any = null;
  quarterCyclesList: any;
  page = 1;
  quarters: any[] = [];
  loading = false;
  modalRef?: BsModalRef;

  selectedGoalTemplates: string[] = [];
  errorMessage: string;
  selectedGoalData: {
    quarterId: number;
    templateId: number;
    expectedCompletionDate: string;
  }[] = [];
  today: Date = new Date();
  alertMessage:any;

  constructor(
    private router: Router,
    private http: HttpClient,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private teamDashboardService: TeamDashboardService,
    private employeeService: EmployeeService,
    private performanceService: PerformanceService,
    private logService: LogService,
    private datePipe: DatePipe
  ) {
    this.authenticationService.currentUser.subscribe(
      (x) => (this.currentUser = x)
    );
  }

  employees: any[] = [];
  goals: any[] = [];

  ngOnInit() {
    this.logService.updateLogInfo(this.log);
    const today = new Date();
    console.log('today date: ', today);

    console.log('Current user:', this.currentUser);
    if (this.currentUser && this.currentUser.hodId) {
      this.hodId = this.currentUser.hodId;
      console.log('HOD ID:', this.hodId);
      // this.getEmployeesInDepartment();
      this.getTeamEmployeeListInTeamDashboard();
      this.loadGoalTemplates();
      this.loadQuarters();
    } else {
      console.error('Current user or HOD ID is undefined');
    }
    this.fetchQuarters();

    let featureMap: Feature = this.currentUser.userMapping.find(
      (userMap) => userMap.featureName == this.feature
    );
    featureMap.subFeatures?.forEach((sub) => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] =
        sub.isActive;
    });

  }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          console.log(this.quarterCyclesList);
         
        } else {
          this.errorMessage =
            response.serviceMessage || 'Failed to load quarters.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Error fetching quarters.';
      },
    });
  }
  
  getTeamEmployeeListInTeamDashboard(){
    this.viewTeamMemberList = [];
    let empObj = {
      empId: this.currentUser.empId,
      employeeRole: this.currentUser.employeeRole,
      departmentId: this.currentUser.departmentId
    };

    this.performanceService.getTeamEmployeeListInTeamDashboard(empObj).subscribe(
      (response: any) => {
        this.viewTeamMemberList = response;
      },
      (error) => {
        console.error('Error fetching team members:', error);
        this.loading = false;
      }
    );
  }
  
  getEmployeesInDepartment() {
    this.teamDashboardService.findEmployeesInSameDepartmentAsCurrentUser(this.hodId).subscribe(
      (response: any) => {
        console.log('Raw response:', JSON.stringify(response))
        if (response && response.serviceStatus && 
            response.serviceStatus.toUpperCase() === 'SUCCESS') {
          this.viewTeamMemberList = response.serviceResponse.map(employee => {
            return {
              id: employee.empId,
              name: employee.name,
              department: this.currentUser.departmentName || 'N/A',
              employeementId: employee.employeementId,
              totalGoals: employee.noOfGoals || 0,
              goalsCompleted: employee.goalsCompleted || 0
            };
          });
          console.log('Employees fetched successfully:', this.viewTeamMemberList);
        } else {
          console.error('Error in response:', response);
          this.viewTeamMemberList = [];
        }
        this.loading = false;
      },
      (error) => {
        console.error('Error fetching department members:', error);
        this.viewTeamMemberList = [];
        this.loading = false;
      }
    );
  }
  
  loadQuarters() {
    console.log('Loading quarters...');
    this.http.get(`${environment.baseUrl}api/quarters`).subscribe(
      (response: any) => {
        console.log('Quarters response:', response);
        if (
          response &&
          response.serviceStatus &&
          response.serviceStatus.toUpperCase() === 'SUCCESS'
        ) {
          this.quarters = response.serviceResponse || [];
          console.log('Processed quarters:', this.quarters);
        } else {
          console.error('Error loading quarters:', response);
          this.quarters = [];
        }
      },
      (error) => {
        console.error('Error fetching quarters:', error);
        this.quarters = [];
      }
    );
  }

  // getEmployeesInDepartment() {
  //   this.loading = true;
  //   this.teamDashboardService
  //     .findEmployeesInSameDepartmentAsCurrentUser(this.hodId)
  //     .subscribe(
  //       (response: any) => {
  //         console.log('Raw response:', JSON.stringify(response));
  //         if (
  //           response &&
  //           response.serviceStatus &&
  //           response.serviceStatus.toUpperCase() === 'SUCCESS'
  //         ) {
  //           // Map the response to match the expected structure
  //           this.viewTeamMemberList = response.serviceResponse.map(
  //             (employee) => {
  //               return {
  //                 id: employee.empId,
  //                 name: employee.name,
  //                 department: this.currentUser.departmentName || 'N/A',
  //                 employeementId: employee.employeementId,
  //                 totalGoals: employee.noOfGoals || 0,
  //                 goalsCompleted: employee.goalsCompleted || 0,
  //               };
  //             }
  //           );
  //           console.log(
  //             'Employees fetched successfully:',
  //             this.viewTeamMemberList
  //           );
  //         } else {
  //           console.error('Error in response:', response);
  //           this.viewTeamMemberList = [];
  //         }
  //         this.loading = false;
  //       },
  //       (error) => {
  //         console.error('Error fetching department members:', error);
  //         this.viewTeamMemberList = [];
  //         this.loading = false;
  //       }
  //     );
  // }



  loadGoalTemplates() {
    console.log('Loading goal templates...');
    this.http.get(`${environment.baseUrl}api/goal-templates`).subscribe(
      (response: any) => {
        console.log('Goal templates response:', response);
        if (
          response &&
          response.serviceStatus &&
          response.serviceStatus.toUpperCase() === 'SUCCESS'
        ) {
          this.goalTemplates = response.serviceResponse || [];
          console.log('Processed goal templates:', this.goalTemplates);
        } else {
          console.error('Error loading goal templates:', response);
          this.goalTemplates = [];
        }
      },
      (error) => {
        console.error('Error fetching goal templates:', error);
        this.goalTemplates = [];
      }
    );
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split('|');
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
  }

  getEmployeePerformance(viewTeamMember) {
    this.employeeService.setEmployee(viewTeamMember);
    this.router.navigate(['/user-performance/view-performance', viewTeamMember.empId]);
  }

  toggleEmployeeSelection(employee: any) {
    console.log('Toggling selection for employee:', employee);
    const index = this.selectedEmployees.findIndex((e) => e.id === employee.id);
    if (index === -1) {
      this.selectedEmployees.push(employee);
    } else {
      this.selectedEmployees.splice(index, 1);
    }
    console.log('Selected employees:', this.selectedEmployees);
  }

  isEmployeeSelected(employee: any): boolean {
    return this.selectedEmployees.some((e) => e.id === employee.id);
  }

  toggleAllSelection(event: any) {
    if (event.target.checked) {
      this.selectedEmployees = [...this.viewTeamMemberList];
    } else {
      this.selectedEmployees = [];
    }
  }

  openBulkAssignModal(template: TemplateRef<any>) {
    console.log('Opening bulk assign modal for:', this.selectedEmployees);
    this.modalRef = this.modalService.show(template, {
      class: 'modal-lg',
    });
  }

  futureDateFilter = (date: Date | null): boolean => {
    const today = new Date();
    return date ? date > today : false;
  };

  disableManualDateInput() {
    return false;
  }

  closeModal() {
    if (this.modalRef) {
      this.modalRef.hide();
    }
  }

  assignGoalToEmployee(employee: any) {
    console.log('Setting up to assign goal to employee:', employee);
    this.selectedEmployee = employee;
    this.selectedEmployees = [employee]; 
    this.modalRef = this.modalService.show(this.singleAssignTemplate, {
      class: 'modal-md',
    });
  }


  openMultiGoalModal(employee: any) {
    console.log('Setting up to assign multiple goals to employee:', employee);
    this.selectedEmployee = employee;
    this.selectedGoalData = []; 
    
    this.loadGoalTemplates();
    this.addNewGoalSelection(); 
    
    this.modalRef = this.modalService.show(this.multiGoalTemplate, {
      class: 'modal-lg',
    });
  }
  
  addNewGoalSelection() {
    this.selectedGoalData.push({
      templateId: null,
      expectedCompletionDate: '',
      quarterId: null,
    });
  }
  
  removeGoalSelection(index: number) {
    if (this.selectedGoalData.length > 1) {
      this.selectedGoalData.splice(index, 1);
    }
  }

  assignGoalsToEmployees(template: TemplateRef<any>) {
    console.log('Attempting to assign goals to multiple employees');

    if (
      !this.selectedGoalTemplate ||
      !this.expectedCompletionDate ||
      this.selectedEmployees.length === 0 ||
      !this.selectedQuarter
    ) {
      console.error('Missing required data for bulk goal assignment');
      this.alertMessage = "Please select a goal template, set an expected completion date, select a quarter, and select at least one employee."
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    const empIds = this.selectedEmployees.map((emp) => emp.id);

    const requestBody = {
      empIds: empIds,
      templateId: Number(this.selectedGoalTemplate),
      expectedCompletionDate: this.expectedCompletionDate,
      quarterId: Number(this.selectedQuarter),
    };

    console.log('Sending bulk assignment request:', requestBody);

    this.http
      .post(`${environment.baseUrl}api/EmployeeGoals/assign-bulk`, requestBody)
      .subscribe(
        (response: any) => {
          console.log('Bulk assignment response:', response);
          if (
            response &&
            response.serviceStatus &&
            response.serviceStatus.toUpperCase() === 'SUCCESS'
          ) {
            this.selectedEmployees.forEach((emp) => {
              const empInList = this.viewTeamMemberList.find(
                (e) => e.id === emp.id
              );

              if (empInList) {
                empInList.totalGoals = (empInList.totalGoals || 0) + 1;
              }
            });

            this.closeModal();
            this.selectedEmployees = [];
            this.selectedGoalTemplate = '';
            this.expectedCompletionDate = '';
            this.selectedQuarter = '';
            
            this.getEmployeesInDepartment();
            this.alertMessage = 'Goals assigned successfully! '
            this.openAlertMod(template, this.alertMessage);
          } else {
            console.error('Error response:', response);
            this.alertMessage = `Error assigning goals: ` + `${response.serviceMessage}`
            this.openAlertMod(template, this.alertMessage);
          }
        },
        (error) => {
          console.error('HTTP error:', error);
          this.alertMessage = "Error assigning goals. Please try again."
          this.openAlertMod(template, this.alertMessage);
        }
      );
  }
  
  assignMultipleGoalsToEmployee(template: TemplateRef<any>) {
    
    const invalidEntries = this.selectedGoalData.some(goal => 
      !goal.templateId || !goal.expectedCompletionDate || !this.selectedQuarter);
    
    if (invalidEntries || this.selectedGoalData.length === 0) {
      console.error('Missing required data for multi-goal assignment');
      this.alertMessage = "Please select a goal template, quarter, and set an expected completion date for each goal."
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    
    
    const assignmentPromises = this.selectedGoalData.map(goal => {
      const requestBody = {
        empId: this.selectedEmployee.id,
        templateId: Number(goal.templateId),
        expectedCompletionDate: this.datePipe.transform(goal.expectedCompletionDate, 'yyyy-MM-dd'),
        quarterId: Number(this.selectedQuarter) 
      };
      
      // console.log('Sending goal assignment request:', requestBody);
      
      return this.http.post(`${environment.baseUrl}api/EmployeeGoals/assign`, requestBody)
        .toPromise()
        .catch((error) => {
          console.error('Error assigning goal:', error, requestBody);
          return {
            serviceStatus: 'ERROR',
            serviceMessage: error.message || 'Failed to assign goal',
            originalError: error,
            requestData: requestBody,
          };
        });
    });

    Promise.all(assignmentPromises)
      .then((responses) => {
        console.log('All assignment responses:', responses);
        this.loading = false;

        const successfulAssignments = responses.filter(
          (response: any) =>
            response &&
            response.serviceStatus &&
            response.serviceStatus.toUpperCase() === 'SUCCESS'
        );

        const failedAssignments =
          responses.length - successfulAssignments.length;

        if (failedAssignments === 0) {
          const empInList = this.viewTeamMemberList.find(
            (e) => e.id === this.selectedEmployee.id
          );
          if (empInList) {
            empInList.totalGoals =
              (empInList.totalGoals || 0) + this.selectedGoalData.length;
          }

          this.closeModal();
          this.selectedEmployee = null;
          this.selectedGoalData = [];
          this.selectedQuarter = '';
          
          this.getEmployeesInDepartment();
          this.alertMessage = "All goals assigned successfully!"
          this.openAlertMod(template, this.alertMessage); 
        } else {
          console.error('Some assignments failed:', responses);

          if (successfulAssignments.length > 0) {
            this.alertMessage = `${successfulAssignments.length} goals assigned successfully, but ${failedAssignments} failed. Check the console for details.`
            this.openAlertMod(template, this.alertMessage); 
            this.getEmployeesInDepartment(); 
          } else {
            this.alertMessage = "Failed to assign any goals. Please check the console for details."
            this.openAlertMod(template, this.alertMessage);
          }
        }
      })
      .catch((error) => {
        this.loading = false;
        console.error('Fatal error assigning multiple goals:', error);
        
        this.alertMessage = "Error assigning goals. Please try again"
        this.openAlertMod(template, this.alertMessage);
      });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
}

