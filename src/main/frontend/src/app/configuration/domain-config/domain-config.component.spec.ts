import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainConfigComponent } from './domain-config.component';

describe('DomainConfigComponent', () => {
  let component: DomainConfigComponent;
  let fixture: ComponentFixture<DomainConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DomainConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
