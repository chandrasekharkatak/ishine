import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360ProfileComponent } from './employee360-profile.component';

describe('Employee360ProfileComponent', () => {
  let component: Employee360ProfileComponent;
  let fixture: ComponentFixture<Employee360ProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360ProfileComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360ProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
