import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DocumentUploadComponent } from 'src/app/user-update-info/document-upload/document-upload.component';
import { EmployeeInfoComponent } from 'src/app/user-update-info/employee-info/employee-info.component';
import { InformationPreviewComponent } from 'src/app/user-update-info/information-preview/information-preview.component';
import { UserUpdateInfoComponent } from 'src/app/user-update-info/user-update-info.component';

const routes: Routes = [{
  path: '', component: UserUpdateInfoComponent,
  children: [
    { path: 'employee-info', component: EmployeeInfoComponent, },
    { path: 'document-upload', component: DocumentUploadComponent, },
    { path: 'info-preview', component: InformationPreviewComponent, },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UpdateInfoRoutingModule { }
