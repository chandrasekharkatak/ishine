import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Survey } from "../models/survey";

@Injectable({
    providedIn: 'root'
})
export class SurveyService {

    private baseUrl: any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

    constructor(private http: HttpClient) { }
 
    createSurvey(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `employeeportal/api/createSurvey`, surveyObj);
    }

    getAllSurveys() {
        return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllSurveys`);
    }

    getAllQuestionsBySurveyId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAllQuestionsBySurveyId`, surveyObj);
    }

    setSurveyResponseByEmpId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `employeeportal/api/setSurveyResponseByEmpId`, surveyObj);
    }

    getAnsweredSurveysByEmpId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `employeeportal/api/getAnsweredSurveysByEmpId`, surveyObj);
    }

    getSurveyResponseByEmpIdAndSurveyId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `employeeportal/api/getSurveyResponseByEmpIdAndSurveyId`, surveyObj);
    }

    getSurveyAllResponsesBySurveyId(surveyObj: Survey) {
        return this.http.post(`${this.baseUrl}` + `employeeportal/api/getSurveyAllResponsesBySurveyId`, surveyObj);
    }
}