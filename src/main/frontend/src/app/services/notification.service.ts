import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { NotificationMessage } from '../models/notification';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  addNotification(notificationObj:NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/addNotification`, notificationObj);
  }

  updateNotification(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/updateNotification`, notificationObj);
  }

  deleteNotification(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/deleteNotification`, notificationObj);
  }

  getAllNotifications() {
    return this.http.get(`${this.baseUrl}` + `api/getAllNotifications`);
  }

  getNotificationById(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/getNotificationById`, notificationObj);
  }

  onDeleteNotification(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/onDeleteNotification`, notificationObj);
  }

  onInActivateNotification(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/onInActivateNotification`, notificationObj);
  }

  submitNotificationConsent(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/submitNotificationConsent`, notificationObj);
  }

  getConsentNotificationResponse(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/getConsentNotificationResponse`, notificationObj);
  }

  getAllNotificationsByNotificationTypeAndEmpId(notificationObj: NotificationMessage) {
    return this.http.post(`${this.baseUrl}` + `api/getAllNotificationsByNotificationTypeAndEmpId`, notificationObj);
  }
}
