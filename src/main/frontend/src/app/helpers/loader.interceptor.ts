import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoaderService } from '../services/loader.service';



@Injectable()
export class LoaderInterceptor implements HttpInterceptor {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  URL_whiteList = [

    `${this.baseUrl}` + `employeeportal/api/getAllEmployees`
    


  ]

  constructor(private loaderService: LoaderService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {


    this.URL_whiteList.forEach((element) => {


      if (element === request.url) {
        
        request = request.clone({
          setHeaders: {
            loader: 'true'
          }
        });
      }



    })

    if (request.headers.get('loader')) {
      this.loaderService.requestStarted();
      return this.handle(next, request);
    }
  
    return next.handle(request);

  }

  handle(next, request) {
    return next.handle(request).pipe(tap((event) => {

      if (event instanceof HttpResponse) {
        this.loaderService.requestEnded();
      }
    },
      (error: HttpErrorResponse) => {
        this.loaderService.resetSpinner();
        throw error;
      }

    ))
  }
}
