import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
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
}

@Component({
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
  modalRef: BsModalRef = new BsModalRef();

  constructor(
    private modalService: BsModalService,
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
    console.log("clone : ", clone);
    
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
          }
      });
    });
  }

  openTimesheetDetailsModal(template: TemplateRef<any>, dayDetails:any){
    this.cancelRequest();
    this.dayDetails = {}
    
    if(dayDetails.className == "in-month"){
      this.dayDetails = dayDetails;
      this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    }
  }
    

  cancelRequest() {
    this.modalRef.hide();
  }

}
