import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyReimbursementComponent } from './my-reimbursement.component';

describe('MyReimbursementComponent', () => {
  let component: MyReimbursementComponent;
  let fixture: ComponentFixture<MyReimbursementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyReimbursementComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyReimbursementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
