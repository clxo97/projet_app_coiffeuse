export type StatutRendezVous = 'PLANIFIE' | 'TERMINE' | 'ANNULE';

export interface RendezVous {
  id: number;
  clientId: number;
  clientNom: string;
  clientTelephone: string | null;
  clientInstagram: string | null;
  dateHeure: string;
  dureeMinutes: number;
  prestation: string;
  prix: number;
  statut: StatutRendezVous;
  notes: string | null;
  messageRappel: string | null;
  rappelEnvoye: boolean;
  creeLe: string;
  modifieLe: string;
}

export interface RendezVousRequest {
  clientId: number;
  dateHeure: string;
  dureeMinutes: number;
  prestation: string;
  prix: number;
  statut: StatutRendezVous;
  notes: string;
}
