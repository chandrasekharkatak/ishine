
export class QuestionnaireDTO {
  questionId?: number;
  questionTitle: string;
  questionDescription?: string;
  createdBy?: number;
  quarterId?: number;
  quarterCycle?: string;
  departmentId?: number;
  department?: string;
  response: number;
  questions: {
    id?: number;
    questionText: string;
  }[];


}