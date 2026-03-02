export class SurveyQuestion{
    surveyQuestionId:any;
    surveyId:any;
    question:any;
    optionType:any;
    options:any;
    required:any = false;
    description:any;
    response:any;
    marksObtained?:number;
    correctAnswer?:string;
    name:any;
	employeementId:any;
    employmentIdAccToET:any;
    passStatus?: string;
    optionsList:any[]= [];
    cuttOffQuestions?:number;

    constructor(optionType?:'radio'){
        this.optionType = optionType;
    }
}
