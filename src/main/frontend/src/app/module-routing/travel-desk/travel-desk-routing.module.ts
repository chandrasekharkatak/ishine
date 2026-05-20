import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyTravelrequestComponent } from 'src/app/travel-allowance/my-travelrequest/my-travelrequest.component';
import { TotalTravelrequestComponent } from 'src/app/travel-allowance/total-travelrequest/total-travelrequest.component';
import { TravelAllowanceComponent } from 'src/app/travel-allowance/travel-allowance.component';
import { TravelrequestapprovalComponent } from 'src/app/travel-allowance/travelrequestapproval/travelrequestapproval.component';
import { ViewTravelrequestComponent } from 'src/app/travel-allowance/view-travelrequest/view-travelrequest.component';

const routes: Routes = [{
  path: '', component: TravelAllowanceComponent,
  children: [
    { path: 'my-travelrequest', component: MyTravelrequestComponent },
    { path: 'view-travelrequest', component: ViewTravelrequestComponent },
    { path: 'approve-travelrequest', component: TravelrequestapprovalComponent },
    { path: 'total-travelrequest', component: TotalTravelrequestComponent }
  ]
},];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class TravelDeskRoutingModule { }
