# HelseID NAV (test)

Test av HelseID pålogging til beskyttet REST endepunkt i NAV

<u>Innholdsfortegnelse</u>
* [Forutsetninger](#forutsetninger)
* [Installasjon](#installasjon)
  * [Kjør lokalt](#kjør-lokalt)
    * [JWK oppsett](#jwk-oppsett)
    * [(valgfritt) Opprett egen test klient](#valgfritt-opprett-egen-test-klient)
    * [Maven oppsett](#maven-oppsett)
  * [Docker](#docker)
* [Support](#support)
<!-- TOC -->

## Forutsetninger

| Teknologi   | Minimum versjon |
|-------------|-----------------|
| JDK         | 21              |

Du kan sjekke hvilken versjon du har installert, ved å bruke denne kommandoen:
``` shell
java -version
```

## Installasjon
``` shell
./mvnw clean install
```


### Kjør lokalt

Det anbefales å bruke Spring Boot konfigurasjon via IntelliJ Run Configurations.

#### JWK oppsett

Kopier [application.yml](./src/main/resources/application.yml) og endre navnet på filen til
application-local.yml

Gi filen følgende felter:

```yml
helse-id:
  issuer: https://helseid-sts.test.nhn.no
  jwk: '[JWK HER]'
```

JWK-en er en JSON-fil med nøkkel-verdi par. Verdiene kan du skaffe deg ved å
kontakte [Team Helseopplysninger](https://github.com/orgs/navikt/teams/helseopplysninger), eller hente via Kubernetes
Secrets i miljøet gitt at du har tilgang.

#### (valgfritt) Opprett egen test klient

Du kan opprette din egen klient i HelseID selvbetjening hvis du ønsker. Dette gjør du ved å

1. Få tilgang i selvbetjening test ved å spørre en av oss i Team Helseopplysninger
2. Logg inn på [NHN Selvbetjening TEST](https://selvbetjening.test.nhn.no/)
3. Opprett ny KLIENT SYSTEM under **Dine klientsystemer**
4. Opprett en ny KLIENT KONFIGURASJON under **Ta i bruk HelseID**
    1. Sørg for at konfigurasjonen opprettes på klienten du lagde i steg 3
5. Last ned konfigurasjonsfilen og legg til de nødvendige verdiene i `application-local.yml`

### Docker

Vi bruker lokal JIB Maven for automatisk oppsett av Docker image. For å opprette et image, kjør
``` shell
./mvnw compile dockerBuild`
```

## Support
Spørsmål knyttet til koden eller prosjektet kan stilles som issues her på GitHub.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen ![#team-helseopplysninger](https://app.slack.com/client/T5LNAMWNA/C01AQTAU3CH),
