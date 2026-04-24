import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { environment } from '../../../environments/environment';
import { Depense, DepenseRequest, FinanceSummary } from './finance.models';

@Injectable({ providedIn: 'root' })
export class FinanceService {
  private readonly apiUrl = `${environment.apiUrl}/finance`;

  constructor(private readonly http: HttpClient) {}

  getSummary(debut: string, fin: string) {
    let params = new HttpParams();

    if (debut) {
      params = params.set('debut', debut);
    }

    if (fin) {
      params = params.set('fin', fin);
    }

    return this.http.get<FinanceSummary>(this.apiUrl, { params });
  }

  createExpense(request: DepenseRequest) {
    return this.http.post<Depense>(`${this.apiUrl}/depenses`, request);
  }

  updateExpense(id: number, request: DepenseRequest) {
    return this.http.put<Depense>(`${this.apiUrl}/depenses/${id}`, request);
  }

  deleteExpense(id: number) {
    return this.http.delete<void>(`${this.apiUrl}/depenses/${id}`);
  }
}
