import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ProjectInsightsConfigComponent } from './project-insights-config.component';

describe('ProjectInsightsConfigComponent', () => {
  let component: ProjectInsightsConfigComponent;
  let fixture: ComponentFixture<ProjectInsightsConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ProjectInsightsConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ProjectInsightsConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
