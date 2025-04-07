import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Newsletter } from '../models/newsletter';
import { Document } from '../models/document';
import { Query } from '../models/query';

@Injectable({
  providedIn: 'root'
})
export class NewsletterService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  uploadNewsletter(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/newsletters/uploadNewsletter`,formData);
  }

  getAllNewsletters() {
    return this.http.get(`${this.baseUrl}` + `api/newsletters/`);
  }
  // added by anurag

  getAllNewslettersByTypeId(doc:Document) {
    return this.http.post(`${this.baseUrl}` + `api/newsletters/getDocumentByType`,doc);
  }

  deleteNewsletter(documentId:any) {
    return this.http.delete(`${this.baseUrl}` + `api/newsletters/${documentId}`);
  }

  downloadDocument(documentId: string) {
    return this.http.get(`${this.baseUrl}` + `api/newsletters/download/${documentId}`, {
      responseType: 'blob'
    });
  }

  onReadNewsletter(newsletter: Newsletter){
    return this.http.post(`${this.baseUrl}` + `api/newsletters/setNewsletterReadResponseByEmpId`, newsletter);

  }

  getAllReadNewslettersByEmpId(newsletter:Newsletter){
    return this.http.post(`${this.baseUrl}` + `api/newsletters/getAllReadNewslettersByEmpId`, newsletter);
  }

  // document Related API's added by anurag addTypeDocument
  addTypeDocument(document){
    return this.http.post(`${this.baseUrl}` + `api/addTypeDocument`, document);
  }

  checkTypeName(document){
    return this.http.post(`${this.baseUrl}` + `api/checkTypeName/${document}`, document);
  }

  getAllTypeName(){
    return this.http.get(`${this.baseUrl}` + `api/getAllTypeName`);
  }

  uploadDocument(formData:FormData){
    return this.http.post(`${this.baseUrl}`+`api/uploadDocument`,formData);
  }

  // deleteType
  deleteType(document){
    return this.http.post(`${this.baseUrl}`+`api/deleteType`,document);
  }

  getTypeById(type){
    return this.http.post(`${this.baseUrl}`+`api/getTypeById`,type);
  }

  // updateType

  updateType(type){
    return this.http.post(`${this.baseUrl}`+`api/updateType`,type);
  }

  // customQueryForDocument
  customQueryForDocument(queryObj: Query) {
    return this.http.post(`${this.baseUrl}` + `api/customQueryForDocument`, queryObj);

}

// saveDocQuery
saveDocQuery(){
  return this.http.get(`${this.baseUrl}`+ `api/saveDocQuery`);
}

// checkDocumentName

checkDocumentName(document: String) {
  return this.http.get(`${this.baseUrl}` + `api/newsletters/checkDocumentName/${document}`);

}

}