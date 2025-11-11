import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectInsightsTabComponent } from './project-insights-tab.component';

describe('ProjectInsightsComponent', () => {
  let component: ProjectInsightsTabComponent;
  let fixture: ComponentFixture<ProjectInsightsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectInsightsTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInsightsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
