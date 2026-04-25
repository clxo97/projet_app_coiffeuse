import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { AdminCoiffeuse, AdminUpdateSubscriptionRequest } from './admin.models';

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly apiUrl = `${environment.apiUrl}/admin`;

  constructor(private readonly http: HttpClient) {}

  getCoiffeuses() {
    return this.http.get<AdminCoiffeuse[]>(`${this.apiUrl}/coiffeuses`);
  }

  updateSubscription(coiffeuseId: number, request: AdminUpdateSubscriptionRequest) {
    return this.http.patch<AdminCoiffeuse>(`${this.apiUrl}/coiffeuses/${coiffeuseId}/subscription`, request);
  }
}
