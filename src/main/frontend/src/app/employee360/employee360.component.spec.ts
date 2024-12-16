import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360Component } from './employee360.component';

describe('Employee360Component', () => {
  let component: Employee360Component;
  let fixture: ComponentFixture<Employee360Component>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360Component ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360Component);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
