import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { RendezVous, RendezVousRequest } from './rendezvous.models';

@Injectable({ providedIn: 'root' })
export class RendezVousService {
  private readonly apiUrl = `${environment.apiUrl}/rendez-vous`;

  constructor(private readonly http: HttpClient) {}

  list(date = '') {
    const params = date ? new HttpParams().set('date', date) : undefined;
    return this.http.get<RendezVous[]>(this.apiUrl, { params });
  }

  create(request: RendezVousRequest) {
    return this.http.post<RendezVous>(this.apiUrl, request);
  }

  update(id: number, request: RendezVousRequest) {
    return this.http.put<RendezVous>(`${this.apiUrl}/${id}`, request);
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  markReminderSent(id: number) {
    return this.http.put<RendezVous>(`${this.apiUrl}/${id}/rappel-envoye`, {});
  }
}
