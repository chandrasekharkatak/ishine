import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MyTimesheetHeaderComponent } from './my-timesheet-header.component';

describe('MyTimesheetHeaderComponent', () => {
  let component: MyTimesheetHeaderComponent;
  let fixture: ComponentFixture<MyTimesheetHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyTimesheetHeaderComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyTimesheetHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});


