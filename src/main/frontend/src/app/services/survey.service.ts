import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Survey } from "../models/survey";
import { environment } from "src/environments/environment";

@Injectable({
    providedIn: 'root'
})
export class SurveyService {

    private baseUrl: any = environment.baseUrl;

    constructor(private http: HttpClient) { }
 
    createSurvey(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/createSurvey`, surveyObj);
    }
    
    updateSurvey(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/updateSurvey`, surveyObj);
    }

    deleteSurvey(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/deleteSurvey`, surveyObj);
    }

    changeSurveyStatus(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/changeSurveyStatus`, surveyObj);
    }

    getAllSurveys() {
        return this.http.get(`${this.baseUrl}` + `api/getAllSurveys`);
    }

    getAllQuestionsBySurveyId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/getAllQuestionsBySurveyId`, surveyObj);
    }

    setSurveyResponseByEmpId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/setSurveyResponseByEmpId`, surveyObj);
    }

    getAnsweredSurveysByEmpId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/getAnsweredSurveysByEmpId`, surveyObj);
    }

    getSurveyResponseByEmpIdAndSurveyId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/getSurveyResponseByEmpIdAndSurveyId`, surveyObj);
    }

    getSurveyAllResponsesBySurveyId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `api/getSurveyAllResponsesBySurveyId`, surveyObj);
    }
}