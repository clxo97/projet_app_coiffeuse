export type SubscriptionStatus = 'TRIAL' | 'ACTIVE' | 'PAST_DUE' | 'CANCELED';

export interface LoginRequest {
  email: string;
  motDePasse: string;
}

export interface LoginResponse {
  token: string;
  tokenType: 'Bearer';
  coiffeuseId: number;
  nom: string;
  email: string;
  admin: boolean;
  subscriptionStatus: SubscriptionStatus;
  abonnementActifJusquAu: string | null;
  abonnementActif: boolean;
}
