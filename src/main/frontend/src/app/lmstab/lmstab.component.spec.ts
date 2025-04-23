import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LmstabComponent } from './lmstab.component';

describe('LmstabComponent', () => {
  let component: LmstabComponent;
  let fixture: ComponentFixture<LmstabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LmstabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LmstabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
