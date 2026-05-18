import { Directive, Input, HostListener } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { environment } from 'src/environments/environment';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ToastService } from './services/toast.service';

@Directive({
  standalone: false,
  selector: '[navigateToProjectView]'
})
export class NavigateToProjectViewDirective {

  @Input('navigateToProjectView') 
  data: any; 

  private baseUrl: any = environment.baseUrl;

  constructor(
    private router: Router,
    private http: HttpClient,
    private toastService: ToastService
  ) { }

  @HostListener('click') async onClick() {
    if (this.data) {
      let resolvedProjectViewId = this.data;
      try {
        const params = new HttpParams().set('projectViewId', String(this.data));
        const response: any = await firstValueFrom(
          this.http.get(`${this.baseUrl}` + `api/resolveProjectViewId`, { params })
        );
        const dto = response?.serviceResponse;
        if (response?.serviceStatus === 'Success' && dto?.resolvedProjectViewId) {
          resolvedProjectViewId = dto.resolvedProjectViewId;
          if (dto.redirected === true) {
            this.toastService.info(
              'This project has been linked. Redirecting to primary project.',
              'Redirect'
            );
          }
        }
      } catch (e) {
        // fall back to original id
        resolvedProjectViewId = this.data;
      }

      const urlTree = this.router.createUrlTree(['/project-view'], {
        queryParams: { projectId: resolvedProjectViewId }
      });
      const serializedUrl = this.router.serializeUrl(urlTree);
      const base = `${window.location.origin}${window.location.pathname}`;
      const fullUrl = `${base}#${serializedUrl}`;
      window.open(fullUrl, '_blank');
    }
  }
}
