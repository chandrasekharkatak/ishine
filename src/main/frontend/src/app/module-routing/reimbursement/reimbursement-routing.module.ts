import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyReimbursementComponent } from 'src/app/reimbursement/my-reimbursement/my-reimbursement.component';
import { ReimbursementComponent } from 'src/app/reimbursement/reimbursement.component';
import { ReimbursementapprovalComponent } from 'src/app/reimbursement/reimbursementapproval/reimbursementapproval.component';
import { TotalReimbursementrequestComponent } from 'src/app/reimbursement/total-reimbursementrequest/total-reimbursementrequest.component';
import { ViewReimbursementComponent } from 'src/app/reimbursement/view-reimbursement/view-reimbursement.component';

const routes: Routes = [{
  path: '', component: ReimbursementComponent,
  children: [
    { path: '', redirectTo: 'my-reimbursement', pathMatch: 'full' },
    { path: 'my-reimbursement', component: MyReimbursementComponent },
    { path: 'view-reimbursement', component: ViewReimbursementComponent },
    { path: 'approve-reimbursement', component: ReimbursementapprovalComponent },
    { path: 'total-reimbursement', component: TotalReimbursementrequestComponent }
  ]
},];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReimbursementRoutingModule { }
