import { Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { Domain, SubDomain, Service, SubService } from '../Type';
import { ProjectInsightDomainServiceService } from 'src/app/services/ProjectInsightDomainService.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-DomainModal',
  templateUrl: './DomainModal.component.html',
  styleUrls: ['./DomainModal.component.scss']
})
export class DomainModalComponent implements OnInit {

  constructor(private readonly projectInsightDomainService: ProjectInsightDomainServiceService, private user: AuthenticationService, private modalService: BsModalService) { }

  currUserId = this.user.currentUserValue.empId;
  loading = false;


  @Input() isVisible = false;
  @Output() onClose = new EventEmitter<void>();
  @Input() incomingDomain:any = null;

  onCloseModal() {
    this.onClose.emit();
  }

  alertMessage = '';

  domain: Domain = {
    domain: '',
    subDomainList: [],
    serviceList: []
  }

  ngOnInit() {
    if(this.incomingDomain){
      this.domain = this.incomingDomain
    }
  }

  @ViewChild('domainCreatedModal') domainCreatedModal?: TemplateRef<any>;
  modalRef?: BsModalRef;

  createDomain() {
    this.loading = true;
    this.projectInsightDomainService.createDomain(this.domain, this.currUserId).subscribe({
      next: (res: any) => {
        this.alertMessage = "Domain created successfully";
        this.openDomainCreatedModal();
        this.onCloseModal();
      }, error: (error: any) => {
        console.error("Error: ", error);
      }, complete: () => {
        this.loading = false;
      }
    })
  }

  toggleService(ind: number) {
    this.domain.serviceList[ind].isOpen = !this.domain.serviceList[ind].isOpen;
  }

  removeServices(ind:number){
    this.domain.serviceList.splice(ind, 1);
  }

  addSubServices(ind:number){
    const subService:SubService = {
      isOpen: true,
      subService: '',
      subServiceChildren: []
    }
    this.domain.serviceList[ind].subServiceList.push(subService);
  }

  openDomainCreatedModal() {
    if (!this.domainCreatedModal) {
      console.error("TemplateRef is undefined");
      return;
    }

    this.modalRef = this.modalService.show(this.domainCreatedModal, { class: 'modal-sm' });
  }

  cancelRequest() {
    this.onCloseModal();
    this.modalRef.hide();
  }

  addServiceList() {
    const service:Service = {
      isOpen: true,
      service: '',
      subServiceList: []
    }
    this.domain.serviceList.push(service);
  }

  addSubDomainList() {
    const subDomain: SubDomain = {
      subdomain: '',
      subDomainChildrenList: [],
      serviceList: [],
      isOpen: true
    }
    this.domain.subDomainList.push(subDomain);

  }

  changeSubDomain(event: Event, index: number) {
    this.domain.subDomainList[index].subdomain = (event.target as HTMLInputElement).value;
  }

  changeChildrenSubDomain(event: any, subDomainId: number, currChildrenSubDomainId: number) {
    this.domain.subDomainList[subDomainId].subDomainChildrenList[currChildrenSubDomainId].subdomain = event.target.value;

  }

  addChildrenSubDomainList(i: number) {
    const subDomain: SubDomain = {
      isOpen: true,
      subdomain: '',
      subDomainChildrenList: [],
      serviceList: []
    }
    this.domain.subDomainList[i].subDomainChildrenList.push(subDomain)
  }


}
