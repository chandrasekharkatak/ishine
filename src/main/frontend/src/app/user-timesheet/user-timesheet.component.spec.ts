import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserTimesheetComponent } from './user-timesheet.component';

describe('UserTimesheetComponent', () => {
  let component: UserTimesheetComponent;
  let fixture: ComponentFixture<UserTimesheetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UserTimesheetComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserTimesheetComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
