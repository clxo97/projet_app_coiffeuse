export interface Client {
  id: number;
  nom: string;
  telephone: string | null;
  email: string | null;
  instagram: string | null;
  notes: string | null;
  smsActif: boolean;
  creeLe: string;
  modifieLe: string;
}

export interface ClientRequest {
  nom: string;
  telephone: string;
  email: string;
  instagram: string;
  notes: string;
  smsActif: boolean;
}
