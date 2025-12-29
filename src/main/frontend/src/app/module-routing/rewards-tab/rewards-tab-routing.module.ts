import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AppreciationComponent } from 'src/app/rewards/appreciation/appreciation.component';
import { RewardsAndRecognisationComponent } from 'src/app/rewards/rewards-and-recognisation/rewards-and-recognisation.component';
import { RewardsComponent } from 'src/app/rewards/rewards.component';

const routes: Routes = [{
  path: '', component: RewardsComponent,
  children: [
    { path: 'rewards-and-recognisation', component: RewardsAndRecognisationComponent, },
    { path: 'rewardsappreciation', component: AppreciationComponent, },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RewardsTabRoutingModule { }
