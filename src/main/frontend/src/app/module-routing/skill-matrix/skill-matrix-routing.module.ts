import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SkillMatrixContainerComponent } from 'src/app/skill-matrix/skill-matrix-container.component';
import { SkillMatrixSubmitComponent } from 'src/app/skill-matrix/skill-matrix-submit.component';
import { SkillMatrixMySubmissionsComponent } from 'src/app/skill-matrix/skill-matrix-my-submissions.component';
import { SkillMatrixApproveRequestsComponent } from 'src/app/skill-matrix/skill-matrix-approve-requests.component';
import { SkillMatrixMasterConfigurationComponent } from 'src/app/skill-matrix/skill-matrix-master-configuration.component';

const routes: Routes = [{
  path: '',
  component: SkillMatrixContainerComponent,
  children: [
    { path: '', pathMatch: 'full', redirectTo: 'submit-for-review' },
    { path: 'submit-for-review', component: SkillMatrixSubmitComponent },
    { path: 'my-submissions', component: SkillMatrixMySubmissionsComponent },
    { path: 'approve-requests', component: SkillMatrixApproveRequestsComponent },
    { path: 'master-configuration', component: SkillMatrixMasterConfigurationComponent },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SkillMatrixRoutingModule { }
