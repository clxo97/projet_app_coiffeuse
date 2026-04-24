import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';
import { DashboardSummary } from '../../core/dashboard/dashboard.models';
import { DashboardService } from '../../core/dashboard/dashboard.service';
import { RendezVous } from '../../core/rendezvous/rendezvous.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="dashboard">
      <header>
        <div class="welcome-msg">
          <p>Heureux de vous revoir,</p>
          <h1>Bonjour {{ authService.currentUser()?.nom }}</h1>
        </div>
        <nav>
          <a routerLink="/rendez-vous" class="nav-link">
            <i class="ph ph-calendar"></i>
            <span>Agenda</span>
          </a>
          <a routerLink="/clients" class="nav-link">
            <i class="ph ph-users"></i>
            <span>Clients</span>
          </a>
          <a routerLink="/finance" class="nav-link">
            <i class="ph ph-coins"></i>
            <span>Finance</span>
          </a>
          <a routerLink="/notifications-sms" class="nav-link">
            <i class="ph ph-chat-circle-dots"></i>
            <span>Messages</span>
          </a>
          <a routerLink="/mon-compte" class="nav-link">
            <i class="ph ph-user-circle"></i>
            <span>Compte</span>
          </a>
          <button type="button" (click)="logout()" class="btn-logout">
            <i class="ph ph-sign-out"></i>
            <span>Quitter</span>
          </button>
        </nav>
      </header>

      <section class="grid-stats">
        <article class="stat-card">
          <div class="stat-icon blue">
            <i class="ph ph-calendar-check"></i>
          </div>
          <div class="stat-info">
            <strong>Rendez-vous</strong>
            <span>{{ summary()?.rendezVousDuJour || 0 }} aujourd'hui</span>
          </div>
        </article>

        <article class="stat-card">
          <div class="stat-icon green">
            <i class="ph ph-check-circle"></i>
          </div>
          <div class="stat-info">
            <strong>Terminés</strong>
            <span>{{ summary()?.rendezVousTerminesDuJour || 0 }}</span>
          </div>
        </article>

        <article class="stat-card">
          <div class="stat-icon gold">
            <i class="ph ph-currency-eur"></i>
          </div>
          <div class="stat-info">
            <strong>Chiffre d'affaires</strong>
            <span>{{ summary()?.chiffreAffairesDuJour || 0 | number:'1.2-2' }} €</span>
          </div>
        </article>

        <article class="stat-card">
          <div class="stat-icon pink">
            <i class="ph ph-bell-ringing"></i>
          </div>
          <div class="stat-info">
            <strong>Rappels</strong>
            <span>{{ summary()?.rappelsAEnvoyer?.length || 0 }} à envoyer</span>
          </div>
        </article>
      </section>

      <div class="panels-container">
        <section class="panel">
          <div class="panel-header">
            <h2><i class="ph ph-megaphone"></i> Rappels à envoyer</h2>
            <a routerLink="/rendez-vous" class="btn-view-all">Tout voir <i class="ph ph-arrow-right"></i></a>
          </div>

          <div class="panel-content">
            <div *ngIf="loading()" class="state-empty">Chargement...</div>
            <div *ngIf="!loading() && !summary()?.rappelsAEnvoyer?.length" class="state-empty">
              <i class="ph ph-chat-centered-slash"></i>
              <p>Aucun rappel en attente</p>
            </div>

            <div class="appointments-list" *ngIf="!loading() && summary()?.rappelsAEnvoyer?.length">
              <article *ngFor="let rv of summary()?.rappelsAEnvoyer; trackBy: trackByAppointmentId" class="appointment-item reminder-item">
                <div class="apt-main">
                  <strong>{{ rv.clientNom }}</strong>
                  <span><i class="ph ph-clock"></i> {{ formatDateTime(rv.dateHeure) }}</span>
                </div>
                <div class="apt-meta">
                  <span class="prestation">{{ rv.prestation }}</span>
                  <span class="price">{{ rv.clientTelephone || 'Sans tél.' }}</span>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section class="panel">
          <div class="panel-header">
            <h2><i class="ph ph-user-list"></i> Prochains rendez-vous</h2>
            <a routerLink="/rendez-vous" class="btn-view-all">Agenda <i class="ph ph-arrow-right"></i></a>
          </div>

          <div class="panel-content">
            <div *ngIf="loading()" class="state-empty">Chargement...</div>
            <div *ngIf="!loading() && !summary()?.prochainsRendezVous?.length" class="state-empty">
              <i class="ph ph-calendar-blank"></i>
              <p>Aucun rendez-vous à venir</p>
            </div>

            <div class="appointments-list" *ngIf="!loading() && summary()?.prochainsRendezVous?.length">
              <article *ngFor="let rv of summary()?.prochainsRendezVous" class="appointment-item">
                <div class="apt-main">
                  <strong>{{ rv.clientNom }}</strong>
                  <span><i class="ph ph-clock"></i> {{ formatDateTime(rv.dateHeure) }}</span>
                </div>
                <div class="apt-meta">
                  <span class="prestation">{{ rv.prestation }}</span>
                  <span class="price">{{ rv.prix | number:'1.2-2' }} €</span>
                </div>
              </article>
            </div>
          </div>
        </section>
      </div>

      <p *ngIf="errorMessage()" class="error-msg">{{ errorMessage() }}</p>
    </main>
  `,
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit {
  readonly summary = signal<DashboardSummary | null>(null);
  readonly loading = signal(false);
  readonly errorMessage = signal('');

  constructor(
    readonly authService: AuthService,
    private readonly dashboardService: DashboardService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.dashboardService.getSummary().subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger le tableau de bord.');
        this.loading.set(false);
      },
    });
  }

  formatDateTime(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  trackByAppointmentId(_: number, rendezVous: RendezVous): number {
    return rendezVous.id;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
