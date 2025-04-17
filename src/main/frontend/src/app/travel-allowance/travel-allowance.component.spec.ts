import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TravelAllowanceComponent } from './travel-allowance.component';

describe('TravelAllowanceComponent', () => {
  let component: TravelAllowanceComponent;
  let fixture: ComponentFixture<TravelAllowanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TravelAllowanceComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TravelAllowanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
