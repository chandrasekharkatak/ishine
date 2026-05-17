import { Component, Input, OnInit, TemplateRef, Output, EventEmitter, ViewChild } from '@angular/core';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Timesheet } from 'src/app/models/timesheet';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';

interface CalendarItem {
  day: string;
  dayName: string;
  className?: string;
  isWeekend: boolean;
  hour?: string;
  date: moment.Moment;

  displayDate?:string;
  dayType?: string;
  status?: string;
  workingHours?: number;
  description?: String;
  projectName?:String;
  activity? : String;
  projects?: ProjectItem[];
}

export interface ProjectItem{
  projectName?: string;  
  description?: string;
  activity?: ActivityItem[];
}

export interface ActivityItem{
  activityName?: string;
  description?: string;
}

@Component({
  standalone: false,
  selector: 'app-calendar',
  templateUrl: './calendar.component.html',
  styleUrls: ['./calendar.component.css']
})
export class CalendarComponent implements OnInit {
  @Input() subtractmonth = 0;
  @Input() timesheetDetails: Timesheet[] = [];
  date = moment();
  calendar: Array<CalendarItem[]> = [];
  dayDetails:any = {};
  modalRef:NgbModalRef;
  popupDescription : any;
  popupActivity: any;
  @Output() openTimesheet = new EventEmitter<CalendarItem>();
  @ViewChild('alert_message') alertMessageTemplate!: TemplateRef<any>;
  @Output() dateClicked = new EventEmitter<{ date: string, status: string }>();
  alertMessage: string = '';
  popupDatas: ProjectItem[] = [];
  @ViewChild('view_timesheet_details') viewTimesheetDetailsTemplate!: TemplateRef<any>;
  popUpDate: string = '';
   dateToDescription: { [key: string]: string[] } = {};
   dayType: string = '';
currentUser:User;
  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService
  ) { 
      this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.date.subtract(this.subtractmonth, 'months');
    this.calendar = this.createCalendar(this.date);
  }

  createCalendar(month: moment.Moment) {
    const date = month.clone();
    const daysInMonth = date.daysInMonth();
    const startOfMonth = date.startOf('months').format('ddd');
    const endOfMonth = date.endOf('months').format('ddd');
    const weekdaysShort = moment.weekdaysShort();
    const calendar: CalendarItem[] = [];

    const daysBefore = weekdaysShort.indexOf(startOfMonth);
    const daysAfter =
      weekdaysShort.length - 1 - weekdaysShort.indexOf(endOfMonth);

    const clone = date.startOf('months').clone();
    //console.log("clone : ", clone);

    if (daysBefore > 0) {
      clone.subtract(daysBefore, 'days');
    }

    for (let i = 0; i < daysBefore; i++) {
      calendar.push(this.createCalendarItem(clone, 'previous-month'));
      clone.add(1, 'days');
    }

    for (let i = 0; i < daysInMonth; i++) {
      calendar.push(this.createCalendarItem(clone, 'in-month'));
      clone.add(1, 'days');
    }

    for (let i = 0; i < daysAfter; i++) {
      calendar.push(this.createCalendarItem(clone, 'next-month'));
      clone.add(1, 'days');
    }

    return calendar.reduce(
      (pre: Array<CalendarItem[]>, curr: CalendarItem) => {
        if (pre[pre.length - 1].length < weekdaysShort.length) {
          pre[pre.length - 1].push(curr);
        } else {
          pre.push([curr]);
        }
        return pre;
      },
      [[]]
    );
  }

  createCalendarItem(data: moment.Moment, className: string) {
    const dayName = data.format('ddd');

    return {
      day: data.format('DD'),
      dayName,
      className,
      isWeekend: dayName === 'Sun' || dayName === 'Sat',
      date: data.clone(),
    };
  }

  // addTimesheetDetails(){
  //   const dateFormat = 'YYYY-MM-DD';

  //   this.calendar.forEach(element => {

  //     element.forEach(calendarItem => {
  //         const date = calendarItem.date?.format(dateFormat);

  //         let timesheet = this.timesheetDetails.find(timesheetObj => timesheetObj.date == date);
  //         if(timesheet){
  //           // calendarItem.displayDate = timesheet.date;
  //           // calendarItem.dayType = timesheet.dayType;
  //           // calendarItem.status = timesheet.status;
  //           // calendarItem.workingHours = timesheet.totalWorkingHours;
  //           // // calendarItem.description = timesheet.description;
  //           // // calendarItem.activity = timesheet.activity;
  //           // calendarItem.projects = timesheet.projects;

  //             const existingData: CalendarItem[] =this.calendar.length > 0 && this.calendar.find(element => element.find(calendarItem => calendarItem.date?.format(dateFormat) == date));
  //             if(existingData){
  //               existingData.forEach(calendarItem => {
  //                 const existingProject: ProjectItem = calendarItem.projects.find(project => project.projectName == timesheet.projectName);
  //                 if(existingProject){
  //                   const existingActivity = existingProject.activity.find(activity => activity.activityName == timesheet.activity);
  //                   if(!existingActivity  ){
  //                     calendarItem.projects.push({
  //                       projectName: existingProject.projectName,
  //                       activity: [{
  //                         activityName: timesheet.activity,
  //                         description: timesheet.description
  //                       }]
  //                     })
  //                   }
  //                 }
  //               })
  //             } else {
  //               calendarItem.displayDate = timesheet.date;
  //               calendarItem.dayType = timesheet.dayType;
  //               calendarItem.status = timesheet.status;
  //               calendarItem.workingHours = timesheet.totalWorkingHours;
  //               calendarItem.projects = [{
  //                 projectName: timesheet.projectName,
  //                 activity: [{
  //                   activityName: timesheet.activity,
  //                   description: timesheet.description
  //                 }]
  //               }]
  //             }
  //         }
  //     });
  //   });
  // }

  addTimesheetDetails(): void {
  if (!Array.isArray(this.timesheetDetails) || !this.calendar?.length) {
    return;
  }

  const dateFormat = 'YYYY-MM-DD';

  this.calendar.forEach(week => {
    week.forEach(day => {
      const dayDate = day.date?.format(dateFormat);
      if (!dayDate) return;

      const rows = this.timesheetDetails.filter(
        r => r.date === dayDate
      );

      if (rows.length === 0) return;

      if (!day.displayDate) {
        const base = rows[0];
        day.displayDate = base.date;
        day.dayType = base.dayType;
        day.status = base.status;
        day.workingHours = base.totalWorkingHours;
      }

      if (!day.projects) {
        day.projects = [];
      }

      rows.forEach(r => {
        if (!r.projectName){
          if(this.dateToDescription[r.date]){
            this.dateToDescription[r.date].push(r.description);
          }else{
            this.dateToDescription[r.date] = [r.description];
          }
        }

        let project = day.projects!.find(
          p => p.projectName === r.projectName
        );

        if (!project) {
          project = {
            projectName: r.projectName,
            description: r.description,
            activity: []
          };
          day.projects!.push(project);
        }

        if (r.activity) {
          const activityExists = project.activity!.some(
            a =>
              a.activityName === r.activity &&
              a.description === r.description
          );

          if (!activityExists) {
            project.activity!.push({
              activityName: r.activity,
              description: r.description
            });
          }
        }
      });
    });
  });

  console.log("This calender: ", this.calendar);
  

}

  showViewTimesheetDetails(day: CalendarItem){
    this.popupDatas = [];
    this.isPopupVisible = true;
    this.popUpDate = day.date.format('DD-MM-YYYY');
    this.dayType = day.dayType

    for (const d of day.projects) {
      this.popupDatas.push({
        projectName: d.projectName,
        description: d.description,
        activity: d.activity
      });
    }
    this.modalRef = this.modalService.open(this.viewTimesheetDetailsTemplate, { modalDialogClass: 'modal-sm' });
  }



  openTimesheetDetailsModal(template: TemplateRef<any>, dayDetails:any){
    this.cancelRequest();
    this.dayDetails = {}

    if(dayDetails.className == "in-month"){
      this.dayDetails = dayDetails;
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-md' });
    }
  }


  cancelRequest() {
    this.modalRef?.close();
  }

  isPopupVisible = false;
  pointerX = 0;
  pointerY = 0;

  onHoverStart(event: MouseEvent, day: CalendarItem): void {
    this.popupDatas = [];
    this.isPopupVisible = true;
    this.popUpDate = day.date.toString();

    for (const d of day.projects) {
      this.popupDatas.push({
        projectName: d.projectName,
        activity: d.activity
      });
    }

    // Store mouse position for popup
    // this.pointerX = event.clientX + 10;
    // this.pointerY = event.clientY + 10;
    // this.showViewTimesheetDetails();
    
  }


  hideViewTimesheetDetails(){
    this.isPopupVisible = false;
    this.modalRef?.close();
  }

onHoverEnd(): void {
    this.isPopupVisible = false;
    this.popupDescription = '';
    this.popupActivity = '';
}

todayDate: string = new Date().toISOString().split('T')[0];
sixDaysBefore = new Date(Date.now() - 6 * 24 * 60 * 60 * 1000)
  .toISOString()
  .split('T')[0];

onDayClick(day: CalendarItem): void {
  console.log("dayyyy",day);
  if (day.status === 'Not Filled') {
    if(this.currentUser.isTimesheetLockCheckEnable == 'true' && this.sixDaysBefore > day.date.format('YYYY-MM-DD') ){
     this.alertMessage = 'Timesheet is Locked for the selected date.';
    this.modalRef = this.modalService.open(this.alertMessageTemplate, {
      modalDialogClass: 'modal-md',
      backdrop: 'static',
      keyboard: false
    });
    return;
    }
    this.openTimesheet.emit(day);
  } else if (day.status === 'Approved') {
    this.alertMessage = 'Timesheet is already approved.';
    this.modalRef = this.modalService.open(this.alertMessageTemplate, {
      modalDialogClass: 'modal-md',
      backdrop: 'static',
      keyboard: false
    });
  } else if (day.status === 'Pending') {
    this.alertMessage = 'Timesheet is already filled. Please update it.';
    this.modalRef = this.modalService.open(this.alertMessageTemplate, {
      modalDialogClass: 'modal-md',
      backdrop: 'static',
      keyboard: false
    });

  }else if (day.status === 'Rejected') {
    this.alertMessage = 'Timesheet is already Rejected. Please fill it.';
    this.modalRef = this.modalService.open(this.alertMessageTemplate, {
      modalDialogClass: 'modal-md',
      backdrop: 'static',
      keyboard: false
    });
  }

    this.dateClicked.emit({
    date: day.date.format('YYYY-MM-DD'),
    status: day.status
  });



}




  cancelRequest_approve_pending(): void {
      this.modalRef?.close();
  }

  parseDateString(dateStr: string): Date {
    const [day, month, year] = dateStr.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

}

