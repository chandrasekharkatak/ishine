import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { AssignGoalsGroupComponent } from '../assign-goals-group/assign-goals-group.component';
import { Sort } from '@angular/material/sort';
import { User } from 'src/app/models/user';
import { TeamViewService } from 'src/app/services/team-view.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { TeamDashboardService } from 'src/app/services/team-dashboard.service';
import { environment } from 'src/environments/environment';
import { EmployeeService } from 'src/app/services/employee.service';

@Component({
  selector: 'app-team-dashboard',
  templateUrl: './team-dashboard.component.html',
  styleUrls: ['./team-dashboard.component.css']
})

export class TeamDashboardComponent implements OnInit {

@ViewChild('bulkAssignTemplate') bulkAssignTemplate: TemplateRef<any>;
@ViewChild('singleAssignTemplate') singleAssignTemplate: TemplateRef<any>;
@ViewChild('multiGoalTemplate') multiGoalTemplate: TemplateRef<any>;

  currentUser: User;
  viewTeamMemberList: any[] = [];
  teamMemberColumns: any[] = ['blank', 'name', 'department', 'totalGoals', 'goalsCompleted'];

  isSearchEnabled: boolean = false;
  filters: any = {};
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  hodId: number;
  selectedEmployees: any[] = []; 
  goalTemplates: any[] = []; 
  selectedGoalTemplate: string = ''; 
  expectedCompletionDate: string = '';
  selectedEmployee: any = null;
  page = 1;
  loading = false;
  modalRef?: BsModalRef;
  
  // New properties for multi-goal assignment
  selectedGoalTemplates: string[] = [];
  selectedGoalData: {templateId: number, expectedCompletionDate: string}[] = [];

  constructor(
    private router: Router,
    private http: HttpClient,
    private modalService: BsModalService,
    private teamViewService: TeamViewService,
    private authenticationService: AuthenticationService,
    private teamDashboardService: TeamDashboardService,
    private employeeService: EmployeeService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  employees: any[] = [];
  goals: any[] = [];

  ngOnInit() {
    console.log('Current user:', this.currentUser);
    if (this.currentUser && this.currentUser.hodId) {
      this.hodId = this.currentUser.hodId;
      console.log('HOD ID:', this.hodId);
      this.getEmployeesInDepartment();
      this.loadGoalTemplates();
    } else {
      console.error('Current user or HOD ID is undefined');
    }
  }

  getEmployeesInDepartment() {
    this.loading = true;
    this.teamDashboardService.findEmployeesInSameDepartmentAsCurrentUser(this.hodId).subscribe(
      (response: any) => {
        console.log('Raw response:', JSON.stringify(response))
        if (response && response.serviceStatus && 
            response.serviceStatus.toUpperCase() === 'SUCCESS') {
          // Map the response to match the expected structure
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
  
  loadGoalTemplates() {
    console.log('Loading goal templates...');
    // Make sure we're using the correct API endpoint with correct casing
    this.http.get(`${environment.baseUrl}api/goal-templates`).subscribe(
      (response: any) => {
        console.log('Raw API response:', JSON.stringify(response));
        console.log('Goal templates response:', response);
        if (response && response.serviceStatus && 
            response.serviceStatus.toUpperCase() === 'SUCCESS') {
          // Ensure we're accessing the correct property in the response
          this.goalTemplates = response.serviceResponse;
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
      let sortParams: any[] = sort.active?.split("|");
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
    this.router.navigate(['/user-performance/view-performance']);
  }


  toggleEmployeeSelection(employee: any) {
    console.log('Toggling selection for employee:', employee);
    const index = this.selectedEmployees.findIndex(e => e.id === employee.id);
    if (index === -1) {
      this.selectedEmployees.push(employee);
    } else {
      this.selectedEmployees.splice(index, 1);
    }
    console.log('Selected employees:', this.selectedEmployees);
  }
  
  isEmployeeSelected(employee: any): boolean {
    return this.selectedEmployees.some(e => e.id === employee.id);
  }

  toggleAllSelection(event: any) {
    if (event.target.checked) {
      this.selectedEmployees = [...this.viewTeamMemberList];
    } else {
      this.selectedEmployees = [];
    }
  }

  // Modal methods
  openBulkAssignModal(template: TemplateRef<any>) {
    console.log('Opening bulk assign modal for:', this.selectedEmployees);
    this.modalRef = this.modalService.show(template, {
      class: 'modal-lg'
    });
  }
  
  closeModal() {
    if (this.modalRef) {
      this.modalRef.hide();
    }
  }

  assignGoalToEmployee(employee: any) {
    console.log('Setting up to assign goal to employee:', employee);
    this.selectedEmployee = employee;
    this.selectedEmployees = [employee]; // This is fine for tracking UI state
    this.modalRef = this.modalService.show(this.singleAssignTemplate, {
      class: 'modal-md'
    });
  }
  
  // New method for opening multi-goal assignment modal
  openMultiGoalModal(employee: any) {
    console.log('Setting up to assign multiple goals to employee:', employee);
    this.selectedEmployee = employee;
    // Reset the selected goals
    this.selectedGoalData = [];
    this.addNewGoalSelection(); // Initialize with one goal selection row
    
    this.modalRef = this.modalService.show(this.multiGoalTemplate, {
      class: 'modal-lg'
    });
  }
  
  // Add a new goal selection row to the form
  addNewGoalSelection() {
    this.selectedGoalData.push({
      templateId: null,
      expectedCompletionDate: ''
    });
  }
  
  // Remove a goal selection row from the form
  removeGoalSelection(index: number) {
    if (this.selectedGoalData.length > 1) {
      this.selectedGoalData.splice(index, 1);
    }
  }

  assignGoalsToEmployees() {
    console.log('Attempting to assign goals to multiple employees');
    
    if (!this.selectedGoalTemplate || !this.expectedCompletionDate || this.selectedEmployees.length === 0) {
      console.error('Missing required data for bulk goal assignment');
      alert('Please select a goal template, set an expected completion date, and select at least one employee.');
      return;
    }
  
    const empIds = this.selectedEmployees.map(emp => emp.id);
    
    const requestBody = {
      empIds: empIds,
      templateId: Number(this.selectedGoalTemplate),
      expectedCompletionDate: this.expectedCompletionDate
    };
    
    console.log('Sending bulk assignment request:', requestBody);
    
    this.http.post(`${environment.baseUrl}api/EmployeeGoals/assign-bulk`, requestBody)
      .subscribe(
        (response: any) => {
          console.log('Bulk assignment response:', response);
          if (response && response.serviceStatus && 
              response.serviceStatus.toUpperCase() === 'SUCCESS') {
            
  
            this.selectedEmployees.forEach(emp => {
              const empInList = this.viewTeamMemberList.find(e => e.id === emp.id);
             
              if (empInList) {
                empInList.totalGoals = (empInList.totalGoals || 0) + 1;
              }
            });
            
            this.closeModal();
            this.selectedEmployees = null;
            this.selectedEmployees = [];
            this.selectedGoalTemplate = '';
            this.expectedCompletionDate = '';
            
            // Still refresh from API to ensure data consistency
            this.getEmployeesInDepartment();
            alert('Goals assigned successfully!');
          } else {
            console.error('Error response:', response);
            alert('Error assigning goals: ' + (response.serviceMessage || 'Unknown error'));
          }
        },
        (error) => {
          console.error('HTTP error:', error);
          alert('Error assigning goals. Please try again.');
        }
      );
  }
  
  assignGoalToSingleEmployee() {
    console.log('Attempting to assign goal to single employee');
    
    if (!this.selectedGoalTemplate || !this.expectedCompletionDate || !this.selectedEmployee) {
      console.error('Missing required data for goal assignment');
      alert('Please select a goal template and set an expected completion date.');
      return;
    }
  
    const requestBody = {
      empId: this.selectedEmployee.id,
      templateId: Number(this.selectedGoalTemplate),
      expectedCompletionDate: this.expectedCompletionDate
    };
    
    console.log('Sending assignment request:', requestBody);
  
    // Use the single employee API endpoint here, not the bulk one
    this.http.post(`${environment.baseUrl}api/EmployeeGoals/assign`, requestBody)
      .subscribe(
        (response: any) => {
          console.log('Assignment response:', response);
          if (response && response.serviceStatus && 
              response.serviceStatus.toUpperCase() === 'SUCCESS') {
            
            // Update local data properly
            const empInList = this.viewTeamMemberList.find(e => e.id === this.selectedEmployee.id);
            if (empInList) {
              empInList.totalGoals = (empInList.totalGoals || 0) + 1;
            }
            
            this.closeModal();
            this.selectedEmployee = null;
            this.selectedEmployees = [];
            this.selectedGoalTemplate = '';
            this.expectedCompletionDate = '';
            
            // Still refresh from API to ensure data consistency
            this.getEmployeesInDepartment();
            alert('Goal assigned successfully!');
          } else {
            console.error('Error response:', response);
            alert('Error assigning goal: ' + (response.serviceMessage || 'Unknown error'));
          }
        },
        (error) => {
          console.error('HTTP error:', error);
          alert('Error assigning goal. Please try again.');
        }
      );
  }
  
  // New method to assign multiple goals to a single employee
  assignMultipleGoalsToEmployee() {
    console.log('Attempting to assign multiple goals to employee:', this.selectedEmployee);
    
    // Validate all selected goals have both template and date
    const invalidEntries = this.selectedGoalData.some(goal => 
      !goal.templateId || !goal.expectedCompletionDate);
    
    if (invalidEntries || this.selectedGoalData.length === 0) {
      console.error('Missing required data for multi-goal assignment');
      alert('Please select a goal template and set an expected completion date for each goal.');
      return;
    }
    
    // Create array of assignment requests (one per goal)
    const assignmentPromises = this.selectedGoalData.map(goal => {
      const requestBody = {
        empId: this.selectedEmployee.id,
        templateId: Number(goal.templateId),
        expectedCompletionDate: goal.expectedCompletionDate
      };
      
      return this.http.post(`${environment.baseUrl}api/EmployeeGoals/assign`, requestBody).toPromise();
    });
    
    // Execute all assignment requests
    Promise.all(assignmentPromises)
      .then(responses => {
        console.log('All assignment responses:', responses);
        
        // Check if all assignments were successful
        const allSuccessful = responses.every((response: any) => 
          response && response.serviceStatus && 
          response.serviceStatus.toUpperCase() === 'SUCCESS'
        );
        
        if (allSuccessful) {
          // Update local data
          const empInList = this.viewTeamMemberList.find(e => e.id === this.selectedEmployee.id);
          if (empInList) {
            empInList.totalGoals = (empInList.totalGoals || 0) + this.selectedGoalData.length;
          }
          
          this.closeModal();
          this.selectedEmployee = null;
          this.selectedGoalData = [];
          
          // Refresh from API to ensure data consistency
          this.getEmployeesInDepartment();
          alert(`${this.selectedGoalData.length} goals assigned successfully!`);
        } else {
          console.error('Some assignments failed:', responses);
          alert('Some goals could not be assigned. Please check the console for details.');
        }
      })
      .catch(error => {
        console.error('Error assigning multiple goals:', error);
        alert('Error assigning goals. Please try again.');
      });
  }
}