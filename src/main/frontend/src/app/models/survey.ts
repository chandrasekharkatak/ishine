export class Survey {
    surveyId: any;
    empId:any;
    surveyName: any;
    isActive: any;
    description: any;
    createdOn: any;
    createdBy: any;
    updatedOn: any;
    updatedBy: any;
    surveyQuestionList:any;

    surveyTemplate:any;
    isAnswered:boolean = false;

    employeementId:any;
    type?: string; // 'quiz' or 'survey'
    
    // Training Quiz Mapping fields (for quiz creation from training)
    trainingId?: number;
    contentId?: number;
    isMandatory?: boolean;
    mustPassToComplete?: boolean;
}
