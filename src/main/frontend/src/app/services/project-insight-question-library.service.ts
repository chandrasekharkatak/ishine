import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { PageDTO } from '../models/pageDTO';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightQuestionLibraryService {

  private baseUrl: any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getAllProjectInsightQuestionEntriesByDepartment(questionLibraryEntryPage: PageDTO) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightQuestionEntriesByDepartment`, questionLibraryEntryPage);
  }

  getAllProjectInsightQuestionsEntry(questionLibraryEntryPage: PageDTO) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightQuestionsEntry`, questionLibraryEntryPage);
  }

  getAllProjectInsightQuestionEntriesByFilter(questionLibraryEntryPage: PageDTO) {
    return this.http.post(`${this.baseUrl}` + `api/getAllProjectInsightQuestionEntriesByFilter`, questionLibraryEntryPage);
  }

  saveProjectInsightQuestionLibraryEntry(questionLibraryEntry: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveProjectInsightQuestionLibraryEntry`, questionLibraryEntry);
  }

  saveEntryToQuestionLibraryFromExcel(obj: any) {
    return this.http.post(`${this.baseUrl}` + `api/saveEntryToQuestionLibraryFromExcel`, obj);
  }

  deleteProjectInsightQuestionLibraryEntryById(questionLibraryEntry: any) {
    return this.http.post(`${this.baseUrl}` + `api/deleteProjectInsightQuestionLibraryEntryById`, questionLibraryEntry);
  }

  searchQuestionLibrary(text: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/searchQuestionLibrary?text=${encodeURIComponent(text)}`);
  }

  getEntryFromsearchQuestionLibraryByText(id: string): Observable<any> {
    return this.http.get<any>(`/api/getEntryFromsearchQuestionLibraryByText?id=${encodeURIComponent(id)}`);
  }

}
