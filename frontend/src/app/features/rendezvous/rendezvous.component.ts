import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Client } from '../../core/clients/client.models';
import { ClientService } from '../../core/clients/client.service';
import { RendezVous, RendezVousRequest, StatutRendezVous } from '../../core/rendezvous/rendezvous.models';
import { RendezVousService } from '../../core/rendezvous/rendezvous.service';

@Component({
  selector: 'app-rendezvous',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './rendezvous.component.html',
  styleUrl: './rendezvous.component.css',
})
export class RendezVousComponent implements OnInit {
  readonly rendezVous = signal<RendezVous[]>([]);
  readonly clients = signal<Client[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly editingRendezVous = signal<RendezVous | null>(null);
  readonly clientSearch = signal('');
  readonly showClientSuggestions = signal(false);
  readonly statuts: StatutRendezVous[] = ['PLANIFIE', 'TERMINE', 'ANNULE'];
  readonly durations = [30, 45, 60, 75, 90, 120, 150, 180, 240];

  readonly filterForm = this.formBuilder.nonNullable.group({
    date: [this.today()],
  });

  readonly form = this.formBuilder.nonNullable.group({
    clientId: [0, [Validators.required, Validators.min(1)]],
    dateHeure: ['', [Validators.required]],
    dureeMinutes: [60, [Validators.required, Validators.min(15), Validators.max(480)]],
    prestation: ['', [Validators.required]],
    prix: [0, [Validators.required, Validators.min(0)]],
    statut: ['PLANIFIE' as StatutRendezVous, [Validators.required]],
    notes: [''],
  });

  constructor(
    private readonly rendezVousService: RendezVousService,
    private readonly clientService: ClientService,
    private readonly formBuilder: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.loadClients();
    this.loadRendezVous();
  }

  loadClients(): void {
    this.clientService.list().subscribe({
      next: (clients) => this.clients.set(clients),
      error: () => this.errorMessage.set('Impossible de charger les clients.'),
    });
  }

  filteredClients(): Client[] {
    const query = this.clientSearch().trim().toLowerCase();
    const clients = this.clients();

    if (!query) {
      return clients.slice(0, 8);
    }

    return clients
      .filter((client) => [
        client.nom,
        client.telephone || '',
        client.email || '',
        client.instagram || '',
      ].some((value) => value.toLowerCase().includes(query)))
      .slice(0, 8);
  }

  onClientSearch(value: string): void {
    this.clientSearch.set(value);
    this.showClientSuggestions.set(true);

    const selectedClient = this.selectedClient();
    if (selectedClient && selectedClient.nom !== value) {
      this.form.controls.clientId.setValue(0);
    }
  }

  selectClient(client: Client): void {
    this.form.controls.clientId.setValue(client.id);
    this.clientSearch.set(client.nom);
    this.showClientSuggestions.set(false);
  }

  selectedClient(): Client | null {
    const clientId = this.form.controls.clientId.value;
    return this.clients().find((client) => client.id === clientId) || null;
  }

  loadRendezVous(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.rendezVousService.list(this.filterForm.controls.date.value).subscribe({
      next: (rendezVous) => {
        this.rendezVous.set(rendezVous);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les rendez-vous.');
        this.loading.set(false);
      },
    });
  }

  clearDateFilter(): void {
    this.filterForm.controls.date.setValue('');
    this.loadRendezVous();
  }

  previousDay(): void {
    this.moveSelectedDate(-1);
  }

  nextDay(): void {
    this.moveSelectedDate(1);
  }

  setToday(): void {
    this.filterForm.controls.date.setValue(this.today());
    this.loadRendezVous();
  }

  submit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const request = this.toRequest();
    const current = this.editingRendezVous();
    const action = current
      ? this.rendezVousService.update(current.id, request)
      : this.rendezVousService.create(request);

    this.saving.set(true);

    action.subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set(current ? 'Rendez-vous modifie.' : 'Rendez-vous ajoute.');
        this.cancelEdit();
        this.loadRendezVous();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Impossible d enregistrer le rendez-vous.');
      },
    });
  }

  edit(rendezVous: RendezVous): void {
    this.editingRendezVous.set(rendezVous);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.form.setValue({
      clientId: rendezVous.clientId,
      dateHeure: rendezVous.dateHeure.slice(0, 16),
      dureeMinutes: rendezVous.dureeMinutes || 60,
      prestation: rendezVous.prestation,
      prix: Number(rendezVous.prix),
      statut: rendezVous.statut,
      notes: rendezVous.notes || '',
    });
    this.clientSearch.set(rendezVous.clientNom);
    this.showClientSuggestions.set(false);
  }

  cancelEdit(): void {
    this.editingRendezVous.set(null);
    this.form.reset({
      clientId: 0,
      dateHeure: '',
      dureeMinutes: 60,
      prestation: '',
      prix: 0,
      statut: 'PLANIFIE',
      notes: '',
    });
    this.clientSearch.set('');
    this.showClientSuggestions.set(false);
  }

  delete(rendezVous: RendezVous): void {
    const confirmed = window.confirm(`Supprimer le rendez-vous de ${rendezVous.clientNom} ?`);

    if (!confirmed) {
      return;
    }

    this.rendezVousService.delete(rendezVous.id).subscribe({
      next: () => {
        this.successMessage.set('Rendez-vous supprime.');
        this.loadRendezVous();
      },
      error: () => this.errorMessage.set('Impossible de supprimer le rendez-vous.'),
    });
  }

  statusLabel(statut: StatutRendezVous): string {
    const labels: Record<StatutRendezVous, string> = {
      PLANIFIE: 'Planifie',
      TERMINE: 'Termine',
      ANNULE: 'Annule',
    };

    return labels[statut];
  }

  formatDateTime(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  formatTime(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(value));
  }

  formatTimeRange(rendezVous: RendezVous): string {
    const start = new Date(rendezVous.dateHeure);
    const end = new Date(start.getTime() + (rendezVous.dureeMinutes || 60) * 60 * 1000);
    return `${this.formatTime(rendezVous.dateHeure)} - ${new Intl.DateTimeFormat('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(end)}`;
  }

  selectedDateLabel(): string {
    const date = this.filterForm.controls.date.value;

    if (!date) {
      return 'Tous les rendez-vous';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date(`${date}T12:00:00`));
  }

  totalTermine(): number {
    return this.rendezVous()
      .filter((rendezVous) => rendezVous.statut === 'TERMINE')
      .reduce((total, rendezVous) => total + Number(rendezVous.prix), 0);
  }

  hasError(controlName: 'clientId' | 'dateHeure' | 'dureeMinutes' | 'prestation' | 'prix', errorName: string): boolean {
    const control = this.form.controls[controlName];
    return control.hasError(errorName) && (control.dirty || control.touched);
  }

  async addToCalendar(rendezVous: RendezVous): Promise<void> {
    const icsContent = this.buildIcs(rendezVous);
    const fileName = `rendez-vous-${rendezVous.id}.ics`;
    const file = new File([icsContent], fileName, { type: 'text/calendar;charset=utf-8' });

    if ('canShare' in navigator && navigator.canShare?.({ files: [file] }) && 'share' in navigator) {
      await navigator.share({
        title: `Rendez-vous ${rendezVous.clientNom}`,
        files: [file],
      });
      return;
    }

    const url = URL.createObjectURL(file);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName;
    link.click();
    URL.revokeObjectURL(url);
  }

  openWhatsAppReminder(rendezVous: RendezVous): void {
    if (!rendezVous.clientTelephone) {
      this.errorMessage.set('Telephone client non renseigne.');
      return;
    }

    const phone = this.toWhatsAppPhone(rendezVous.clientTelephone);
    const message = this.buildReminderMessage(rendezVous);
    const url = `https://wa.me/${phone}?text=${encodeURIComponent(message)}`;
    window.open(url, '_blank', 'noopener,noreferrer');
  }

  openInstagram(rendezVous: RendezVous): void {
    if (!rendezVous.clientInstagram) {
      this.errorMessage.set('Instagram client non renseigne.');
      return;
    }

    window.open(`https://instagram.com/${rendezVous.clientInstagram}`, '_blank', 'noopener,noreferrer');
  }

  markReminderSent(rendezVous: RendezVous): void {
    this.rendezVousService.markReminderSent(rendezVous.id).subscribe({
      next: (updated) => {
        this.rendezVous.update((items) => items.map((item) => item.id === updated.id ? updated : item));
        this.successMessage.set('Rappel marque comme envoye.');
      },
      error: () => this.errorMessage.set('Impossible de marquer le rappel.'),
    });
  }

  copyReminder(rendezVous: RendezVous): void {
    const message = this.buildReminderMessage(rendezVous);

    navigator.clipboard.writeText(message).then(() => {
      this.successMessage.set('Message copie. Tu peux le coller dans WhatsApp ou Instagram.');
    }).catch(() => {
      this.errorMessage.set('Impossible de copier le rappel.');
    });
  }

  trackByAppointmentId(_: number, rendezVous: RendezVous): number {
    return rendezVous.id;
  }

  private toRequest(): RendezVousRequest {
    const value = this.form.getRawValue();
    return {
      clientId: Number(value.clientId),
      dateHeure: value.dateHeure,
      dureeMinutes: Number(value.dureeMinutes),
      prestation: value.prestation.trim(),
      prix: Number(value.prix),
      statut: value.statut,
      notes: value.notes.trim(),
    };
  }

  private today(): string {
    const now = new Date();
    return now.toISOString().slice(0, 10);
  }

  private moveSelectedDate(days: number): void {
    const current = this.filterForm.controls.date.value || this.today();
    const date = new Date(`${current}T12:00:00`);
    date.setDate(date.getDate() + days);
    this.filterForm.controls.date.setValue(date.toISOString().slice(0, 10));
    this.loadRendezVous();
  }

  private buildIcs(rendezVous: RendezVous): string {
    const start = new Date(rendezVous.dateHeure);
    const end = new Date(start.getTime() + (rendezVous.dureeMinutes || 60) * 60 * 1000);
    const description = [
      `Client: ${rendezVous.clientNom}`,
      rendezVous.clientTelephone ? `Telephone: ${rendezVous.clientTelephone}` : '',
      `Prestation: ${rendezVous.prestation}`,
      `Prix: ${Number(rendezVous.prix).toFixed(2)} EUR`,
      rendezVous.notes ? `Notes: ${rendezVous.notes}` : '',
    ].filter(Boolean).join('\\n');

    return [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'PRODID:-//App Coiffure//Planning//FR',
      'CALSCALE:GREGORIAN',
      'METHOD:PUBLISH',
      'BEGIN:VEVENT',
      `UID:rendez-vous-${rendezVous.id}@app-coiffure.local`,
      `DTSTAMP:${this.toIcsDate(new Date())}`,
      `DTSTART:${this.toIcsDate(start)}`,
      `DTEND:${this.toIcsDate(end)}`,
      `SUMMARY:${this.escapeIcs(`Coiffure - ${rendezVous.clientNom}`)}`,
      `DESCRIPTION:${this.escapeIcs(description)}`,
      `STATUS:${rendezVous.statut === 'ANNULE' ? 'CANCELLED' : 'CONFIRMED'}`,
      'END:VEVENT',
      'END:VCALENDAR',
    ].join('\r\n');
  }

  private buildReminderMessage(rendezVous: RendezVous): string {
    if (rendezVous.messageRappel?.trim()) {
      return rendezVous.messageRappel;
    }

    return [
      `Bonjour ${rendezVous.clientNom},`,
      `petit rappel pour votre rendez-vous coiffure le ${this.formatDateTime(rendezVous.dateHeure)}.`,
      `Prestation : ${rendezVous.prestation}.`,
      `Duree : ${rendezVous.dureeMinutes || 60} min.`,
      'A bientot.',
    ].join('\n');
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

  private toIcsDate(date: Date): string {
    const value = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return value.toISOString().replace(/[-:]/g, '').split('.')[0];
  }

  private escapeIcs(value: string): string {
    return value
      .replaceAll('\\', '\\\\')
      .replaceAll(';', '\\;')
      .replaceAll(',', '\\,')
      .replaceAll('\n', '\\n');
  }
}
