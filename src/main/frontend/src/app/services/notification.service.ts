import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NotificationMessage } from '../models/notification';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  /* Leave */
  addNotification(notificationObj:NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `/api/addNotification`, notificationObj);
  }

  updateNotification(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `/api/updateNotification`, notificationObj);
  }

  deleteNotification(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `/api/deleteNotification`, notificationObj);
  }

  getAllNotifications() {
    return this.http.get(`${this.baseUrl}` + `/api/getAllNotifications`);
  }
}
