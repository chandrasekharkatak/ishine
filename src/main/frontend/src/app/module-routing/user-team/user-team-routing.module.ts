import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { MyTeamComponent } from 'src/app/user-team/my-team/my-team.component';
import { ResourceManagementComponent } from 'src/app/user-team/resource-management/resource-management.component';
import { TeamConfigComponent } from 'src/app/user-team/team-config/team-config.component';
import { TeamMemberComponent } from 'src/app/user-team/team-member/team-member.component';
import { UserTeamComponent } from 'src/app/user-team/user-team.component';

const routes: Routes = [
  {
    path: '', component: UserTeamComponent,
    children: [
      { path: 'my-team', component: MyTeamComponent, },
      { path: 'team-member', component: TeamMemberComponent, },
      { path: 'team-config', component: TeamConfigComponent, },
      { path: 'resource-management', component: ResourceManagementComponent, },
      { path: 'resource-management/:id', component: ResourceManagementComponent, },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserTeamRoutingModule { }
