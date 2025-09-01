import { Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { Domain, Service, SubDomain, SubService } from '../Type';
import { first } from 'rxjs/operators';

@Component({
  selector: 'app-ViewDomain',
  templateUrl: './ViewDomain.component.html',
  styleUrls: ['./ViewDomain.component.scss']
})
export class ViewDomainComponent implements OnInit {

  @Input() domain: Domain = null;
  @Input() isEditing: boolean = false;
  @Output() onClose = new EventEmitter<void>()

  editDomain = null;
  toBeEdited: { parent_id: number; name: string; parent_id_name: string }[] = []

  constructor(private readonly projectInsightDomainService: ProjectInsightDomainService, private modalService: BsModalService) { }

  ngOnInit() {
    if (this.domain) {
      this.editDomain = this.domain
      console.log("EditDomain: ", this.editDomain);
    }
  }

  @ViewChild('successAlert') deleteDomainConfirmation?: TemplateRef<any>;
  modalRef?: BsModalRef;

  message: string = null;

  changeInput(event: Event, id: number, type: string) {
    const value = (event.target as HTMLInputElement).value;

    const existing = this.toBeEdited.find(
      item => item.parent_id === id && item.parent_id_name === type
    );

    if (existing) {
      existing.name = value;
    } else {
      this.toBeEdited.push({ parent_id: id, name: value, parent_id_name: type });
    }
  }

  openModal(template: TemplateRef<any>) {
    console.log("Template: ", template);

    this.modalRef = this.modalService.show(template, {class : 'modal-sm'});
  }


  deleteDomainData(id: number, type: string, domain: Domain | Service| SubDomain | SubService, isActive: boolean) {
    this.projectInsightDomainService.deleteDomainData(id, type).pipe(first()).subscribe({
      next: (res: any) => {
        console.log("Deleted Domain: ", res);
        domain.isActive = !isActive
      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

  onToggle(domain: Domain | Service| SubDomain | SubService, isActive: boolean) {
    domain.isActive = !isActive
  }

  getDomainData(name: string) {
    this.projectInsightDomainService.getDomain(name).pipe(first()).subscribe({
      next: (res: any) => {
        console.log("Domain: ", res);
        this.editDomain = res
      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

  closeModal() {
    this.onClose.emit();
  }

  toggle(domain: any) {
    domain.isOpen = !domain.isOpen
  }

  // addChildrenSubDomainList(i: number) {
  //     const subDomain: SubDomain = {
  //       isOpen: true,
  //       subdomain: '',
  //       subDomainChildrenList: [],
  //       serviceList: []
  //     }
  //     this.domain.subDomainList[i].subDomainChildrenList.push(subDomain)
  // }

  // addChildrenServiceList(i: number, j: number) {
  //   const service: Service = {
  //     isOpen: true,
  //     service: '',
  //     subServiceList: []
  //   }
  //   this.domain.subDomainList[i].subDomainChildrenList[j].serviceList.push(service)
  // }

  // addSubServiceList(i: number, j: number,parent:string, k?: number) {
  //   const subService: SubService = {
  //     isOpen: true,
  //     subService: '',
  //     subServiceChildren: []
  //   }
  //   if(parent === 'domain' ){
  //     this.domain.service
  //   }
  //   this.domain.subDomainList[i].subDomainChildrenList[j].serviceList[i].subServiceList.push(subService);
  // }

}
