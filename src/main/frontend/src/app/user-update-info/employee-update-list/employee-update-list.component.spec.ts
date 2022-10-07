import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeUpdateListComponent } from './employee-update-list.component';

describe('EmployeeUpdateListComponent', () => {
  let component: EmployeeUpdateListComponent;
  let fixture: ComponentFixture<EmployeeUpdateListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmployeeUpdateListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EmployeeUpdateListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
