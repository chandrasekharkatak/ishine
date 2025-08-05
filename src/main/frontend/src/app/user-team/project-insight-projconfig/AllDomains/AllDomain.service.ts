import { Injectable } from '@angular/core';
import { AllDomainsI } from './AllDomains.component';

@Injectable({
  providedIn: 'root'
})
export class AllDomainService {

  allDomains:AllDomainsI[] = []

  setAllDomains(allDomains:AllDomainsI[]) {
    this.allDomains = allDomains
  }

  getAllDomains() {
    return this.allDomains
  }

}
