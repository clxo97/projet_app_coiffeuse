import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AdminService } from '../../core/admin/admin.service';
import { AdminCoiffeuse } from '../../core/admin/admin.models';
import { SubscriptionStatus } from '../../core/auth/auth.models';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-admin-subscriptions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-subscriptions.component.html',
  styleUrl: './admin-subscriptions.component.css',
})
export class AdminSubscriptionsComponent implements OnInit {
  readonly coiffeuses = signal<AdminCoiffeuse[]>([]);
  readonly loading = signal(true);
  readonly savingId = signal<number | null>(null);
  readonly editingId = signal<number | null>(null);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly form = this.formBuilder.nonNullable.group({
    subscriptionStatus: ['TRIAL' as SubscriptionStatus, [Validators.required]],
    abonnementActifJusquAu: [''],
  });

  readonly statuses: { value: SubscriptionStatus; label: string }[] = [
    { value: 'TRIAL', label: 'Essai' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'PAST_DUE', label: 'Paiement en attente' },
    { value: 'CANCELED', label: 'Inactive' },
  ];

  constructor(
    private readonly adminService: AdminService,
    private readonly authService: AuthService,
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isAdmin()) {
      this.router.navigateByUrl('/dashboard');
      return;
    }

    this.loadCoiffeuses();
  }

  loadCoiffeuses(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.adminService.getCoiffeuses().subscribe({
      next: (coiffeuses) => {
        this.coiffeuses.set(coiffeuses);
        this.loading.set(false);
      },
      error: (error) => {
        this.loading.set(false);

        if (error.status === 403) {
          this.router.navigateByUrl('/dashboard');
          return;
        }

        this.errorMessage.set('Impossible de charger la liste des abonnements.');
      },
    });
  }

  startEdit(coiffeuse: AdminCoiffeuse): void {
    this.editingId.set(coiffeuse.id);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.form.setValue({
      subscriptionStatus: coiffeuse.subscriptionStatus,
      abonnementActifJusquAu: this.toDateTimeLocal(coiffeuse.abonnementActifJusquAu),
    });
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.form.reset({
      subscriptionStatus: 'TRIAL',
      abonnementActifJusquAu: '',
    });
  }

  save(coiffeuse: AdminCoiffeuse): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    const requiresDate = value.subscriptionStatus === 'TRIAL' || value.subscriptionStatus === 'ACTIVE';

    if (requiresDate && !value.abonnementActifJusquAu) {
      this.errorMessage.set('Une date de validite est obligatoire pour un essai ou un abonnement actif.');
      return;
    }

    this.savingId.set(coiffeuse.id);

    this.adminService.updateSubscription(coiffeuse.id, {
      subscriptionStatus: value.subscriptionStatus,
      abonnementActifJusquAu: value.abonnementActifJusquAu ? new Date(value.abonnementActifJusquAu).toISOString() : null,
    }).subscribe({
      next: (updated) => {
        this.coiffeuses.update((items) => items.map((item) => item.id === updated.id ? updated : item));
        this.savingId.set(null);
        this.editingId.set(null);
        this.successMessage.set(`Abonnement mis a jour pour ${updated.nom}.`);
      },
      error: (error) => {
        this.savingId.set(null);

        if (error.status === 400) {
          this.errorMessage.set('Parametres d abonnement invalides.');
          return;
        }

        if (error.status === 403) {
          this.router.navigateByUrl('/dashboard');
          return;
        }

        this.errorMessage.set('Impossible de mettre a jour cet abonnement.');
      },
    });
  }

  subscriptionLabel(status: SubscriptionStatus): string {
    return this.statuses.find((item) => item.value === status)?.label ?? status;
  }

  formatDate(value: string | null): string {
    if (!value) {
      return 'Non definie';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'long',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  private toDateTimeLocal(value: string | null): string {
    if (!value) {
      return '';
    }

    const date = new Date(value);
    const timezoneOffset = date.getTimezoneOffset() * 60000;
    return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 16);
  }
}
