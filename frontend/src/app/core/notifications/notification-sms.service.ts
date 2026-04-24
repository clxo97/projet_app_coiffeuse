import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { NotificationSms } from './notification-sms.models';

@Injectable({ providedIn: 'root' })
export class NotificationSmsService {
  private readonly apiUrl = `${environment.apiUrl}/notifications-sms`;

  constructor(private readonly http: HttpClient) {}

  list() {
    return this.http.get<NotificationSms[]>(this.apiUrl);
  }
}
