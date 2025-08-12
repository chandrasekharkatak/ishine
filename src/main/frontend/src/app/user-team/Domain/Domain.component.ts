import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { inject } from '@angular/core/testing';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { Domain, Service, SubDomain, SubService } from './Type';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-Domain',
  templateUrl: './Domain.component.html',
  styleUrls: ['./Domain.component.scss']
})
export class DomainComponent implements OnInit {

  constructor(private readonly projectInsightDomainService: ProjectInsightDomainService, private user: AuthenticationService, private modalService: BsModalService) {

  }

  currUserId = this.user.currentUserValue.empId;
  loading = false;

  isVisible = false
  domain: Domain = {
    name: '',
    isActive: true,
    subDomains: [],
    services: [],
    type: '',
    isOpen: true
  }

  ngOnInit() {
  }

  @ViewChild('domainCreatedModal') domainCreatedModal?: TemplateRef<any>;
  modalRef?: BsModalRef;

  openDomainCreatedModal() {
    if (!this.domainCreatedModal) {
      console.error("TemplateRef is undefined");
      return;
    }

    this.modalRef = this.modalService.show(this.domainCreatedModal, { class: 'modal-sm' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openDomainModal() {
    console.log("Domain: ", this.isVisible);

    this.isVisible = true;
  }

  refreshTable = false;

  onCloseModal() {
    this.isVisible = false;
    this.refreshTable = true
  }

  changeChildrenSubDomain(event: any, subDomainId: number, currChildrenSubDomainId: number) {
    this.domain.subDomains[subDomainId].subDomains[currChildrenSubDomainId].name = event.target.value;
  }

}
