import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild,ChangeDetectorRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Rewards } from 'src/app/models/rewards';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { Sort } from '@angular/material/sort';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { first } from 'rxjs/operators';
import { ValidationService } from 'src/app/services/validation.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { UtilityService } from 'src/app/services/utility.service';
import { SortPipe } from 'src/app/sort.pipe';
import { Feature } from 'src/app/models/feature';
@Component({
  selector: 'app-rewards-and-recognisation',
  templateUrl: './rewards-and-recognisation.component.html',
  styleUrls: ['./rewards-and-recognisation.component.css']
})
export class RewardsAndRecognisationComponent implements OnInit {

  @ViewChild('alert_message')
  alertMessageTemplate!: TemplateRef<any>;
  items = 10;
  currentUser: User;
  userMapping: any = {};
  rewardsCategories: Rewards[] = [];
  rewards: Rewards[] = [];
  employees: Employee[] = [];
  selectedEmployee: string = '';
  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;
  selectedReward: Rewards | null = null;
  selectedRewardType: { [key: string]: string } = {};
  isRewards: boolean = true;
  isRewardshitory: boolean = false;
  iswalloffame :boolean = false;
  activeCategoryId: number | null = null;
  sumbitRewards: Rewards = new Rewards();
  selectedSubReward: any;
  selectemmpName: string;
  fromDatestr: any;
  todatestr: any;
  teams: any[] = [];
  selectedTeamId: any;
  selectedIDdprimiryKey: any;
  // @ViewChild('alert_message')
  rewardHistoryList: any = [];
  isSearchEnabled: boolean = false;
  filters: any = {};
  isTeamRewardHistory: boolean = false;
  rewardHistoryColumns: any[] = ['blank','rewardedToByName','rewardTypeName','createdByName','createdOn','managerName','ofmonthyear','updatedByName','updatedOn','remark'];
  page: number = 1;
  HistoryList: any[] = [];
  isSelectAll: boolean = false;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  name: 'Employee_Reward.xlsx';
  rewardsDataForExcel: any[];
  isEditing: boolean = false;
  isTeam: any;
  rewardTypeOptions: string[] = [];
  employeeSearchText: any = '';
  teamSearchText: any = '';
  ofmonthyear:any;
  editRewardssss: Rewards = new Rewards();
  allEmployeeList360: any[] = [];
  feature = "Rewards";

  @ViewChild('confirmDelete')
  delete_template: any;

  constructor(
    private cdr: ChangeDetectorRef,
    private locationStrategy: LocationStrategy,
    private authenticationService: AuthenticationService,
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService,
    private validationService: ValidationService,
    private employeeService: EmployeeService,
    private utilityService: UtilityService

  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.preventBackButton();
    this.getRewardsCategories(this.alertMessageTemplate);
    this.fetchRewardHistory();
    this.getAllEmployeeFor360View();
        let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
        console.log("feature Name ", featureMap);
        featureMap.subFeatures?.forEach(sub => {
          this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
        });
    
    console.log('usermapping -- ', this.userMapping);
  }

  getAllEmployeeFor360View(){
    this.allEmployeeList360 = [];
    this.employeeService.getAllEmployeesFor360View().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList360 = response.serviceResponse;
        console.log("allEmployeeListFor360 : ", this.allEmployeeList360)
        this.allEmployeeList360.forEach(employeeObj => {
          employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
          employeeObj.dateOfJoining = (employeeObj.dateOfJoining) ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.dateOfRelieving = (employeeObj.dateOfRelieving) ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
          employeeObj.updatedOn = (employeeObj.updatedOn) ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          employeeObj.createdOn = (employeeObj.createdOn) ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          if (employeeObj.isConsultant == 'true')
            employeeObj.employeeType = 'Consultant';
          else if (employeeObj.isApprenticeship == 'true')
            employeeObj.employeeType = 'Apprentice';
          else
            employeeObj.employeeType = 'Regular';
          });
          this.allEmployeeList360 = this.allEmployeeList360;
          this.allEmployeeList360 = new SortPipe().transform(this.allEmployeeList360, ['name', 'string', 'asc']);
        } else {
          alert(response.serviceResponse);
        }
    });
  }
  

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    });
  }

  getRewardsCategories(template: TemplateRef<any>) {
    this.rewardsService.getAllRewardsCategory().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success' && response.serviceResponse && response.serviceResponse.length > 0) {
          this.rewardsCategories = response.serviceResponse;
          const firstCategory = this.rewardsCategories[0];
          if (firstCategory) {
            this.getRewardsByCategoryId(firstCategory.rewardCategoryId, template);
          }
          this.isRewards = true;
        } else {
          this.openAlertMod(template, 'No reward categories available at the moment.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error fetching reward categories. Please try again later.');
      }
    );
  }

  getRewardsByCategoryId(categoryId: number, template: TemplateRef<any>) {
    this.activeCategoryId = categoryId;
    this.rewardsService.getAllRewardsByCategoryId(categoryId).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.rewards = response.serviceResponse.map((reward: any) => ({
            ...reward,
            rewardTypes: reward.rewardTypes[0].split(',').map((type: string) => type.trim())
          }));
          console.log("Reward details fetched", this.rewards);

          const firstReward = this.rewards[0];
          if (firstReward) {
            this.isTeam = firstReward.isTeam; 
            this.setSelectedReward(firstReward); // Select the first reward initially.
          }
        } else {
          this.openAlertMod(template, 'No rewards found for the selected category.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error fetching rewards. Please try again later.');
      }
    );
}

setSelectedReward(reward: Rewards) {
    this.selectedReward = reward;
    this.selectReward(reward);
}

  selectReward(reward: Rewards) {
    this.selectedReward = reward;
    console.log('Selected reward:', reward);
    this.selectedRewardType[reward.rewardName] = '';
    const rewardId = reward.id;
    this.fetchEmployees(rewardId);
    this.selectedIDdprimiryKey = rewardId;
    console.log('Reward ID:', rewardId);
    this.getActiveTeams(reward);
  }

  fetchEmployees(rewardId: number) {
    this.rewardsService.fetchEmployeesFromRewardCondition(rewardId).subscribe(
      (response: any) => {
        this.employees = response.serviceResponse.map(emp => {
          return {
            employeeNameForReward: emp.employeeName,
            employeeIdForReward: emp.employeeEmpId,
            managerNameForReward: emp.managerName,
            managerIdForReward: emp.managerEmpId,
            rewardId: rewardId
          } as Employee;
        });
        console.log(this.employees);
      },
      (error) => {
        console.error('Error fetching employees:', error);
      }
    );
  }

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  rewardsHistoryfun() {
    this.isRewards = false;
    this.isRewardshitory = true;
    this.isEditing = false;
    this.iswalloffame = false;
    this.fetchRewardHistory();
  }

  isRewardsfuc() {
    this.isRewards = true;
    this.isRewardshitory = false;
    this.iswalloffame = false;
  }

  wallOffame(){
    this.isRewards = false;
    this.isRewardshitory = false;
    this.iswalloffame = true;
    this.wallOfFameMonths = ['']; 
  }

  rewardstypeName: any

  onTeamSelected(event: any): void {
   
    const selectedTeam = this.teams.find((team) => team.teamId === event.option.value);
  
    if (selectedTeam) {
      
      this.teamSearchText = selectedTeam.teamName; 
      this.sumbitRewards.teamId = selectedTeam.teamId; 
      this.sumbitRewards.teamLeadId = selectedTeam.teamLeadId;
      this.sumbitRewards.rewardedTo = null;
    }
  }

  
  submitRewardForEmployees(template: TemplateRef<any>) {


    //  this.selectedReward.selectedType = this.valuess;
    this.sumbitRewards.remark = this.remarks;
    this.sumbitRewards.isActive = 1;
    this.sumbitRewards.createdBy = this.currentUser.empId;
    this.sumbitRewards.fromDate = this.fromDatestr;
    this.sumbitRewards.toDate = this.todatestr;
   
    this.sumbitRewards.rewardTypeName = this.selectedReward.selectedType;
    this.sumbitRewards.id = this.selectedIDdprimiryKey;
    this.sumbitRewards.ofmonthyear = this.ofmonthyear;


    console.log("Team Reward Submit check", this.sumbitRewards);
    if (!this.validateRewardsWhileSubmit(template)) {
      return; // Stop execution if validation fails
    }


    this.rewardsService.submitRewardForEmployee(this.sumbitRewards).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {

          console.log(response.serviceResponse);
          this.openAlertMod(template, response.serviceMessage);
          this.isRewards = false;
          this.isRewardshitory = true;
               this.ofmonthyear = ''; 
               this.employeeSearchText = '';
               this.remarks = '';
               this.selectedReward = null;
               this.isEditing = false; 
              //  this.activeCategoryId = null;

              if (this.rewardsCategories && this.rewardsCategories.length > 0) {
                this.activeCategoryId = this.rewardsCategories[0].rewardCategoryId;
              }
            
              if (this.rewards && this.rewards.length > 0) {
                this.selectedReward = this.rewards[0];
                this.selectedReward.selectedType= null;
              }
          this.fetchRewardHistory();


        } else {
          this.openAlertMod(template, 'No reward categories available at the moment.');
        }
      },
      (error) => {
        this.openAlertMod(template, 'Error fetching reward categories. Please try again later.');
      }
    );
    this.isEditing = false;
  }

  validateRewardsWhileSubmit(template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.ofmonthyear)) {
      this.alertMessage = "Please select the Month for which Employee is to be rewarded!!";
      this.openAlertMod(template, this.alertMessage);
      return false;
  }
    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.rewardedTo)) {
        this.alertMessage = "Please select an Employee to reward !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.rewardTypeName)) {
        this.alertMessage = "Please select a Reward Type !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(this.sumbitRewards.remark)) {
        this.alertMessage = "Please enter a Remark !!";
        this.openAlertMod(template, this.alertMessage);
        return false;
    }

    return true;
}


editReward(rewardId: number) {
  this.isEditing = true;
  this.isRewards = true;
  this.isRewardshitory = false;

  this.rewardsService.getEmployeeRewardByRewardId(rewardId).subscribe(
      (response: any) => {
          if (response.serviceStatus === 'Success' && response.serviceResponse.length > 0) {
              const rewardData = response.serviceResponse[0];
            
              this.sumbitRewards.rewardId = rewardData.rewardId;
              this.selectemmpName = rewardData.rewardedToByName;
              this.selectedTeamId = rewardData.teamId;
              this.rewardstypeName = rewardData.rewardTypeName;
              this.remarks = rewardData.remark;
              this.fromDatestr = rewardData.fromDate;
              this.todatestr = rewardData.toDate;
              this.ofmonthyear = rewardData.ofmonthyear;
              this.employeeSearchText = rewardData.rewardedToByName;

              this.selectedReward = {
                rewardName: rewardData.rewardName,
                                  categoryId: rewardData.categoryId || null,
                                  categoryName: rewardData.categoryName || null,
                                  rewardTypes: rewardData.rewardTypes || [],
                                  customFilterDTOList: rewardData.customFilterDTOList || [],
                                  rewardCategoryId: rewardData.rewardCategoryId || null,
                                  employeeName: rewardData.employeeName || null,
                                  managerName: rewardData.managerName || null,
                                  managerEmpId: rewardData.managerEmpId || null,
                                  id: rewardData.id || null,
                                  createdBy: rewardData.createdBy || null,
                                  updatedBy: rewardData.updatedBy || null,
                                  rewardId: rewardData.rewardId || null,
                                  rewardedTo: rewardData.rewardedTo || null,
                                  rewardedToByName: rewardData.rewardedToByName || null,
                                  rewardType: rewardData.rewardType || null,
                                  managerId: rewardData.managerId || null,
                                  teamLeadId: rewardData.teamLeadId || null,
                                  fromDate: rewardData.fromDate || null,
                                  toDate: rewardData.toDate || null,
                                  remark: rewardData.remark || null,
                                  createdByName: rewardData.createdByName || null,
                                  updatedByName: rewardData.updatedByName || null,
                                  updatedOn: rewardData.updatedOn || null,
                                  isActive: rewardData.isActive || null,
                                  rewardTypeName: rewardData.rewardTypeName || null,
                                  teamLeadName: rewardData.teamLeadName || null,
                                  isTeam: rewardData.isTeam || null,
                                  createdOn: rewardData.createdOn || null,
                                  teamId:rewardData.teamId || null,
                                  selectedType:rewardData.rewardTypeName || null,
                                  ofmonthyear:rewardData.ofmonthyear || null
              };

              this.sumbitRewards.managerName = rewardData.managerName;
              this.sumbitRewards.teamLeadId = rewardData.teamLeadId;
          } else {
              console.log('Error: Reward data not found');
          }
      },
      (error) => {
          console.log('Error fetching reward data:', error);
      }
  );
}

reloadPage() {
  window.location.reload();
}


wallOfFameMonths: string[] = [''];
  addMonthYear() {
    this.wallOfFameMonths.push(''); // Add an empty month-year value.
  }

  removeMonthYear(index: number) {
    if (this.wallOfFameMonths.length > 1) {
      this.wallOfFameMonths.splice(index, 1); // Remove the selected month-year.
    } else {
      alert('At least one month-year must be selected.');
    }
  }


updateRewards(template: TemplateRef<any>) {
  
  this.editRewardssss.rewardId =  this.selectedReward.rewardId;
  this.editRewardssss.ofmonthyear = this.ofmonthyear;
  this.editRewardssss.rewardedTo = this.sumbitRewards.rewardedTo || this.selectedReward.rewardedTo;
  this.editRewardssss.rewardTypeName = this.selectedReward.selectedType;
  this.editRewardssss.remark = this.remarks;
  this.editRewardssss.updatedBy = this.currentUser.empId;
  this.editRewardssss.managerId = this.sumbitRewards.managerId || this.selectedReward.managerId;

  console.log("updateddddddddddddd--", this.editRewardssss);
  this.rewardsService.updateRewardForEmployee(this.editRewardssss).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {

        console.log(response.serviceResponse);
        this.openAlertMod(template, response.serviceResponse);
        this.isRewards = false;
        this.isRewardshitory = true;
        this.fetchRewardHistory();

      } else {
        this.openAlertMod(template, 'No reward categories available at the moment.');
      }
    },
    (error) => {
      this.openAlertMod(template, 'Error while updating');
    }
  );
  this.isEditing = false;
}
  

bulkDisableRewards(template: TemplateRef<any>) {

  this.rewardsService.bulkDisableRewards().subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
        console.log(response.serviceResponse);
        this.openAlertMod(template, response.serviceResponse);
        this.fetchRewardHistory(); // Refresh the reward history
      } else {
        this.openAlertMod(template, 'Bulk disable operation failed.');
      }
    },
    (error) => {
      this.openAlertMod(template, 'An error occurred while performing bulk disable.');
    }
  );
}

bulkEnable(template: TemplateRef<any>) {
  
  this.rewardsService.bulkEnableMonthYear(this.wallOfFameMonths).subscribe(
    (response: any) => {
      if (response.serviceStatus === 'Success') {
        console.log(response.serviceResponse);
        this.openAlertMod(template, response.serviceResponse);
        this.isRewards = false;
          this.isRewardshitory = true;
          this.iswalloffame = false;
        this.fetchRewardHistory(); 
      } else {
        this.openAlertMod(template, 'Bulk Enable for the following month-year failed.');
      }
    },
    (error) => {
      this.openAlertMod(template, 'An error occurred while performing bulk enable.');
    }
  );
}



  remarks: string;

  onEmployeeChange(event: any) {
    this.selectemmpName = event?.option?.value;

    console.log(' this.selectemmpName',  this.selectemmpName);

    if (this.selectemmpName) {
      const filterEMP = this.employees.find(employee => employee.employeeNameForReward === this.selectemmpName);
      console.log('emp---', filterEMP);
      const latestReward = this.rewards[this.rewards.length - 1] || new Rewards();
      console.log('yessss', latestReward);
      this.sumbitRewards.rewardedTo = filterEMP.employeeIdForReward;
      this.sumbitRewards.rewardType = 0;
      this.sumbitRewards.rewardTypeName = latestReward.rewardTypes[0];
      this.sumbitRewards.managerId = filterEMP.managerIdForReward;
      this.sumbitRewards.teamId = null;
      this.sumbitRewards.teamLeadId = null;
    }

    console.log('yessss', this.sumbitRewards);
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  handlePageChange(event) {
    this.page = event;
    this.isSelectAll = false
    this.rewardHistoryList.forEach(x => {
      x.isSelected = false;
    })
  }

  fetchRewardHistory() {
    this.rewardHistoryList = [];
    this.filters = {};
    this.rewardsService.showAllEmployeeRewards().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {

          console.log("Reward History", response);
          this.rewardHistoryList = response.serviceResponse;
          this.rewardHistoryList.forEach(rewards => {
            rewards.createdOn = (rewards.createdOn)? moment(rewards.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
            rewards.updatedOn = (rewards.updatedOn)? moment(rewards.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            let matchingEmployee = this.allEmployeeList360.find(emp => emp.empId === rewards.empId);
            rewards.emp360 = matchingEmployee ? matchingEmployee : {};
          });
        } else {
          console.error('No rewards data available');
        }
      },
      (error) => {
        console.error('Error fetching rewards history:', error);
      }
    );
  }


  exportToExcelList: any[] = [];
  exportToExcel() {
    this.rewardsService.showAllEmployeeRewards().subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          console.log("Reward exportToExcelList", response.serviceResponse);
          this.exportToExcelList = response.serviceResponse;
        } else {
          this.exportToExcelList = [];
          console.error('No rewards data available');
        }
      },
      (error) => {
        console.error('Error fetching rewards history:', error);
      },
      () => {
        // Ensure the list is defined before mapping
        if (Array.isArray(this.exportToExcelList)) {
          const onlySpecificDataArr = this.exportToExcelList.map(
            (x) => ({
              "Employee": x.rewardedToByName || 'N/A',
              "Reward Type": x.rewardTypeName ? x.rewardTypeName : "null",
              "Rewarded On": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
              "Manager Name": x.managerName || 'N/A',
              "Month Year": x.ofmonthyear || 'N/A',
              "Updated By": x.updatedByName || 'N/A',
              "Updated On": (x.updatedOn) ? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : ' - ',
              "Remarks": x.remark || 'N/A',
            })
          );
  
          // Call the export function with the processed data
          this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, 'Reward_History.xlsx');
        } else {
          console.error('exportToExcelList is not an array');
        }
      }
    );
  }
  
  Approve(rewardId: any, template: TemplateRef<any>) {
    const obj = new Rewards();
    obj.isActive = 1;
    obj.rewardId = rewardId;
    this.rewardsService.isActive(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Approved');
        this.fetchRewardHistory();
      }
    });
  }

  undoApprove(rewardId: any, template: TemplateRef<any>) {
    const obj = new Rewards();
    obj.isActive = 0;  
    obj.rewardId = rewardId;
    
    this.rewardsService.isActive(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Undone');
        this.fetchRewardHistory(); 
      }
    });
  }

  getActiveTeams(rewards): void {
    const payload = { isTeam: rewards.isTeam };
    this.rewardsService.getAllActiveTeams(payload).subscribe(
      (response: any) => {
        if (response.serviceStatus && response.serviceStatus.toLowerCase() === 'success') {
          if (Array.isArray(response.serviceResponse)) {
            this.teams = response.serviceResponse;
            console.log("Team list:", this.teams);
          } else {
            console.error('Expected serviceResponse to be an array but received:', response.serviceResponse);
          }
        } else {
          console.error('Service failed:', response);
        }
      },
      (error) => {
        console.error('Error fetching teams:', error);
      }
    );
  }

  onTeamSelect(event: Event): void {
    const selectedElement = event.target as HTMLSelectElement;
    this.selectedTeamId = selectedElement.value;
    console.log('Selected Team ID:', this.selectedTeamId);
  }

  openConfirmDeleteModal(template: TemplateRef<any>, rewardID: any) {
    this.selectedReward = rewardID;
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }
  confirmDeleteReward() {

    if (!this.selectedReward) return;
  
    this.rewardsService.deleteEmployeeRewardByRewardId(this.selectedReward).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          console.log(response.serviceResponse);
          // this.openAlertMod(this.modalRef?.content, response.serviceResponse);
          this.isRewards = false;
          this.isRewardshitory = true;
          this.fetchRewardHistory();
        } else {
          this.openAlertMod(this.modalRef?.content, 'No reward categories available at the moment.');
        }
      },
      (error) => {
        this.openAlertMod(this.modalRef?.content, 'Error fetching reward categories. Please try again later.');
      }
    );
    this.modalRef?.hide();
  }
}