# App Coiffure

Application fullstack pour une coiffeuse independante.

## Structure

- `backend/` : API Spring Boot, securite, connexion PostgreSQL.
- `frontend/` : application Angular avec page de connexion et espace prive.

## Configuration PostgreSQL

Par defaut, le backend cherche une base PostgreSQL :

- base : `salon_coiffure`
- hote : `172.28.240.1`
- utilisateur : `app_coiffure`
- mot de passe : `app_coiffure`
- port : `5432`

Ces valeurs sont dans `backend/src/main/resources/application.properties` et peuvent etre surchargees :

```bash
export DB_NAME=nom_de_ta_base
export DB_USERNAME=app_coiffure
export DB_PASSWORD=app_coiffure
export DB_HOST=172.28.240.1
export DB_PORT=5432
```

Depuis WSL, l'adresse Windows/WampServer peut changer apres un redemarrage. Pour la retrouver :

```bash
ip route | awk '/default/ {print $3}'
```

Si tu lances le backend directement depuis Windows ou Render, utilise plutot l'hote PostgreSQL fourni par la plateforme.

Au premier demarrage, Spring cree la table `coiffeuses` si elle n'existe pas. Si aucune coiffeuse n'existe encore, l'application cree un compte initial configurable :

- nom : variable `APP_COIFFEUSE_NOM`, valeur par defaut `Coiffeuse`
- email : variable `APP_COIFFEUSE_EMAIL`, valeur par defaut `coiffeuse@salon.local`
- mot de passe : variable `APP_COIFFEUSE_PASSWORD`, valeur par defaut `ChangeMe123!`

Pour creer directement le vrai compte de la coiffeuse sur une base vide :

```bash
export APP_COIFFEUSE_NOM="Nom de la coiffeuse"
export APP_COIFFEUSE_EMAIL="email@exemple.fr"
export APP_COIFFEUSE_PASSWORD="mot-de-passe-solide"
```

## Demarrer le backend

```bash
cd backend
mvn spring-boot:run
```

Test rapide :

```bash
curl http://localhost:8080/api/health
```

Connexion :

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"coiffeuse@salon.local","motDePasse":"ChangeMe123!"}'
```

## SMS automatiques

Lorsqu'un rendez-vous est cree ou modifie, le backend programme automatiquement :

- un SMS de confirmation immediat ;
- un SMS de rappel 24h avant le rendez-vous.

Par defaut, `SMS_PROVIDER=console` : aucun vrai SMS n'est facture ni envoye, le message apparait dans les logs du backend. Le planificateur verifie les SMS a envoyer toutes les 60 secondes par defaut.

Pour tester plus vite en local :

```bash
SMS_SCHEDULER_DELAY_MS=1000 mvn spring-boot:run
```

Pour envoyer de vrais SMS avec Twilio :

```bash
export SMS_PROVIDER=twilio
export TWILIO_ACCOUNT_SID=ton_account_sid
export TWILIO_AUTH_TOKEN=ton_auth_token
export TWILIO_FROM=+1234567890
mvn spring-boot:run
```

Les numeros clients doivent etre renseignes au format international, par exemple `+33600000000`.

Tu peux aussi utiliser un fichier local non versionne :

```bash
cp backend/.env.example backend/.env
nano backend/.env
set -a
source backend/.env
set +a
cd backend
mvn spring-boot:run
```

Dans Twilio, recupere :

- `TWILIO_ACCOUNT_SID` : Account SID du projet Twilio.
- `TWILIO_AUTH_TOKEN` : Auth Token du meme projet.
- `TWILIO_FROM` : numero Twilio achete/active, au format international comme `+1234567890`.

Avec un compte Twilio d'essai, les destinataires doivent souvent etre verifies dans Twilio avant envoi. Si une variable manque en mode `SMS_PROVIDER=twilio`, le backend refuse de demarrer avec un message explicite. La page `Mon compte` affiche le mode actif : console local ou Twilio vrais SMS.

Notes pour un compte Twilio trial :

- le destinataire doit etre verifie dans Twilio, par exemple `+336...` dans la liste des numeros verifies ;
- `TWILIO_FROM` doit etre un numero Twilio actif, pas le numero de la cliente ;
- le `service_sid` commencant par `VA...` sert a Twilio Verify et ne remplace pas `TWILIO_FROM` pour les SMS de rendez-vous ;
- Twilio peut bloquer certains pays si les permissions geographiques SMS ne sont pas activees ;
- les SMS trial peuvent contenir une mention ajoutee par Twilio.

## WhatsApp et Instagram

Pour un usage quotidien sans frais SMS, l'application privilegie maintenant un envoi manuel :

- sur la fiche client, renseigner le telephone et l'identifiant Instagram ;
- dans l'agenda, le bouton WhatsApp ouvre une conversation avec le rappel pre-rempli ;
- le bouton Instagram ouvre le profil de la cliente si son identifiant est renseigne ;
- le bouton Copier permet de coller le message dans WhatsApp, Instagram ou un autre canal ;
- le bouton coche permet de marquer le rappel comme envoye manuellement.
- les textes envoyes sont personnalisables dans `Mon compte > Modeles de messages` avec les variables `{client}`, `{date}`, `{prestation}` et `{duree}`.

Pour eviter les erreurs Twilio en local, garder `SMS_PROVIDER=console` dans `backend/.env`. Les vrais SMS Twilio restent possibles plus tard en remettant `SMS_PROVIDER=twilio` et un vrai numero Twilio dans `TWILIO_FROM`.

## Demarrer le frontend

```bash
cd frontend
npm install
npm start
```

Puis ouvrir `http://localhost:4200`.

Le frontend appelle l'API sur `http://localhost:8080/api`, configurable dans `frontend/src/environments/environment.ts`.

## Pages disponibles

- `http://localhost:4200/login` : connexion.
- `http://localhost:4200/dashboard` : accueil prive avec indicateurs du jour et prochains rendez-vous.
- `http://localhost:4200/mon-compte` : informations du compte et changement de mot de passe.
- `http://localhost:4200/clients` : base clients, recherche, creation, modification, suppression.
- `http://localhost:4200/rendez-vous` : agenda, creation, modification, duree reelle des seances, rappels WhatsApp avec suivi d'envoi, suppression, export calendrier mobile et suivi financier simple.
- `http://localhost:4200/finance` : chiffre d'affaires par periode, depenses materielles, resultat net, detail et export CSV.

## SaaS multi-coiffeuses

La version actuelle isole maintenant les donnees par coiffeuse :

- chaque `client` appartient a une coiffeuse ;
- chaque `depense` appartient a une coiffeuse ;
- les `rendez-vous`, le dashboard, la finance et l'historique SMS sont filtres par le compte connecte ;
- un compte ne peut plus lire ni modifier les donnees d'une autre coiffeuse.

Inscription API :

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom":"Nadia",
    "nomSalon":"Studio Nadia",
    "email":"nadia@salon.fr",
    "motDePasse":"MotDePasse123!"
  }'
```

Chaque nouveau compte est cree en `TRIAL` avec 14 jours d'acces. Le domaine contient deja :

- `subscriptionStatus`
- `abonnementActifJusquAu`
- `stripeCustomerId`
- `stripeSubscriptionId`

Cela permet de brancher un abonnement mensuel plus tard sans refaire le modele de donnees.

## Deploiement en ligne

Architecture recommandee :

- frontend Angular statique sur Vercel / Netlify / Cloudflare Pages ;
- backend Spring Boot sur Render / Railway / Fly.io / VPS Docker ;
- PostgreSQL gere sur Render / Railway / Neon / serveur prive ;
- Stripe pour les abonnements mensuels (Checkout + webhook).

Variables backend minimales :

```bash
SERVER_PORT=8080
DB_HOST=...
DB_PORT=5432
DB_NAME=salon_coiffure
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=une-cle-longue-et-secrete
CORS_ALLOWED_ORIGINS=https://app.ton-domaine.fr
APP_COIFFEUSE_NOM=CompteInitial
APP_COIFFEUSE_EMAIL=admin@ton-domaine.fr
APP_COIFFEUSE_PASSWORD=ChangeMe123!
```

Variables frontend :

- l'API doit pointer vers le domaine public du backend, par exemple `https://api.ton-domaine.fr/api`
- le frontend accepte aussi une config runtime via `window.__APP_CONFIG__.apiUrl`

Exemple dans `index.html` ou via injection serveur/CDN :

```html
<script>
  window.__APP_CONFIG__ = {
    apiUrl: 'https://api.ton-domaine.fr/api'
  };
</script>
```

Flux de billing mensuel recommande :

1. la coiffeuse cree son compte ou est invitee ;
2. le frontend ouvre une session Stripe Checkout abonnement mensuel ;
3. Stripe appelle un webhook backend sur paiement reussi / echec / annulation ;
4. le backend met a jour `subscriptionStatus`, `abonnementActifJusquAu`, `stripeCustomerId`, `stripeSubscriptionId` ;
5. les routes metier restent accessibles uniquement si l'abonnement est actif.

Pour finaliser le billing en production, il manque encore l'integration technique Stripe elle-meme (creation de Checkout Session + webhook signe). Le modele SaaS et le cloisonnement des donnees sont deja en place.

## API Clients

Toutes les routes ci-dessous demandent l'en-tete `Authorization: Bearer <token>` :

- `GET /api/clients?recherche=texte`
- `POST /api/clients`
- `PUT /api/clients/{id}`
- `DELETE /api/clients/{id}`

## API Rendez-vous

Toutes les routes ci-dessous demandent l'en-tete `Authorization: Bearer <token>` :

- `GET /api/rendez-vous?date=YYYY-MM-DD`
- `POST /api/rendez-vous`
- `PUT /api/rendez-vous/{id}`
- `DELETE /api/rendez-vous/{id}`

## API Dashboard

Toutes les routes ci-dessous demandent l'en-tete `Authorization: Bearer <token>` :

- `GET /api/dashboard`
- `GET /api/dashboard?date=YYYY-MM-DD`

## API Finance

Toutes les routes ci-dessous demandent l'en-tete `Authorization: Bearer <token>` :

- `GET /api/finance`
- `GET /api/finance?debut=YYYY-MM-DD&fin=YYYY-MM-DD`
- `POST /api/finance/depenses`
- `PUT /api/finance/depenses/{id}`
- `DELETE /api/finance/depenses/{id}`
