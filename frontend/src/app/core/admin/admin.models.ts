import { SubscriptionStatus } from '../auth/auth.models';

export interface AdminCoiffeuse {
  id: number;
  nom: string;
  nomSalon: string | null;
  email: string;
  subscriptionStatus: SubscriptionStatus;
  abonnementActifJusquAu: string | null;
  abonnementActif: boolean;
  creeLe: string;
}

export interface AdminUpdateSubscriptionRequest {
  subscriptionStatus: SubscriptionStatus;
  abonnementActifJusquAu: string | null;
}
