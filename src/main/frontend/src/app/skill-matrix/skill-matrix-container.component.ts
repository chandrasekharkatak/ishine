import { Component, OnInit } from '@angular/core';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  standalone: false,
  selector: 'app-skill-matrix-container',
  templateUrl: './skill-matrix-container.component.html',
  styleUrls: ['./skill-matrix-container.component.css']
})
export class SkillMatrixContainerComponent implements OnInit {

  currentUser: User = new User();
  /** Same pattern as Reimbursement: keys from sub_feature_master (skill_matrix_*). */
  userMapping: { [key: string]: boolean } = {};

  constructor(private authenticationService: AuthenticationService) {
    this.authenticationService.currentUser.subscribe(u => this.currentUser = u);
  }

  ngOnInit(): void {
    const feature = this.currentUser.userMapping?.find((f: Feature) => f.featureName === 'Skill Matrix');
    feature?.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  hasAnySkillMatrixAccess(): boolean {
    return !!(
      this.userMapping['skill_matrix_submit_for_review'] ||
      this.userMapping['skill_matrix_my_submissions'] ||
      this.userMapping['skill_matrix_approve_skill_requests'] ||
      this.userMapping['skill_matrix_master_configuration']
    );
  }
}
