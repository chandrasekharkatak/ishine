import { Component, Input, OnInit, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectInsightDomainServiceService } from 'src/app/services/ProjectInsightDomainService.service';
import { SubDomain, SubService } from '../../Type';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Sort } from '@angular/material/sort';
import { DatePipe } from '@angular/common';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  selector: 'app-DomainTables',
  templateUrl: './DomainTables.component.html',
  styleUrls: ['./DomainTables.component.scss']
})
export class DomainTablesComponent implements OnInit {

  isSearchEnabled = false;
  page = 1;
  limit = 10;
  allDomainData: any[] = [];
  totalItems = 0;
  totalCount = 0;

  selectedDomain: any = null

  isVisible = false;
  isEditing = false;

  @Input() refreshTable = false;

  onCloseModal() {
    this.isVisible = false;
    this.selectedDomain = null
    this.isEditing = false
  }

  constructor(private user: AuthenticationService,private readonly projectInsightDomainService: ProjectInsightDomainServiceService, private modalService: BsModalService) { }

  ngOnInit() {
    // if(this.refreshTable){
    //   console.log("refreshTable: ", this.refreshTable);
      
    //   this.getAllProjectInsightDomain();
    // }
    this.getAllProjectInsightDomain();
  }

  ngOnChanges(changes: SimpleChanges) {
    console.log("changes: ", changes["refreshTable"]);
    if (changes['refreshTable'] && changes['refreshTable'].currentValue) {
      this.getAllProjectInsightDomain();
    } else{
      this.getAllProjectInsightDomain();
    }
  }

  @ViewChild('deleteDomainConfirmation') deleteDomainConfirmation?: TemplateRef<any>;
  modalRef?: BsModalRef;

  openDeleteModal(domain: any, event: Event): void {
    event.preventDefault();
    this.selectedDomainToDelete = domain

    this.modalRef = this.modalService.show(this.deleteDomainConfirmation);
  }

  originalData: any[] = [];

  getAllProjectInsightDomain(params?: any) {
    this.projectInsightDomainService.getAllDomain(this.page-1, this.limit, params).subscribe({
      next: (res: any) => {
        console.log("allDomainData: ", this.allDomainData);
        this.totalItems = res.totalElements;
        this.totalCount = res.totalPages;
        this.allDomainData = res.content;
        if (!this.originalData || this.originalData.length === 0) {
          this.originalData = [...res.content]
        }
        this.sortData({
          active: `${this.sortColumn}|${this.sortColumnType}`,
          direction: this.sortDirection
        });
      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

  viewDomain(domain: any) {
    this.isVisible = true;
    this.isEditing = false;
    this.getDomain(domain);
    console.log(this.selectedDomain);

  }

  getDomain(domain: any): any {
    this.projectInsightDomainService.getDomain(domain.domain).subscribe({
      next: (res: any) => {
        this.selectedDomain = {
          ...res,
          isOpen: false,
          subDomains: this.addIsOpenToSubDomains(res.subDomains),
          services: res.services.filter(service => this.isEditing ? true : service.isActive).map(service => ({
            ...service,
            isOpen: false,
            subServices: this.addIsOpenToSubServices(service.subServices)
          }))
        }
        console.log("selectedDomain: ", this.selectedDomain);

      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

  addIsOpenToSubDomains(subDomains: SubDomain[]): SubDomain[] {
    return subDomains.filter(subDomain => this.isEditing ? true : subDomain.isActive).map(subDomain => ({
      ...subDomain,
      isOpen: false,
      children: this.addIsOpenToSubDomains(subDomain.children),
      services: subDomain.services.filter(service => this.isEditing ? true : service.isActive).map(service => ({
        ...service,
        isOpen: false,
        subServices: this.addIsOpenToSubServices(service.subServices)
      }))
    }));
  }

  addIsOpenToSubServices(subServices: SubService[]): SubService[] {
    return subServices.filter(subService => this.isEditing ? true : subService.isActive).map(subService => ({
      ...subService,
      isOpen: false,
      children: this.addIsOpenToSubServices(subService.children)
    }));
  }


  editDomain(domain: any) {
    this.isVisible = true;
    this.isEditing = true;
    this.getDomain(domain);
    console.log("selectedDomain: ", this.selectedDomain);

  }
  toggle(domain: any) {
    console.log("Domain: ", domain);

    domain.isOpen = !domain.isOpen
  }

  selectedDomainToDelete: any = null;
  selectedId: number = null;

  deleteDomain() {
    console.log("selectedDomainToDelete: ", this.selectedDomainToDelete);

    this.projectInsightDomainService.deleteDomainData(null, "domain", this.selectedDomainToDelete.domain)
      .subscribe({
        next: (res: any) => {
          console.log("Deleted Domain: ", res);
          this.allDomainData = this.allDomainData.map((d: any) => {
            if (d.domain === this.selectedDomainToDelete.domain) {
              return { ...d, isActive: false };
            }
            return d;
          })
          this.modalRef?.hide();
          this.selectedDomainToDelete = null
          this.getAllProjectInsightDomain()
        },
        error: (error: any) => {
          console.error("Error: ", error);
        }
      })
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;

    if (!this.isSearchEnabled) {
      console.log("Original Data: ", this.originalData);

      this.allDomainData = [...this.originalData];
    }
  }

  sortColumn: string = 'domain';
  sortColumnType: string = 'string';
  sortDirection: 'asc' | 'desc' = 'asc';

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction === '' ? 'asc' : sort.direction;

      this.allDomainData.sort((a, b) => {
        return this.compare(a, b, this.sortColumn, this.sortColumnType, this.sortDirection);
      });
    }
  }

  currUserId = this.user.currentUserValue.empId;

  updateIsApprovedDomain(domainId:number, status:string){
    this.projectInsightDomainService.approveDomain(domainId,status,this.currUserId).subscribe({
      next: (res: any) => {
        console.log("Approved Domain: ", res);
        this.getAllProjectInsightDomain()
      },
      error: (error: any) => {
        console.error("Error: ", error);
      }
    })
  }

  private compare(a: any, b: any, field: string, type: string, direction: string): number {
    let comparison = 0;
    const valueA = this.getFieldValue(a, field);
    const valueB = this.getFieldValue(b, field);

    if (valueA == null && valueB == null) return 0;
    if (valueA == null) return direction === 'asc' ? 1 : -1;
    if (valueB == null) return direction === 'asc' ? -1 : 1;

    if (type === 'string') {
      const strA = String(valueA);
      const strB = String(valueB);
      comparison = strA.localeCompare(strB);
    } else if (type === 'date') {
      const dateA = valueA instanceof Date ? valueA : new Date(valueA);
      const dateB = valueB instanceof Date ? valueB : new Date(valueB);
      comparison = dateA.getTime() - dateB.getTime();
    } else if (type === 'number') {
      const numA = Number(valueA);
      const numB = Number(valueB);
      comparison = numA - numB;
    } else {
      const strA = String(valueA);
      const strB = String(valueB);
      comparison = strA.localeCompare(strB);
    }

    return direction === 'asc' ? comparison : -comparison;
  }

  domainColumns = [
    { field: 'domain', header: 'Domain Name', type: 'string' },
    { field: 'createdBy', header: 'Created By', type: 'string' },
    { field: 'createdOn', header: 'Created At', type: 'date' },
    { field: 'isActive', header: 'Status', type: 'boolean' },
    { field: 'isApproved', header: 'Approved Status', type: 'string' }
  ];

  domainSearchFilterColumn = ['blank', "domain", "createdBy", "createdOn", "isActive",'isApproved', 'blank'];

  datePipe = new DatePipe('en-US');

  onDomainSearch(filters: any) {

    if (filters?.createdOn) {
      // Format as "YYYY-MM-DDTHH:mm:ss" (ISO format without milliseconds)
      filters.createdOn = new Date(filters.createdOn).toISOString().split('.')[0];
    }
    console.log("filters: ", filters);

    this.getAllProjectInsightDomain(filters);

    this.sortData({
      active: `${this.sortColumn}|${this.sortColumnType}`,
      direction: this.sortDirection
    });

    console.log("Filtered and sorted data: ", this.allDomainData);
  }

  private getFieldValue(obj: any, field: string): any {
    return field.split('.').reduce((o, i) => o?.[i], obj);
  }

  onSearch(searchData: any) {
  }

  getName(name:string){
    if(!name){
      return '-';
    }
    if(name.length > 15){
      return name.substring(0, 14) + '...';
    }
    return name;
  }

  handlePageChange(event:any) {
    this.page = event;
    this.getAllProjectInsightDomain();
  }

}
