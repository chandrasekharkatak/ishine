export class LMSResponse{
    status:boolean;
    message:any;
    Courses:Courses[]=[];
    Quizzes:Quizzes[]=[];
    Surveys:Surveys[]=[];

}
export class Courses{
    Name:any;
    Completion_Progress:any;

}
export class Quizzes{
    Name:any;
    No_Of_Attempts:any;
    Score:any;

}
export class Surveys{
    Name:any;
    Status:any;
}