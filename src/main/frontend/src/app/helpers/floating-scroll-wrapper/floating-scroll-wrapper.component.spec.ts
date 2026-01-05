import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FloatingScrollWrapperComponent } from './floating-scroll-wrapper.component';

describe('FloatingScrollWrapperComponent', () => {
  let component: FloatingScrollWrapperComponent;
  let fixture: ComponentFixture<FloatingScrollWrapperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FloatingScrollWrapperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FloatingScrollWrapperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
