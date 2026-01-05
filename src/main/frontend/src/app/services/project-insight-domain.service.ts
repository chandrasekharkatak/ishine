import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Domain } from '../user-team/Type';
import { environment } from 'src/environments/environment';
import { AllDomainsI } from '../user-team/project-insight/components/all-project-insight-domains/all-project-insight-domains.component';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightDomainService {
  baseUrl = environment.baseUrl;
  allDomains: AllDomainsI[] = []
  newDomains: AllDomainsI[] = []

  constructor(private readonly http: HttpClient) { }

  setAllDomains(allDomains: AllDomainsI[]) {
    this.allDomains = allDomains
  }

  setNewDomains(newDomains: AllDomainsI[]) {
    this.newDomains = newDomains
  }

  getNewDomains() {
    return this.newDomains
  }

  getAllDomains() {
    return this.allDomains
  }

  searchDomain(query:string){
    return this.http.get(this.baseUrl + "api/searchDomain", {
      params: {
        query
      }
    });
  }

  getDomainSearchRecommendation(query:string){
    return this.http.get(this.baseUrl + "api/getDomainRecommendation", {
      params: {
        query
      }
    });
  }

  getAllProjectInsightDomain(ids: any) {
    if (!Array.isArray(ids)) ids = [ids]
    return this.http.post(this.baseUrl + "api/get-all-project-insight-domains", ids);
  }

  createDomain(domain: Domain, createdBy: number) {
    return this.http.post(this.baseUrl + "api/create-project-insight-domain", domain, {
      params: {
        createdBy
      }
    });
  }

  editDomain(data: any) {
    return this.http.put(this.baseUrl + "api/add-new-sub-domain", data);
  }

  getAllDomain(page: number, limit: number, params?: any) {
    return this.http.get(this.baseUrl + "api/get-project-domain?page=" + page + "&limit=" + limit, {
      params: params
    });
  }

  getDomain(domain: string) {
    return this.http.get(this.baseUrl + "api/get-domain", {
      params: {
        domain
      }
    });
  }

  getAllDomainList() {
    return this.http.get(this.baseUrl + "api/getAllDomains");
  }

  deleteDomainData(id: number, type: string, name?: string) {
    if (id == null) {
      id = 0
    }
    return this.http.delete(this.baseUrl + "api/delete-domain-data", {
      params: {
        id,
        type,
        name,
      },
    });
  }

  editDomainData(data: any, currUserId: number) {
    return this.http.put(this.baseUrl + "api/edit-domains", data, { params: { createdBy: currUserId } });
  }

  approveDomain(id: number, status: string, approvedBy: number) {
    return this.http.put(this.baseUrl + "api/approve-domain", {}, {
      params: {
        id,
        isApproved: status,
        approvedBy,
      }
    });
  }

  loadAllFilters() {
    return this.http.get(this.baseUrl + "api/load-all-filters");
  }


  getDomainHierarchy() {
    return this.http.get(this.baseUrl + "api/getDomainHierarchy");
  }

}
