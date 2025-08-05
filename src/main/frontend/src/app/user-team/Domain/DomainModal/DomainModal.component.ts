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

  @Input() viewing = false;
  @Input() isEditing = false;
  @Input() isVisible = false;
  @Output() onClose = new EventEmitter<void>();
  @Input() incomingDomain: any = null;

  onCloseModal() {
    this.onClose.emit();
  }

  alertMessage = '';

  domain: Domain = {
    name: '',
    subDomains: [],
    services: [],
    isActive: true,
    type: 'domain',
    isOpen: true
  }

  deleteDomainData(id: number, type: string, domain: Domain | Service | SubDomain | SubService, isActive: boolean) {
    this.projectInsightDomainService.deleteDomainData(id, type).subscribe({
      next: (res: any) => {
        console.log("Deleted Domain: ", res);
        domain.isActive = !isActive
      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

  newData(data: any) {
    return !data.id;
  }

  ngOnInit() {
    if (this.incomingDomain) {
      this.domain = this.incomingDomain
    }
  }

  @ViewChild('domainCreatedModal') domainCreatedModal?: TemplateRef<any>;
  modalRef?: BsModalRef;

  createDomain() {
    this.loading = true;
    console.log("Domain: ", this.domain);
    if (this.isEditing) {
      this.projectInsightDomainService.editDomainData(this.domain, this.currUserId).subscribe({
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
    else {
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
  }

  toggleService(ind: number) {
    this.domain.services[ind].isOpen = !this.domain.services[ind].isOpen;
  }

  removeServices(ind: number) {
    this.domain.services.splice(ind, 1);
  }

  addSubServices(ind: number) {
    const subService: SubService = {
      isOpen: true,
      name: '',
      subServices: [],
      isActive: true,
      type: 'subService'
    }
    this.domain.services[ind].subServices.push(subService);
    this.domain.services[ind].isOpen = true
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
    const service: Service = {
      isOpen: true,
      name: '',
      isActive: true,
      subServices: [],
      type: 'service'
    }
    this.domain.services.push(service);
    this.domain.isOpen = true
  }

  addSubDomainList() {
    const subDomain: SubDomain = {
      name: '',
      subDomains: [],
      isActive: true,
      services: [],
      type: 'subDomain',
      isOpen: true
    }
    this.domain.subDomains.push(subDomain);
    this.domain.isOpen = true

  }

  changeSubDomain(event: Event, index: number) {
    this.domain.subDomains[index].name = (event.target as HTMLInputElement).value;
  }

  changeChildrenSubDomain(event: any, subDomainId: number, currChildrenSubDomainId: number) {
    this.domain.subDomains[subDomainId].subDomains[currChildrenSubDomainId].name = event.target.value;

  }

  addChildrenSubDomainList(i: number) {
    const subDomain: SubDomain = {
      isOpen: true,
      name: '',
      subDomains: [],
      isActive: true,
      services: [],
      type: 'subDomain'
    }
    this.domain.subDomains[i].subDomains.push(subDomain)
    this.domain.subDomains[i].isOpen = true
  }


}
