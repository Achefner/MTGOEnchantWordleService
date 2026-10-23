# CLAUDE.md

Thin REST bridge between the `../MTGOEnchantWordle` React app and `../MTGODatabase`.
Deliberately *not* hexagonal: one JAX-RS resource (`CardResource`), one MicroProfile REST
client interface (`MtgoDatabaseClient`, config key `mtgo-database`), one in-memory cache
(`CardNamesCache`) and an exception mapper (`UpstreamErrorMapper`, upstream failures → 502).

```bash
mvn quarkus:dev     # :8081 (test port :8091); needs MTGODatabase on :8080
mvn test            # @QuarkusTest + rest-assured; the REST client is replaced with @InjectMock
```

- `CardDto` mirrors MTGODatabase's `CardResponse` field for field; keep them in sync when the
  database adds a field (`firstPrintedAt`, `rarity`, `originalPrintedSetName` and `originalPrintedSetType` were added on 2026-10-23).
- `CardNamesCache` holds both reference lists (card names ~38k, first-printing sets ~550),
  each fetched from the database once and kept until restart; `clear()` exists for tests.
  An empty upstream list is never cached.
- `SetDto` mirrors MTGODatabase's `SetResponse`.
- Requires JDK 21 on PATH, same as MTGODatabase.
