import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Activity } from 'src/app/models/activity';
import { Feature } from 'src/app/models/feature';
import { Team } from 'src/app/models/team';
import { TeamMember } from 'src/app/models/teamMember';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-team-config',
  templateUrl: './team-config.component.html',
  styleUrls: ['./team-config.component.css']
})
export class TeamConfigComponent implements OnInit {

  feature="Team";
  currentUser:User;
  userMapping:any = {};

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;

  isTeamForm:boolean = false;
  isActivityForm:boolean = false;
  isTeamTable:boolean = false;
  isActivityTable:boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj 
  teamObj:Team = new Team();
  allTeamMembers:any[] = [];
  allTeamsList:any[] = [];

  activityObj:Activity = new Activity();
  allActivityList:any[] = [];

  employeeList:any[] = [];
  allClientList:any[] = [];
  allProjectList:any[] = [];

  selectedClient:any;
  selectedProject:any;
  selectedTeam:any;


  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    // Dynamic Subfeature Flags 
    // let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    // featureMap.subFeatures?.forEach(sub => {
    //   this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    // });
    // console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showCreateTeamForm();
  }

  showCreateTeamForm(){
    this.isTeamForm = true;
    this.isCreation = true;
    
    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isUpdation = false;

    this.reset();
  }

  showViewTeams(){
    this.isTeamTable = true;
    
    this.isTeamForm = false;
    this.isActivityForm = false;
    this.isActivityTable = false;
    this.isCreation = false;
    this.isUpdation = false;

  }

  showCreateActivityForm(){
    this.isActivityForm = true;
    this.isCreation = true;
    
    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityTable = false;
    this.isUpdation = false;

    this.reset();
  }

  showViewActivities(){
    this.isActivityTable = true;
    
    this.isTeamForm = false;
    this.isTeamTable = false;
    this.isActivityForm = false;
    this.isCreation = false;
    this.isUpdation = false;
  }

  reset(){
    this.teamObj = new Team();

    this.activityObj = new Activity();

    this.allTeamMembers = [];
    this.addInputTeamMemberField();
  }

  // Manage team members
  addInputTeamMemberField() {
    let newTeamMemberObj = new TeamMember();
    this.allTeamMembers.push(newTeamMemberObj);
  }

  removeInputTeamMemberField(teamMember) {
    this.allTeamMembers.forEach((value, index) => {
      if (value == teamMember) this.allTeamMembers.splice(index, 1);
    });
  }











  //modals
  openDeleteTeamMod(template: TemplateRef<any>, jobRole: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openDeleteActivityMod(template: TemplateRef<any>, jobRole: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
