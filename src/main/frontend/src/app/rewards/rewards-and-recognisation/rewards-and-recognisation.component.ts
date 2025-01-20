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
  activeCategoryId: number | null = null;
  sumbitRewards: Rewards = new Rewards();
  selectedSubReward: any;
  selectemmpName: string;
  fromDatestr: any;
  todatestr: any;
  teams: any[] = [];
  selectedTeamId: any;
  selectedIDdprimiryKey: any;
  @ViewChild('alert_message')
  rewardHistoryList: any = [];
  isSearchEnabled: boolean = false;
  filters: any = {};
  isTeamRewardHistory: boolean = false;
  rewardHistoryColumns: any[] = ['', 'rewardedToByName', 'rewardTypeName', 'createdByName','createdOn', 'managerName', 'fromDate', 'toDate', 'updatedByName', 'updatedOn', 'remark',''];
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

  constructor(
    private cdr: ChangeDetectorRef,
    private locationStrategy: LocationStrategy,
    private authenticationService: AuthenticationService,
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.preventBackButton();
    this.getRewardsCategories(this.alertMessageTemplate);
    this.fetchRewardHistory();
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
    this.fetchRewardHistory();
  }

  isRewardsfuc() {
    this.isRewards = true;
    this.isRewardshitory = false;
  }

  rewardstypeName: any

  submitRewardForEmployees(template: TemplateRef<any>) {

    this.sumbitRewards.remark = this.remarks;
    this.sumbitRewards.isActive = 1;
    this.sumbitRewards.createdBy = this.currentUser.empId;
    this.sumbitRewards.fromDate = this.fromDatestr;
    this.sumbitRewards.toDate = this.todatestr;
    this.sumbitRewards.teamLeadId = 12334;
    this.sumbitRewards.rewardTypeName = this.selectedReward.selectedType;
    this.sumbitRewards.id = this.selectedIDdprimiryKey;
    this.sumbitRewards.ofmonthyear = this.ofmonthyear;


    console.log("yesss", this.rewardstypeName)


    this.rewardsService.submitRewardForEmployee(this.sumbitRewards).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {

          console.log(response.serviceResponse);
          this.openAlertMod(template, response.serviceMessage);
          this.isRewards = false;
          this.isRewardshitory = true;
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

  editReward(rewardId: number) {
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
                  selectedType:rewardData.selectedType || null,
                  ofmonthyear:rewardData.ofmonthyear || null
              };

              this.isEditing = true;
                
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
      this.sumbitRewards.rewardType = latestReward.id;
      this.sumbitRewards.rewardTypeName = latestReward.rewardTypes[0];
      this.sumbitRewards.managerId = filterEMP.managerIdForReward;
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
    if(this.isSearchEnabled == true){
      this.filters = searchData;
      console.log("Updated Filter : ", this.filters);
    }
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

        } else {
          this.rewardHistoryList = [];
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
              "From Date": x.fromDate || 'N/A',
              "To Date": x.toDate || 'N/A',
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

  deleteEmployeeRewardByRewardId(rewardID: any, template: TemplateRef<any>) {
    this.rewardsService.deleteEmployeeRewardByRewardId(rewardID).subscribe(
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
        this.openAlertMod(template, 'Error fetching reward categories. Please try again later.');
      }
    );
  }

}