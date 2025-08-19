import { Component, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';

@Component({
  selector: 'app-question-tabs',
  templateUrl: './question-tabs.component.html',
  styleUrls: ['./question-tabs.component.css']
})
export class QuestionTabsComponent implements OnInit {
  @Input() project: any;
  @Input() showGroupCard: any;
  @Input() currentNode: any;
  @Input() currentNodeType:string = 'Group';
  @ViewChild('Open_Question_Overview') quesDetailView!: TemplateRef<any>;
  @ViewChild('alert_message') alertMessageTemplate!: TemplateRef<any>;
  @ViewChild('ask_confirmation') confirmation!: TemplateRef<any>;
  @ViewChild('ask_level_confirmation') levelConfirmation!: TemplateRef<any>;

  alertModalRef: BsModalRef = new BsModalRef();

  alertMessage: string = '';
  alertMessage2: string = '';
  alertMessage3: string = '';

  currentUser: any;
  isCurrentNodeGroup: boolean = false;
  viewMode: 'View' | 'Edit' | 'Create' = 'View';

  // project: any = null;
  selectedProj: any = null;

  // showGroupCard: any = null;
  selectedGroup: any = null;
  allChildsRecursiv:boolean = false;

  constructor(
    private authenticationService: AuthenticationService,
    public projectService: ProjectService,
    private projectInsightService: ProjectInsightService,
    private modalService: BsModalService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
      
  }

  onGetProjectInsightGroupDetailsByObjectId(event: any): void {
    console.log('Fetch group details for objectId:', event);
    // Assign dummy group for now
    this.currentNodeType = 'Group';
    this.isCurrentNodeGroup = true;
    this.showGroupCard = {
      groupTitle: 'Sample Group',
      groupType: 'Main Group'
    };
    this.selectedGroup = { draftCount: 2 };
  }

  onGetProjectInsightDetailsByObjectId(): void {
    console.log('Fetch project insight details');
    this.currentNodeType = 'Project';
    // this.project = {
    //   projectName: 'Demo Project',
    //   departments: [{ name: 'IT' }, { name: 'HR' }],
    //   projectManagerName: 'John Doe',
    //   client: { clientName: 'XYZ Corp' },
    //   apmosysRM: 'Apmosys RM Name',
    //   clientRM: 'Client RM Name',
    //   additionalInfo: {
    //     location: 'Mumbai',
    //     tags: ['Agile', 'Migration']
    //   }
    // };

    this.selectedProj = { draftCount: 3 };
  }

  formatKey(key: unknown): string {
    if (typeof key !== 'string') {
      return '';
    }
    return key
      .replace(/[_\-]/g, ' ')
      .replace(/([a-z])([A-Z])/g, '$1 $2')
      .replace(/\b\w/g, char => char.toUpperCase());
  }
  
  isArray(value: unknown): value is any[] {
    return Array.isArray(value);
  }

castToArray(value: unknown): any[] {
  return Array.isArray(value) ? value : [];
}

sendQuestionsForApproval(project:any){
  let counts = this.projectService.projectMap.get(project.projectId);
  if(counts?.totalCount == 0){
    this.alertMessage = 'No Questions Present in this group';
      this.alertModalRef = this.modalService.show(this.alertMessageTemplate);
  }else if(counts?.pendingCount > 0){
    this.alertMessage2 = 'Some Questions Are not answered in this group Do you wish to submit only answered questions for review and leave remaining one ?';
      this.alertModalRef = this.modalService.show(this.confirmation);
  }else{
    this.askLevelApproval();
  }
}

askLevelApproval(){
  this.cancelRequest();
  this.alertMessage3 = 'Do You Wish to Submit Group Level Questions Only or send All Questions recursively from All child groups also?';
  this.alertModalRef = this.modalService.show(this.levelConfirmation);
}

saveConsent(consent:boolean){
  this.cancelRequest();
  this.allChildsRecursiv = consent;
  this.sendAnsweredforApproval(this.project)
}

sendAnsweredforApproval(proj:any){
  let request = {
    empId: this.currentUser.empId,
    parentType:this.currentNodeType,
    parentId: proj.id,
    toAllChilds : this.allChildsRecursiv
  }
  //Call Api to assign reviewer for all answers of all questions in that group one level or AllLevel? 
  this.projectService.assignQuestionsToReviewers(request).pipe(first()).subscribe({
    next: (res: any) => {
      this.cancelRequest();
      // this.groupbrowser.loadQuestionsByGroupOrProjectId(proj.projectId,'Project');
      // this.groupbrowser.sendForUpdate(this.project,3);
      this.alertMessage = res;
      this.alertModalRef = this.modalService.show(this.alertMessageTemplate);
    },
    error: (error: any) => {
      this.cancelRequest();
      this.alertMessage = error;
      this.alertModalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
    }
  });
}

openAlertModal(message: any) {
  this.alertMessage = message;
  this.alertModalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
}

cancelRequest() {
  if (this.alertModalRef) {
    this.alertModalRef.hide();
  }
}

}
