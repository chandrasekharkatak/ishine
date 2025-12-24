import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DomainComponent } from 'src/app/user-team/Domain/Domain.component';
import { FormBuilderComponent } from 'src/app/user-team/form-builder/form-builder.component';
import { KnowledgeHubComponent } from 'src/app/user-team/KnowledgeHub/KnowledgeHub.component';
import { ProjectInsightDetailsComponent } from 'src/app/user-team/project-insight/components/project-insight-details/project-insight-details.component';
import { ProjectInsightQuestionLibraryComponent } from 'src/app/user-team/project-insight/components/project-insight-question-library/project-insight-question-library.component';
import { ProjectInsightComponent } from 'src/app/user-team/project-insight/project-insight.component';

const routes: Routes = [{
  path: '', component: ProjectInsightComponent,
  children: [{
    path: 'project-insight-details', component: ProjectInsightDetailsComponent,
    children: [
      { path: 'department-forms', component: FormBuilderComponent },
      { path: "knowledge-hub", component: KnowledgeHubComponent },
      { path: "domains", component: DomainComponent },
      { path: 'question-library', component: ProjectInsightQuestionLibraryComponent },
    ]
  }],
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProjectInsightRoutingModule { }
