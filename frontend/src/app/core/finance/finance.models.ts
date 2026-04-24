export interface FinanceLine {
  rendezVousId: number;
  clientId: number;
  clientNom: string;
  dateHeure: string;
  prestation: string;
  prix: number;
}

export interface FinanceSummary {
  debut: string;
  fin: string;
  chiffreAffaires: number;
  totalDepenses: number;
  resultatNet: number;
  prestationsTerminees: number;
  ticketMoyen: number;
  lignes: FinanceLine[];
  depenses: Depense[];
}

export interface Depense {
  id: number;
  dateDepense: string;
  libelle: string;
  categorie: string;
  montant: number;
  notes: string | null;
  creeLe: string;
  modifieLe: string;
}

export interface DepenseRequest {
  dateDepense: string;
  libelle: string;
  categorie: string;
  montant: number;
  notes: string;
}
