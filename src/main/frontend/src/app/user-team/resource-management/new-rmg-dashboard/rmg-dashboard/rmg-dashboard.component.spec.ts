import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RmgDashboardComponent } from './rmg-dashboard.component';

describe('RmgDashboardComponent', () => {
  let component: RmgDashboardComponent;
  let fixture: ComponentFixture<RmgDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RmgDashboardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RmgDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
