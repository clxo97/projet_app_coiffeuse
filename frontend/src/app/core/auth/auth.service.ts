import { HttpClient } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';

import { environment } from '../../../environments/environment';
import { LoginRequest, LoginResponse } from './auth.models';

const TOKEN_KEY = 'app_coiffure_token';
const USER_KEY = 'app_coiffure_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  readonly currentUser = signal<LoginResponse | null>(this.readUser());

  constructor(private readonly http: HttpClient) {}

  login(credentials: LoginRequest) {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(response));
        this.currentUser.set(response);
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    const token = localStorage.getItem(TOKEN_KEY);

    if (token && this.isTokenExpired(token)) {
      this.logout();
      return null;
    }

    return token;
  }

  isAuthenticated(): boolean {
    return Boolean(this.getToken());
  }

  private readUser(): LoginResponse | null {
    const rawUser = localStorage.getItem(USER_KEY);
    return rawUser ? (JSON.parse(rawUser) as LoginResponse) : null;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = token.split('.')[1];

      if (!payload) {
        return true;
      }

      const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
      const paddedPayload = normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=');
      const claims = JSON.parse(atob(paddedPayload)) as { exp?: number };

      return typeof claims.exp !== 'number' || claims.exp <= Date.now();
    } catch {
      return true;
    }
  }
}
