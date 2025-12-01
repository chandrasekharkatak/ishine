import { formatDate } from '@angular/common';
import { Component, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';

interface Activity {
  id: string;
  actor: string;
  action: 'submitted' | 'approved' | 'edited_and_forwarded' | 'rejected' | 'rated' | 'reassigned' | 'published';
  message?: string;
  rating?: number;
  toStage?: string;
  timestamp: string;
}

@Component({
  standalone: false,
  selector: 'app-approver-workflow',
  templateUrl: './approver-workflow.component.html',
  styleUrls: ['./approver-workflow.component.css']
})
export class ApproverWorkflowComponent {
  @Input() selectedQuesDetails:any;
  @Output() afterSubmitApproval = new EventEmitter<any>();
  @Output() showAlertMessage = new EventEmitter<any>();
  stages: string[] = ['L1', 'L2', 'L3', 'L4'];
  stageIndex = 0;
  published = false;

  question:any;
  quesDescription:any;
  responder:any = 'Priya Sharma (Delivery)';
  submittedAt:any = '04 Sep 2025, 11:30';

  answer:any;
  originalAnswer:any;

  rating:number = 0;
  quality: string = '';
  reviewerName: string = '';
  reassignReason:any;
  remarks = '';
  editBuffer = '';
  isEditing = false;
  enableApprovalButtons:boolean = false;
  currentUser:any;

  activity: Activity[] = [
    {
      id: crypto.randomUUID(),
      actor: this.responder,
      action: 'submitted',
      message: 'Initial response submitted',
      timestamp: this.submittedAt,
    }
  ];

  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.stages=[];
    if(this.selectedQuesDetails.response.reviewerInfo!=null){
      this.selectedQuesDetails.response.reviewerInfo.forEach(review=>{
        this.stages.push('L-'+review.level);
      });
    }
    this.question = this.selectedQuesDetails.question.question;
    this.quesDescription = this.selectedQuesDetails.question.description;
    this.originalAnswer=this.answer;
    this.answer = this.question?.optionType=='text'?this.stripHtml(this.selectedQuesDetails.response.response):this.selectedQuesDetails.response.response;
    this.originalAnswer=this.answer;
    this.responder = this.selectedQuesDetails.response?.responseByEmpName+'(Answered)'
    let dateStr = this.selectedQuesDetails.response?.lastSavedOn;
    this.submittedAt = formatDate(dateStr, 'dd MMM yyyy, HH:mm', 'en-US');
    let n = this.selectedQuesDetails.response.reviewerInfo.length;
    this.setApprovalDetails(n-1,this.selectedQuesDetails.response.reviewerInfo[n-1]);
    if(this.selectedQuesDetails.response.reviewerInfo.length==2 && this.selectedQuesDetails.response.reviewerInfo[1].isApproved==true){
      this.published=true;
    }
  }

  setApprovalDetails(index:number,reviewData:any){
    this.enableApprovalButtons=false;
    this.reassignReason=null;
    this.stageIndex=index;
    this.remarks = reviewData?.remarks;
    this.rating = reviewData?.marks!=null?reviewData?.marks:0;
    this.quality = reviewData?.quality!=null?reviewData?.quality:'';
    this.reviewerName = reviewData?.reviewerName;
    this.reassignReason = reviewData?.reassignReason;
    if(reviewData?.reviewerid == this.currentUser.empId && this.stageIndex==this.selectedQuesDetails.response.reviewerInfo.length-1){
      this.enableApprovalButtons=true;
    }
  }

  convertToHtml(text: string): string {
    if (!text) return '';
    return text
      .split('\n')
      .map(line => line.trim() ? `<div>${line}</div>` : '<div><br></div>')
      .join('');
  }

  stripHtml(html: string): string {
    if (!html) return '';
    return html
      .replace(/<div>/gi, '\n')
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/<\/div>/gi, '')
      .replace(/<[^>]+>/g, '')
      .trim();
  }

  get answerEdited(): boolean {
    return this.answer.trim() !== this.originalAnswer.trim();
  }

  updateSlider(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.rating = +input.value;
    const percent = (this.rating / +input.max) * 100;
    input.style.background = `linear-gradient(to right, #3f51b5 ${percent}%, #d3d3d3 ${percent}%)`;
  }

  goToStage(index: number) {
    this.stageIndex = index;
    this.setApprovalDetails(index,this.selectedQuesDetails.response?.reviewerInfo[index]);
  }  

  toggleEdit() {
    this.isEditing = !this.isEditing;
    this.editBuffer = this.isEditing ? this.answer : '';
  }

  setRating(val: number) {
    this.rating = val;
  }

  saveApprovalConsents(type: any) {
    let n = this.selectedQuesDetails?.response?.reviewerInfo.length;
  
    if (n && n > 0) {
      let reviewer = this.selectedQuesDetails.response.reviewerInfo[n - 1];
  
      if (type === 'approve') {
        reviewer.marks = this.rating;
        reviewer.remarks = this.remarks;
        reviewer.quality = this.quality;
        reviewer.isApproved = true;
  
      } else if (type === 'edit') {
        reviewer.marks = this.rating;
        this.remarks +=
          '(Edited by ' +
          this.currentUser.name +
          ' at level-' +
          reviewer?.level +
          '.)';
        reviewer.remarks = this.remarks;
        reviewer.quality = this.quality;
        reviewer.isApproved = true;
        this.selectedQuesDetails.response.response =
          this.question?.optionType == 'text'
            ? this.convertToHtml(this.answer)
            : this.answer;
  
      } else if (type === 'reject') {
        reviewer.remarks = this.remarks;
        reviewer.isApproved = false;
      }
  
      let Payload = {
        approvalType: type,
        quesAns: this.selectedQuesDetails,
      };
  
      this.afterSubmitApproval.emit(Payload);
    }
  }  

  approveForward() {
    this.saveApprovalConsents('approve');
  }

  editForward() {
    if(this.answer==this.originalAnswer){
      this.showAlertMessage.emit('Edited Answer is exactly same with original Answer.');
    }else{
      this.saveApprovalConsents('edit');
    }
  }

  reject() {
    this.saveApprovalConsents('reject');
  }

  private log(a: Partial<Activity>) {
    this.activity.unshift({
      id: crypto.randomUUID(),
      actor: `${this.stages[this.stageIndex]} Approver`,
      action: a.action!,
      message: a.message,
      rating: a.rating,
      toStage: a.toStage,
      timestamp: new Date().toLocaleString(),
    });
  }

  label(action: Activity['action']): string {
    switch (action) {
      case 'submitted': return 'submitted the response';
      case 'approved': return 'approved';
      case 'edited_and_forwarded': return 'edited & forwarded';
      case 'rejected': return 'rejected';
      case 'rated': return 'rated';
      case 'reassigned': return 'reassigned';
      case 'published': return 'published';
    }
  }
}
