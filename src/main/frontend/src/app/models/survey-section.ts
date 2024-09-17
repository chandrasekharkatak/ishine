import { SurveyQuestion } from "./surveyQuestion";

export class SurveySection {
    sectionName: string = '';
    questions: SurveyQuestion[] = [new SurveyQuestion()];
    goToSection: string | null = null; 
}
