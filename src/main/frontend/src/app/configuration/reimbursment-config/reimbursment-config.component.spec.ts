import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReimbursmentConfigComponent } from './reimbursment-config.component';

describe('ReimbursmentConfigComponent', () => {
  let component: ReimbursmentConfigComponent;
  let fixture: ComponentFixture<ReimbursmentConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReimbursmentConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReimbursmentConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
