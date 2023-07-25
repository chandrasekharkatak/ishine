import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Newsletter } from '../models/newsletter';

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
}
