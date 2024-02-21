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
| Kotlin      | 1.9.22          |
| Spring Boot | 3.2.2           |

## Installasjon

`$ ./mvnw clean install`

### Kjør lokalt

Det anbefales å bruke Spring Boot konfigurasjon via IntelliJ Run Configurations.

#### JWK oppsett

Kopier [application-dev-gcp.yml](./src/main/resources/application-dev-gcp.yml) og endre navnet på filen til
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

#### Maven oppsett

Du trenger tilgang til å hente NAV spesifikk verktøy på GitHub. Dette gjør du ved å legge til innstillinger i
din `~/.m2/settings.xml`:

Følg [denne guiden på GitHub](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
for å generere Personal Access Token (PAT) med riktig scope.
Maven innstillingene skal til slutt inneholde disse verdiene:

```xml

<repository>
    <id>helse</id>
    <name>GitHub felles Apache Maven Packages</name>
    <url>https://maven.pkg.github.com/navikt/helseid-nav-test</url>
</repository>

<servers>
<server>
    <id>github</id>
    <username>[ditt github brukernavn]</username>
    <password>[din personal access token]</password>
</server>
</servers>
```

Husk at du må autorisere PATen på NAV IKT og NAIS. Dette gjør du i menyen på PATen.

Dersom du bruker IntelliJ bør du huke av for at din lokale `settings.xml` overskriver default Maven settings i
innstillingene.
Dette finner du i **Build, Execution, Deployment > Build Tools > Maven**. Huk av *User settings file override*

### Docker

Vi bruker lokal JIB Maven for automatisk oppsett av Docker image. For å opprette et image, kjør

`$ ./mvnw compile com.google.cloud.tools:jib-maven-plugin:3.3.2:dockerBuild`

## Support

NAV ansatte kan kontakte teamet på [#team-helseopplysninger](https://app.slack.com/client/T5LNAMWNA/C01AQTAU3CH),