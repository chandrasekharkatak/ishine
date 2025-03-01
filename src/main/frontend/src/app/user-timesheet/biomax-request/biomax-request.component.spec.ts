import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BiomaxRequestComponent } from './biomax-request.component';

describe('BiomaxRequestComponent', () => {
  let component: BiomaxRequestComponent;
  let fixture: ComponentFixture<BiomaxRequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BiomaxRequestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BiomaxRequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
