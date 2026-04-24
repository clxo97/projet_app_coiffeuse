import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { ChangePasswordRequest, CoiffeuseAccount, SmsConfig, SmsTemplatesRequest } from './account.models';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly apiUrl = `${environment.apiUrl}/coiffeuse`;

  constructor(private readonly http: HttpClient) {}

  getAccount() {
    return this.http.get<CoiffeuseAccount>(`${this.apiUrl}/me`);
  }

  getSmsConfig() {
    return this.http.get<SmsConfig>(`${this.apiUrl}/me/sms-config`);
  }

  changePassword(request: ChangePasswordRequest) {
    return this.http.patch<void>(`${this.apiUrl}/me/password`, request);
  }

  updateSmsTemplates(request: SmsTemplatesRequest) {
    return this.http.patch<CoiffeuseAccount>(`${this.apiUrl}/me/sms-templates`, request);
  }
}
