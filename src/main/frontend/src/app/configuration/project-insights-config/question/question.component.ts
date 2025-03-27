import { Component, Input, OnInit } from '@angular/core';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { SurveyOption } from 'src/app/models/sureyOption';

@Component({
  selector: 'app-question',
  templateUrl: './question.component.html',
  styleUrls: ['./question.component.css']
})
export class QuestionComponent implements OnInit {

  @Input() group: any;
  @Input() mileIndex: number;

  constructor() { }

  ngOnInit(): void {
  }


  addQuestion(i: any) {
    this.group.projectQuestion.splice(i + 1, 0, new ProjectQuestion());
  }

  removeQuestion(i){
    this.group.projectQuestion.splice(i,1);
  }

  addOption(i, questionObj:ProjectQuestion){
    let question = this.group.projectQuestion.find(ques => ques == questionObj);
    question.optionsList.splice(i+1,0, new SurveyOption());
  }

  removeOption(i, questionObj:ProjectQuestion){
    let question = this.group.projectQuestion.find(ques => ques == questionObj);
    question.optionsList.splice(i,1);
  }

  setOption(questionObj:ProjectQuestion){
    if(questionObj.optionType == "checkbox" || questionObj.optionType == "radio"){
      let question = this.group.projectQuestion.find(ques => ques == questionObj);
      question.optionsList = [];
      question.optionsList.splice(1,0,new SurveyOption());
    }
  }
  

}
