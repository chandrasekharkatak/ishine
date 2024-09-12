export class SurveyQuestion{
    surveyQuestionId:any;
    surveyId:any;
    question:any;
    optionType:any;
    options:any;
    required:any = false;
    description:any;
    response:any;
    answer:any;
    name:any;
	employeementId:any;
    scaleMin: number = 1;
    scaleMax: number = 5;
    scaleLabelMin: string = 'Low';
    scaleLabelMax: string = 'High';
    optionsList:any[]= [];
    enableNavigation: boolean = false;
    goToSectionBasedOnAnswer: { [optionValue: string]: number | null } = {};
    image: string = '';
}