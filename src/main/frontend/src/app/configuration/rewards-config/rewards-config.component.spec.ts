import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RewardsConfigComponent } from './rewards-config.component';

describe('RewardsConfigComponent', () => {
  let component: RewardsConfigComponent;
  let fixture: ComponentFixture<RewardsConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RewardsConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RewardsConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
