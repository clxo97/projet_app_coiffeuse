import { Routes } from '@angular/router';

import { adminGuard, authGuard } from './core/auth/auth.guard';
import { AdminSubscriptionsComponent } from './features/admin/admin-subscriptions.component';
import { AccountComponent } from './features/account/account.component';
import { ClientsComponent } from './features/clients/clients.component';
import { FinanceComponent } from './features/finance/finance.component';
import { DashboardComponent } from './features/login/dashboard.component';
import { LoginComponent } from './features/login/login.component';
import { NotificationSmsHistoryComponent } from './features/notifications/notification-sms-history.component';
import { RendezVousComponent } from './features/rendezvous/rendezvous.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
  { path: 'mon-compte', component: AccountComponent, canActivate: [authGuard] },
  { path: 'admin/abonnements', component: AdminSubscriptionsComponent, canActivate: [adminGuard] },
  { path: 'clients', component: ClientsComponent, canActivate: [authGuard] },
  { path: 'rendez-vous', component: RendezVousComponent, canActivate: [authGuard] },
  { path: 'finance', component: FinanceComponent, canActivate: [authGuard] },
  { path: 'notifications-sms', component: NotificationSmsHistoryComponent, canActivate: [authGuard] },
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: '**', redirectTo: 'dashboard' },
];
