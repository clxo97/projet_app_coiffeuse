import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Client, ClientRequest } from '../../core/clients/client.models';
import { ClientService } from '../../core/clients/client.service';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './clients.component.html',
  styleUrl: './clients.component.css',
})
export class ClientsComponent implements OnInit {
  readonly clients = signal<Client[]>([]);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly editingClient = signal<Client | null>(null);

  readonly searchForm = this.formBuilder.nonNullable.group({
    recherche: [''],
  });

  readonly clientForm = this.formBuilder.nonNullable.group({
    nom: ['', [Validators.required]],
    telephone: [''],
    email: ['', [Validators.email]],
    instagram: [''],
    notes: [''],
    smsActif: [true],
  });

  constructor(
    private readonly clientService: ClientService,
    private readonly formBuilder: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.loadClients();
  }

  loadClients(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.clientService.list(this.searchForm.controls.recherche.value).subscribe({
      next: (clients) => {
        this.clients.set(clients);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les clients.');
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      return;
    }

    const request = this.toRequest();
    const currentClient = this.editingClient();
    const action = currentClient
      ? this.clientService.update(currentClient.id, request)
      : this.clientService.create(request);

    this.saving.set(true);

    action.subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set(currentClient ? 'Client modifie.' : 'Client ajoute.');
        this.cancelEdit();
        this.loadClients();
      },
      error: () => {
        this.saving.set(false);
        this.errorMessage.set('Impossible d enregistrer le client.');
      },
    });
  }

  edit(client: Client): void {
    this.editingClient.set(client);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.clientForm.setValue({
      nom: client.nom,
      telephone: client.telephone || '',
      email: client.email || '',
      instagram: client.instagram || '',
      notes: client.notes || '',
      smsActif: client.smsActif,
    });
  }

  cancelEdit(): void {
    this.editingClient.set(null);
    this.clientForm.reset();
  }

  delete(client: Client): void {
    const confirmed = window.confirm(`Supprimer ${client.nom} ?`);

    if (!confirmed) {
      return;
    }

    this.clientService.delete(client.id).subscribe({
      next: () => {
        this.successMessage.set('Client supprime.');
        this.loadClients();
      },
      error: () => this.errorMessage.set('Impossible de supprimer le client.'),
    });
  }

  hasError(controlName: 'nom' | 'email', errorName: string): boolean {
    const control = this.clientForm.controls[controlName];
    return control.hasError(errorName) && (control.dirty || control.touched);
  }

  private toRequest(): ClientRequest {
    const value = this.clientForm.getRawValue();
    return {
      nom: value.nom.trim(),
      telephone: value.telephone.trim(),
      email: value.email.trim(),
      instagram: value.instagram.trim(),
      notes: value.notes.trim(),
      smsActif: value.smsActif,
    };
  }
}
