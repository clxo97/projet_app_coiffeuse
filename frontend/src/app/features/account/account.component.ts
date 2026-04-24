import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AccountService } from '../../core/account/account.service';
import { CoiffeuseAccount, SmsConfig } from '../../core/account/account.models';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './account.component.html',
  styleUrl: './account.component.css',
})
export class AccountComponent implements OnInit {
  readonly account = signal<CoiffeuseAccount | null>(null);
  readonly loadingAccount = signal(true);
  readonly smsConfig = signal<SmsConfig | null>(null);
  readonly saving = signal(false);
  readonly savingSmsTemplates = signal(false);
  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly smsSuccessMessage = signal('');
  readonly smsErrorMessage = signal('');
  readonly showCurrentPassword = signal(false);
  readonly showNewPassword = signal(false);

  readonly form = this.formBuilder.nonNullable.group({
    motDePasseActuel: ['', [Validators.required]],
    nouveauMotDePasse: ['', [Validators.required, Validators.minLength(8)]],
    confirmationMotDePasse: ['', [Validators.required]],
  });

  readonly smsForm = this.formBuilder.nonNullable.group({
    modeleSmsConfirmation: [''],
    modeleSmsModification: [''],
    modeleSmsRappel: [''],
  });

  constructor(
    private readonly accountService: AccountService,
    private readonly authService: AuthService,
    private readonly formBuilder: FormBuilder,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.accountService.getAccount().subscribe({
      next: (account) => {
        this.account.set(account);
        this.smsForm.setValue({
          modeleSmsConfirmation: account.modeleSmsConfirmation || this.defaultConfirmationTemplate,
          modeleSmsModification: account.modeleSmsModification || this.defaultModificationTemplate,
          modeleSmsRappel: account.modeleSmsRappel || this.defaultReminderTemplate,
        });
        this.loadingAccount.set(false);
      },
      error: () => {
        this.authService.logout();
        this.router.navigateByUrl('/login');
      },
    });

    this.accountService.getSmsConfig().subscribe({
      next: (config) => this.smsConfig.set(config),
      error: () => this.smsConfig.set(null),
    });
  }

  submit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.form.invalid || !this.passwordsMatch()) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving.set(true);

    this.accountService.changePassword({
      motDePasseActuel: value.motDePasseActuel,
      nouveauMotDePasse: value.nouveauMotDePasse,
    }).subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set('Mot de passe modifie.');
        this.form.reset();
      },
      error: (error) => {
        this.saving.set(false);

        if (error.status === 403) {
          this.errorMessage.set('Le mot de passe actuel est incorrect.');
          return;
        }

        this.errorMessage.set('Impossible de modifier le mot de passe.');
      },
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }

  submitSmsTemplates(): void {
    this.smsSuccessMessage.set('');
    this.smsErrorMessage.set('');

    const value = this.smsForm.getRawValue();
    this.savingSmsTemplates.set(true);

    this.accountService.updateSmsTemplates({
      modeleSmsConfirmation: value.modeleSmsConfirmation.trim(),
      modeleSmsModification: value.modeleSmsModification.trim(),
      modeleSmsRappel: value.modeleSmsRappel.trim(),
    }).subscribe({
      next: (account) => {
        this.account.set(account);
        this.savingSmsTemplates.set(false);
        this.smsSuccessMessage.set('Modeles de messages enregistres.');
      },
      error: () => {
        this.savingSmsTemplates.set(false);
        this.smsErrorMessage.set('Impossible d enregistrer les modeles de messages.');
      },
    });
  }

  resetSmsTemplates(): void {
    this.smsForm.setValue({
      modeleSmsConfirmation: this.defaultConfirmationTemplate,
      modeleSmsModification: this.defaultModificationTemplate,
      modeleSmsRappel: this.defaultReminderTemplate,
    });
  }

  readonly defaultConfirmationTemplate = 'Votre rendez-vous coiffure est confirme le {date}. Prestation : {prestation}. Duree : {duree} min. A bientot.';
  readonly defaultModificationTemplate = 'Votre rendez-vous coiffure a ete modifie le {date}. Prestation : {prestation}. Duree : {duree} min. A bientot.';
  readonly defaultReminderTemplate = 'Rappel : votre rendez-vous coiffure est prevu le {date}. Prestation : {prestation}. Duree : {duree} min. A bientot.';

  passwordsMatch(): boolean {
    const value = this.form.getRawValue();
    return value.nouveauMotDePasse === value.confirmationMotDePasse;
  }

  hasError(controlName: 'motDePasseActuel' | 'nouveauMotDePasse' | 'confirmationMotDePasse', errorName: string): boolean {
    const control = this.form.controls[controlName];
    return control.hasError(errorName) && (control.dirty || control.touched);
  }

  showConfirmationError(): boolean {
    const control = this.form.controls.confirmationMotDePasse;
    return !this.passwordsMatch() && (control.dirty || control.touched);
  }

  toggleCurrentPassword(): void {
    this.showCurrentPassword.update((visible) => !visible);
  }

  toggleNewPassword(): void {
    this.showNewPassword.update((visible) => !visible);
  }
}
