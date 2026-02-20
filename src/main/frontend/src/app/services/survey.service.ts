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

    getAllSurveys(trainingId?: number, type: string = null) {
     let params: any = {};
   
     if (trainingId) {
            params.trainingId = trainingId;
          }
     if (type) {
            params.type = type;
          }
        
       return this.http.get(`${this.baseUrl}api/getAllSurveys`, { params });
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

    uploadImage(file: File) {
        const formData: FormData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.baseUrl}api/upload/uploadImage`, formData);
    }

    uploadVideo(file: File) {
        const formData: FormData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.baseUrl}api/upload/uploadVideo`, formData);
    }

    private _surveyObj: any;

    setSurveyData(surveyObj: any) {
        this._surveyObj = surveyObj;
        console.log("this._surveyObj get", this._surveyObj );
        
    }

    getSurveyData(): any {
        console.log("this._surveyObj set", this._surveyObj );
        return this._surveyObj;

  }

  getQuizQuestionByTrainingId(trainingId: number) {
    return this.http.get(`${this.baseUrl}api/training/getQuizQuestionByTrainingId/${trainingId}`);
  }

}