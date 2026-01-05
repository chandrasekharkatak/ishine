import { Component, Input, OnInit, TemplateRef, Output, EventEmitter, ViewChild } from '@angular/core';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Timesheet } from 'src/app/models/timesheet';

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

  constructor(
    private modalService: NgbModal,
  ) { }

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

  addTimesheetDetails(){
    const dateFormat = 'YYYY-MM-DD';

    this.calendar.forEach(element => {

      element.forEach(calendarItem => {
          const date = calendarItem.date?.format(dateFormat);

          let timesheet = this.timesheetDetails.find(timesheetObj => timesheetObj.date == date);
          if(timesheet){
            calendarItem.displayDate = timesheet.date;
            calendarItem.dayType = timesheet.dayType;
            calendarItem.status = timesheet.status;
            calendarItem.workingHours = timesheet.totalWorkingHours;
            calendarItem.description = timesheet.description;
            calendarItem.activity = timesheet.activity;


          }
      });
    });
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

  // onHoverStart(description: any): void {
  //   this.isPopupVisible = true;
  //   this.popupDescription = description;
  //   console.log(description);

  // }

  // onHoverEnd(description : any): void {
  //   this.isPopupVisible = false;
  //   this.popupDescription = '';
  // }



  // onHoverStart(event: MouseEvent, description: any): void {
  //   this.isPopupVisible = true;
  //   this.popupDescription = description;
  //   this.pointerX = event.clientX + 10; // Add offset for better positioning
  //   this.pointerY = event.clientY + 10;
  // }

  // onHoverEnd(): void {
  //   this.isPopupVisible = false;
  //   this.popupDescription = '';
  // }

  onHoverStart(event: MouseEvent, description: any, activity: any ): void {
    this.isPopupVisible = true;
    this.popupDescription = description;
    this.popupActivity = activity;
    this.pointerX = event.clientX + 10; // Offset for positioning
    this.pointerY = event.clientY + 10;
}

onHoverEnd(): void {
    this.isPopupVisible = false;
    this.popupDescription = '';
    this.popupActivity = '';
}

todayDate: string = new Date().toISOString().split('T')[0];

onDayClick(day: CalendarItem): void {
  console.log("dayyyy",day);
  if (day.status === 'Not Filled') {
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

}

