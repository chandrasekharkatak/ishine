import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { InterviewTrackerComponent } from 'src/app/interview-tracker/interview-tracker.component';

const routes: Routes = [{
    path: '', component: InterviewTrackerComponent
}];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class InterviewTrackerRoutingModule { }
