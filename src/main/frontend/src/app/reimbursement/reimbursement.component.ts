import { Component, OnInit } from '@angular/core';
import { Feature } from '../models/feature';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { ReimbursementService } from '../services/reimbursement.service';

@Component({
  standalone: false,
  selector: 'app-reimbursement',
  templateUrl: './reimbursement.component.html',
  styleUrls: ['./reimbursement.component.css']
})
export class ReimbursementComponent implements OnInit {

  currentUser: User;
  feature = "Reimbursement";
  userMapping: any = {};
  showApplyReimbursementTab = true;

   constructor(
      private authenticationService: AuthenticationService,
      private reimbursementService: ReimbursementService,
    ) { 
      this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.reimbursementService.getReimbursementSubmissionWindowStatus().subscribe({
      next: (res: any) => {
        if (res?.serviceStatus === 'Success' && res.serviceResponse) {
          this.showApplyReimbursementTab = res.serviceResponse.allowed !== false;
        }
      }
    });
  }

}
