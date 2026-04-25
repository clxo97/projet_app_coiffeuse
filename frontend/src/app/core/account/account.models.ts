import { SubscriptionStatus } from '../auth/auth.models';

export interface CoiffeuseAccount {
  id: number;
  nom: string;
  nomSalon: string | null;
  email: string;
  admin: boolean;
  subscriptionStatus: SubscriptionStatus;
  abonnementActifJusquAu: string | null;
  abonnementActif: boolean;
  modeleSmsConfirmation: string | null;
  modeleSmsModification: string | null;
  modeleSmsRappel: string | null;
}

export interface ChangePasswordRequest {
  motDePasseActuel: string;
  nouveauMotDePasse: string;
}

export interface SmsTemplatesRequest {
  modeleSmsConfirmation: string;
  modeleSmsModification: string;
  modeleSmsRappel: string;
}

export interface SmsConfig {
  provider: 'console' | 'twilio' | string;
  twilioConfigured: boolean;
  from: string | null;
  modeLabel: string;
  description: string;
}
