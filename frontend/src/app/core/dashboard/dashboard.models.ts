import { RendezVous } from '../rendezvous/rendezvous.models';

export interface DashboardSummary {
  rendezVousDuJour: number;
  rendezVousTerminesDuJour: number;
  chiffreAffairesDuJour: number;
  prochainsRendezVous: RendezVous[];
  rappelsAEnvoyer: RendezVous[];
}
