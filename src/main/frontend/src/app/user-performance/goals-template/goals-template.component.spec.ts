import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalsTemplateComponent } from './goals-template.component';

describe('GoalsTemplateComponent', () => {
  let component: GoalsTemplateComponent;
  let fixture: ComponentFixture<GoalsTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GoalsTemplateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GoalsTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
