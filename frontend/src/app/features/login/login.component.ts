import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements OnInit {
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly showPassword = signal(false);

  readonly form = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8)]],
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.router.navigateByUrl('/dashboard');
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    const credentials = this.form.getRawValue();

    this.authService.login({
      email: credentials.email.trim(),
      motDePasse: credentials.motDePasse,
    }).subscribe({
      next: (response) => {
        this.router.navigateByUrl(response.abonnementActif ? '/dashboard' : '/mon-compte');
      },
      error: (error) => {
        if (error.status === 401) {
          this.errorMessage.set('Email ou mot de passe incorrect.');
        } else if (error.status === 402) {
          this.errorMessage.set('Abonnement inactif. Ouvre Mon compte pour regulariser la situation.');
        } else {
          this.errorMessage.set('Connexion impossible pour le moment.');
        }

        this.loading.set(false);
      },
    });
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((visible) => !visible);
  }

  hasError(controlName: 'email' | 'motDePasse', errorName: string): boolean {
    const control = this.form.controls[controlName];
    return control.hasError(errorName) && (control.dirty || control.touched);
  }
}
