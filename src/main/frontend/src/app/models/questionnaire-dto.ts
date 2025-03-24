// src/app/models/questionnaire-dto.ts

export class QuestionDTO {
  id?: number;
  questionText: string;

  constructor(id?: number, questionText: string = '') {
    this.id = id;
    this.questionText = questionText;
  }
}

export class QuestionnaireDTO {
  questionId?: number;
  questionTitle: string;
  questionDescription?: string;
  createdBy?: number;
  quarterId?: number;
  quarterCycle?: string;
  questions?: QuestionDTO[];
  departmentId?: number;
  department?: string;
  response: number;


}