import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { Client, ClientRequest } from './client.models';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly apiUrl = `${environment.apiUrl}/clients`;

  constructor(private readonly http: HttpClient) {}

  list(recherche = '') {
    const params = recherche.trim()
      ? new HttpParams().set('recherche', recherche.trim())
      : undefined;

    return this.http.get<Client[]>(this.apiUrl, { params });
  }

  create(request: ClientRequest) {
    return this.http.post<Client>(this.apiUrl, request);
  }

  update(id: number, request: ClientRequest) {
    return this.http.put<Client>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
