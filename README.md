# MTGOEnchantWordleService

Backend for the [MTGOEnchantWordle](../MTGOEnchantWordle) web app. A thin REST bridge
in front of [MTGODatabase](../MTGODatabase): no hexagonal layering, just a resource, a
REST client and a names cache.

## Endpoints (port 8081)

| Method | Path                | Returns                                                        |
|--------|---------------------|----------------------------------------------------------------|
| GET    | `/api/cards/random` | a random card (same JSON shape as MTGODatabase)                |
| GET    | `/api/cards/{name}` | the card with that name (case-insensitive), 404 when unknown   |
| GET    | `/api/cards/names`  | every card name, sorted; cached in memory after the first call |
| GET    | `/api/cards/sets`   | sets in which cards were first printed (code, name, type, release date, card count); cached |

MTGODatabase failures (unreachable, 5xx) are reported as `502 Bad Gateway`.

## Run

```bash
mvn quarkus:dev        # http://localhost:8081, expects MTGODatabase on http://localhost:8080
mvn test
```

Upstream URL: `quarkus.rest-client.mtgo-database.url` in `application.properties`
(override with `QUARKUS_REST_CLIENT_MTGO_DATABASE_URL=...`). CORS is open to any origin in the
`dev` profile only.
