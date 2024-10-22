import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Rewards } from 'src/app/models/rewards';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';

@Component({
  selector: 'app-rewards-and-recognisation',
  templateUrl: './rewards-and-recognisation.component.html',
  styleUrls: ['./rewards-and-recognisation.component.css']
})
export class RewardsAndRecognisationComponent implements OnInit {

  @ViewChild('alert_message') 
  alertMessageTemplate!: TemplateRef<any>;

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

  constructor(
    private locationStrategy: LocationStrategy,
    private authenticationService: AuthenticationService,
    private rewardsService: RewardsServiceService,
    private modalService: BsModalService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.preventBackButton();
    this.getRewardsCategories(this.alertMessageTemplate);
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
    this.rewardsService.getAllRewardsByCategoryId(categoryId).subscribe(
      (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.rewards = response.serviceResponse;
          const firstReward = this.rewards[0];
          if (firstReward) {
            this.selectReward(firstReward); 
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

  selectReward(reward: Rewards) {
    this.selectedReward = reward;
    console.log('Selected reward:', reward);
    this.selectedRewardType[reward.rewardName] = ''; 
    const rewardId = reward.id; 
    console.log('Reward ID:', rewardId);
    this.fetchEmployees(rewardId);
  }

  fetchEmployees(rewardId: number) {
    this.rewardsService.fetchEmployeesFromRewardCondition(rewardId).subscribe(
      (response: any) => {
        this.employees = response.serviceResponse.map(emp => {
          return {
              employeeNameForReward: emp.employeeName,  // Map to your model's property
              employeeIdForReward: emp.employeeEmpId,     // Assuming this is the ID for the employee
              managerNameForReward: emp.managerName,      // Map manager's name
              managerIdForReward: emp.managerEmpId,       // Map the manager's employee ID (adjust if different)
              rewardId: rewardId                           // Link to the selected reward ID
          } as Employee;  // Cast it to the Employee type
      });
        console.log(this.employees); // You can handle the employees data here
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
}
