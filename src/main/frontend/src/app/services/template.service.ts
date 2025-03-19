import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { QuestionnaireDTO } from '../models/questionnaire-dto';

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private apiUrl = environment.baseUrl;

  constructor(private http: HttpClient) { }

  getAllQuestionnaires(): Observable<any> {
    return this.http.get(`${this.apiUrl}api/questionnaires/getAllQuestionnaires`);
  }

  getQuestionnaireById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}api/questionnaires/getQuestionnaireById/${id}`);
  }

  getQuestionsByQuarter(quarterId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}api/questionnaires/getQuestionnaireByQuarter/${quarterId}`);
  }

  createQuestionnaireTemplate(questionnaireData: QuestionnaireDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}api/questionnaires/createQuestionnaireTemplate`, questionnaireData);
  }

  updateQuestionnaire(questionId: number, questionnaireData: QuestionnaireDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}api/questionnaires/${questionId}`, questionnaireData);
  }

  deleteQuestionnaire(questionId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}api/questionnaires/deleteQuestionnaire/${questionId}`);
  }
}