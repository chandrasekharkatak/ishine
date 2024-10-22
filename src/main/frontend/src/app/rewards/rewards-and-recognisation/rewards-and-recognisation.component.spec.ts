import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RewardsAndRecognisationComponent } from './rewards-and-recognisation.component';

describe('RewardsAndRecognisationComponent', () => {
  let component: RewardsAndRecognisationComponent;
  let fixture: ComponentFixture<RewardsAndRecognisationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RewardsAndRecognisationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RewardsAndRecognisationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
