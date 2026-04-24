import { CommonModule } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Depense, DepenseRequest, FinanceLine, FinanceSummary } from '../../core/finance/finance.models';
import { FinanceService } from '../../core/finance/finance.service';

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './finance.component.html',
  styleUrl: './finance.component.css',
})
export class FinanceComponent implements OnInit {
  readonly summary = signal<FinanceSummary | null>(null);
  readonly loading = signal(false);
  readonly savingExpense = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly editingExpense = signal<Depense | null>(null);
  readonly expenseCategories = ['Materiel', 'Produits', 'Hygiene', 'Communication', 'Transport', 'Autre'];

  readonly filterForm = this.formBuilder.nonNullable.group({
    debut: [this.firstDayOfMonth()],
    fin: [this.today()],
  });

  readonly expenseForm = this.formBuilder.nonNullable.group({
    dateDepense: [this.today(), [Validators.required]],
    libelle: ['', [Validators.required]],
    categorie: ['Materiel', [Validators.required]],
    montant: [0, [Validators.required, Validators.min(0)]],
    notes: [''],
  });

  constructor(
    private readonly financeService: FinanceService,
    private readonly formBuilder: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    const filters = this.filterForm.getRawValue();

    this.financeService.getSummary(filters.debut, filters.fin).subscribe({
      next: (summary) => {
        this.summary.set(summary);
        this.loading.set(false);
      },
      error: () => {
        this.errorMessage.set('Impossible de charger les donnees financieres.');
        this.loading.set(false);
      },
    });
  }

  setCurrentMonth(): void {
    this.filterForm.setValue({
      debut: this.firstDayOfMonth(),
      fin: this.today(),
    });
    this.loadSummary();
  }

  setToday(): void {
    const today = this.today();
    this.filterForm.setValue({
      debut: today,
      fin: today,
    });
    this.loadSummary();
  }

  formatDateTime(value: string): string {
    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  exportCsv(): void {
    const summary = this.summary();

    if (!summary) {
      return;
    }

    const incomeHeader = ['Type', 'Date', 'Client', 'Prestation/Depense', 'Categorie', 'Montant'];
    const incomeRows = summary.lignes.map((line) => [
      'Recette',
      this.formatDateTime(line.dateHeure),
      line.clientNom,
      line.prestation,
      '',
      String(line.prix),
    ]);
    const expenseRows = summary.depenses.map((expense) => [
      'Depense',
      expense.dateDepense,
      '',
      expense.libelle,
      expense.categorie,
      String(-expense.montant),
    ]);
    const csv = [incomeHeader, ...incomeRows, ...expenseRows]
      .map((row) => row.map((cell) => `"${cell.replaceAll('"', '""')}"`).join(';'))
      .join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `finance-${summary.debut}-${summary.fin}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  trackByLineId(_: number, line: FinanceLine): number {
    return line.rendezVousId;
  }

  submitExpense(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      return;
    }

    const request = this.toExpenseRequest();
    const currentExpense = this.editingExpense();
    const action = currentExpense
      ? this.financeService.updateExpense(currentExpense.id, request)
      : this.financeService.createExpense(request);

    this.savingExpense.set(true);

    action.subscribe({
      next: () => {
        this.savingExpense.set(false);
        this.successMessage.set(currentExpense ? 'Depense modifiee.' : 'Depense ajoutee.');
        this.cancelExpenseEdit();
        this.loadSummary();
      },
      error: () => {
        this.savingExpense.set(false);
        this.errorMessage.set('Impossible d enregistrer la depense.');
      },
    });
  }

  editExpense(expense: Depense): void {
    this.editingExpense.set(expense);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.expenseForm.setValue({
      dateDepense: expense.dateDepense,
      libelle: expense.libelle,
      categorie: expense.categorie,
      montant: Number(expense.montant),
      notes: expense.notes || '',
    });
  }

  cancelExpenseEdit(): void {
    this.editingExpense.set(null);
    this.expenseForm.reset({
      dateDepense: this.today(),
      libelle: '',
      categorie: 'Materiel',
      montant: 0,
      notes: '',
    });
  }

  deleteExpense(expense: Depense): void {
    const confirmed = window.confirm(`Supprimer la depense "${expense.libelle}" ?`);

    if (!confirmed) {
      return;
    }

    this.financeService.deleteExpense(expense.id).subscribe({
      next: () => {
        this.successMessage.set('Depense supprimee.');
        this.loadSummary();
      },
      error: () => this.errorMessage.set('Impossible de supprimer la depense.'),
    });
  }

  trackByExpenseId(_: number, expense: Depense): number {
    return expense.id;
  }

  hasExpenseError(controlName: 'dateDepense' | 'libelle' | 'categorie' | 'montant', errorName: string): boolean {
    const control = this.expenseForm.controls[controlName];
    return control.hasError(errorName) && (control.dirty || control.touched);
  }

  private toExpenseRequest(): DepenseRequest {
    const value = this.expenseForm.getRawValue();
    return {
      dateDepense: value.dateDepense,
      libelle: value.libelle.trim(),
      categorie: value.categorie.trim(),
      montant: Number(value.montant),
      notes: value.notes.trim(),
    };
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private firstDayOfMonth(): string {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10);
  }
}
