import { Component, Input, OnInit, Output } from '@angular/core';
import { Domain, SubDomain, Service, SubService } from '../Type';
import { ProjectInsightDomainServiceService } from 'src/app/services/ProjectInsightDomainService.service';

@Component({
  selector: 'app-SubDomain',
  templateUrl: './SubDomain.component.html',
  styleUrls: ['./SubDomain.component.scss']
})
export class SubDomainComponent implements OnInit {
  @Input() subDomains: SubDomain[] = [];
  @Input() isViewing = false;
  @Input() isEditing = false;

  ngOnInit() {
    console.log("subDomainList: ", this.subDomains);
  }

  constructor(private readonly projectInsightDomainService: ProjectInsightDomainServiceService) { }

  removeSubDomain(ind:number){
    this.subDomains.splice(ind, 1)
  }

  toggleSubDomain(ind: number) {
    this.subDomains[ind].isOpen = !this.subDomains[ind].isOpen
  }

  newData(data:any){
    return !data.id;
  }

  addChildrenSubDomainList(ind: number) {
    this.subDomains[ind].isOpen = true
    this.subDomains[ind].subDomains.push({ name: '',isActive: true, subDomains: [], services: [],isOpen: true,type: 'subDomain' })
  }

  removeChildrenSubDomainList(ind: number, jnd: number) {
    this.subDomains[ind].subDomains.splice(jnd, 1)
  }

  addServices(ind: number) {
    this.subDomains[ind].isOpen = true
    this.subDomains[ind].services.push({isOpen: true,isActive: true, name: '', subServices: [], type: 'service'})
  }

  removeServices(ind: number, jnd: number) {
    this.subDomains[ind].services.splice(jnd, 1)
  }

  addSubServices(ind: number, jnd: number) {
    this.subDomains[ind].services[jnd].isOpen = true
    this.subDomains[ind].services[jnd].subServices.push({isOpen: true,isActive: true, name: '', subServices: [], type: 'subService'})
  }

  removeSubServices(ind: number, jnd: number, knd: number) {
    this.subDomains[ind].services[jnd].subServices.splice(knd, 1)
  }

  toggleService(ind: number, jnd: number) {
    this.subDomains[ind].services[jnd].isOpen = !this.subDomains[ind].services[jnd].isOpen
  }

  deleteDomainData(id: number, type: string, domain: Domain | Service| SubDomain | SubService, isActive: boolean) {
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

}
