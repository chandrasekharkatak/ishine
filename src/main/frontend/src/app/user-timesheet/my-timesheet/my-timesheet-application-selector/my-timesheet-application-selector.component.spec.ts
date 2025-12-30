import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { MyTimesheetApplicationSelectorComponent } from './my-timesheet-application-selector.component';

describe('MyTimesheetApplicationSelectorComponent', () => {
  let component: MyTimesheetApplicationSelectorComponent;
  let fixture: ComponentFixture<MyTimesheetApplicationSelectorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [MyTimesheetApplicationSelectorComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyTimesheetApplicationSelectorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});


