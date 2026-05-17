import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RmgModalHostComponent } from './rmg-modal-host.component';

describe('RmgModalHostComponent', () => {
  let component: RmgModalHostComponent;
  let fixture: ComponentFixture<RmgModalHostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RmgModalHostComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RmgModalHostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
