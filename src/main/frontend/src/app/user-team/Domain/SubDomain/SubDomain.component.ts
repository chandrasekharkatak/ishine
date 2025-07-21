import { Component, Input, OnInit, Output } from '@angular/core';
import { SubDomain } from '../Type';

@Component({
  selector: 'app-SubDomain',
  templateUrl: './SubDomain.component.html',
  styleUrls: ['./SubDomain.component.scss']
})
export class SubDomainComponent implements OnInit {
  @Input() subDomainList: SubDomain[] = [];

  ngOnInit() {
  }

  removeSubDomain(ind:number){
    this.subDomainList.splice(ind, 1)
  }

  toggleSubDomain(ind: number) {
    this.subDomainList[ind].isOpen = !this.subDomainList[ind].isOpen
  }

  addChildrenSubDomainList(ind: number) {
    this.subDomainList[ind].subDomainChildrenList.push({ subdomain: '', subDomainChildrenList: [], serviceList: [],isOpen: true })
  }

  removeChildrenSubDomainList(ind: number, jnd: number) {
    this.subDomainList[ind].subDomainChildrenList.splice(jnd, 1)
  }

  addServices(ind: number) {
    this.subDomainList[ind].serviceList.push({isOpen: true, service: '', subServiceList: [] })
  }

  removeServices(ind: number, jnd: number) {
    this.subDomainList[ind].serviceList.splice(jnd, 1)
  }

  addSubServices(ind: number, jnd: number) {
    this.subDomainList[ind].serviceList[jnd].subServiceList.push({isOpen: true, subService: '', subServiceChildren: [] })
  }

  removeSubServices(ind: number, jnd: number, knd: number) {
    this.subDomainList[ind].serviceList[jnd].subServiceList.splice(knd, 1)
  }

  toggleService(ind: number, jnd: number) {
    
    this.subDomainList[ind].serviceList[jnd].isOpen = !this.subDomainList[ind].serviceList[jnd].isOpen
    console.log("this.subDomainList[ind].serviceList[jnd].isOpen: ", this.subDomainList[ind].serviceList[jnd].isOpen);
    
  }

}
