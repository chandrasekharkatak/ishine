import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyResignationComponent } from 'src/app/user-exit/my-resignation/my-resignation.component';
import { ResignationComponent } from 'src/app/user-exit/resignation/resignation.component';
import { UserExitComponent } from 'src/app/user-exit/user-exit.component';

const routes: Routes = [{
  path: '', component: UserExitComponent,
  children: [
    { path: 'my-resignation', component: MyResignationComponent, },
    { path: 'my-resignation/:id', component: MyResignationComponent, },
    { path: 'resignation', component: ResignationComponent, },
  ]
}];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserExitRoutingModule { }
