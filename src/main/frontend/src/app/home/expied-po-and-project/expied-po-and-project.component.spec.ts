import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExpiedPoAndProjectComponent } from './expied-po-and-project.component';

describe('ExpiedPoAndProjectComponent', () => {
  let component: ExpiedPoAndProjectComponent;
  let fixture: ComponentFixture<ExpiedPoAndProjectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExpiedPoAndProjectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExpiedPoAndProjectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
