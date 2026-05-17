import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyTimesheetComponent } from './my-timesheet.component';

describe('MyTimesheetComponent', () => {
  let component: MyTimesheetComponent;
  let fixture: ComponentFixture<MyTimesheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyTimesheetComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyTimesheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

/*
1)During update data should be auto filled 
2)Updated data should conditional updatable
3)
*/
