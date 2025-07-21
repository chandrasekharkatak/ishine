import { Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ProjectInsightDomainServiceService } from 'src/app/services/ProjectInsightDomainService.service';

@Component({
  selector: 'app-ViewDomain',
  templateUrl: './ViewDomain.component.html',
  styleUrls: ['./ViewDomain.component.scss']
})
export class ViewDomainComponent implements OnInit {

  @Input() domain: any = {}
  @Input() isEditing: boolean = false;
  @Output() onClose = new EventEmitter<void>()

  editDomain = null;
  toBeEdited: { parent_id: number; name: string; parent_id_name: string }[] = []

  constructor(private readonly projectInsightDomainService: ProjectInsightDomainServiceService, private modalService: BsModalService) { }

  ngOnInit() {
    if (this.domain) {
      this.editDomain = this.domain
      console.log("EditDomain: ", this.editDomain);
    }
  }

  @ViewChild('successAlert') deleteDomainConfirmation?: TemplateRef<any>;
  modalRef?: BsModalRef;

  message: string = null;

  saveDomain() {
    console.log("Saved Domain: ", this.toBeEdited);

    this.projectInsightDomainService.editDomainData(this.toBeEdited).subscribe({
      next: (res: any) => {
        console.log("Edited Domain: ", res);
        // this.getDomainData(this.editDomain.domain)
        this.message = "Domain edited successfully";
        this.openModal(this.deleteDomainConfirmation);
        this.onClose.emit();
      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

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


  deleteDomainData(id: number, type: string, domain: any, isActive: boolean) {
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

  onToggle(domain: any, isActive: boolean) {
    domain.isActive = !isActive
  }

  getDomainData(domain: string) {
    this.projectInsightDomainService.getDomain(domain).subscribe({
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

}
