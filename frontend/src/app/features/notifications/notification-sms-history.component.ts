import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { NotificationSms, NotificationSmsStatus, NotificationSmsType } from '../../core/notifications/notification-sms.models';
import { NotificationSmsService } from '../../core/notifications/notification-sms.service';

@Component({
  selector: 'app-notification-sms-history',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './notification-sms-history.component.html',
  styleUrl: './notification-sms-history.component.css',
})
export class NotificationSmsHistoryComponent implements OnInit {
  readonly notifications = signal<NotificationSms[]>([]);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly statusFilter = signal<NotificationSmsStatus | 'TOUS'>('TOUS');

  readonly filteredNotifications = computed(() => {
    const status = this.statusFilter();
    const filtered = status === 'TOUS'
      ? this.notifications()
      : this.notifications().filter((notification) => notification.statut === status);

    return [...filtered].sort((first, second) => {
      if (first.statut === 'A_ENVOYER' && second.statut === 'A_ENVOYER') {
        return new Date(first.dateHeure).getTime() - new Date(second.dateHeure).getTime();
      }

      if (first.statut === 'A_ENVOYER') {
        return -1;
      }

      if (second.statut === 'A_ENVOYER') {
        return 1;
      }

      return new Date(second.creeLe).getTime() - new Date(first.creeLe).getTime();
    });
  });

  constructor(private readonly notificationSmsService: NotificationSmsService) {}

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.notificationSmsService.list().subscribe({
      next: (notifications) => {
        this.notifications.set(notifications);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger l historique des messages.');
        this.loading.set(false);
      },
    });
  }

  setStatusFilter(status: NotificationSmsStatus | 'TOUS'): void {
    this.statusFilter.set(status);
  }

  formatDateTime(value: string | null): string {
    if (!value) {
      return '-';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  typeLabel(type: NotificationSmsType): string {
    return type === 'RAPPEL_24H' ? 'Rappel 24h' : 'Confirmation';
  }

  statusLabel(status: NotificationSmsStatus): string {
    if (status === 'A_ENVOYER') {
      return 'A envoyer';
    }

    return status === 'ENVOYEE' ? 'Traite' : 'Erreur';
  }

  statusHint(notification: NotificationSms): string {
    if (notification.statut === 'A_ENVOYER') {
      return `A traiter avant le rendez-vous du ${this.formatDateTime(notification.dateHeure)}`;
    }

    if (notification.statut === 'ENVOYEE') {
      return notification.dateEnvoi
        ? `Traite le ${this.formatDateTime(notification.dateEnvoi)}`
        : 'Deja traite';
    }

    return 'Une erreur est a verifier';
  }

  openWhatsApp(notification: NotificationSms): void {
    if (!notification.telephone) {
      this.errorMessage.set('Telephone client non renseigne.');
      return;
    }

    const phone = this.toWhatsAppPhone(notification.telephone);
    const url = `https://wa.me/${phone}?text=${encodeURIComponent(notification.message)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  }

  openInstagram(notification: NotificationSms): void {
    if (!notification.clientInstagram) {
      this.errorMessage.set('Instagram client non renseigne.');
      return;
    }

    window.open(`https://instagram.com/${notification.clientInstagram}`, '_blank', 'noopener,noreferrer');
  }

  copyMessage(notification: NotificationSms): void {
    navigator.clipboard.writeText(notification.message).then(() => {
      this.errorMessage.set('');
    }).catch(() => {
      this.errorMessage.set('Impossible de copier le message.');
    });
  }

  trackByNotificationId(_: number, notification: NotificationSms): number {
    return notification.id;
  }

  private toWhatsAppPhone(phone: string): string {
    const cleaned = phone.replace(/[^\d+]/g, '');

    if (cleaned.startsWith('+')) {
      return cleaned.substring(1);
    }

    if (cleaned.startsWith('0')) {
      return `33${cleaned.substring(1)}`;
    }

    return cleaned;
  }
}
