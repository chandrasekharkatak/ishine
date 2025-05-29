import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TotalReimbursementrequestComponent } from './total-reimbursementrequest.component';

describe('TotalReimbursementrequestComponent', () => {
  let component: TotalReimbursementrequestComponent;
  let fixture: ComponentFixture<TotalReimbursementrequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TotalReimbursementrequestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TotalReimbursementrequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
