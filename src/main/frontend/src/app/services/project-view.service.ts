import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProjectViewService {

  private navigationSubject = new Subject<void>();

  constructor( private router: Router) { }

  getNavigationEvent() {
    return this.navigationSubject.asObservable();
  }

  navigateToProjectView(data: any) {
    this.router.navigate(['/project-view'], { state: { data } }).then(() => {
      this.navigationSubject.next();
    });
  }
}
