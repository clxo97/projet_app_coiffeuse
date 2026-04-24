export type NotificationSmsType = 'CONFIRMATION_RDV' | 'RAPPEL_24H';
export type NotificationSmsStatus = 'A_ENVOYER' | 'ENVOYEE' | 'ECHEC';

export interface NotificationSms {
  id: number;
  rendezVousId: number;
  clientNom: string;
  prestation: string;
  dateHeure: string;
  clientInstagram: string | null;
  type: NotificationSmsType;
  statut: NotificationSmsStatus;
  dateEnvoiPrevue: string;
  dateEnvoi: string | null;
  telephone: string;
  message: string;
  erreur: string | null;
  creeLe: string;
}
