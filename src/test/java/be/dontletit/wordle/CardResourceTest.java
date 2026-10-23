package be.dontletit.wordle;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class CardResourceTest {

    static final CardDto SERRA_ANGEL = new CardDto(
            "serra-angel", "Serra Angel", "{3}{W}{W}", List.of("CREATURE"), "Flying, vigilance.",
            4.0, 4.0, List.of(), List.of("Angel"), "W", List.of("lea", "m15"), "lea",
            "Limited Edition Alpha", "core", LocalDate.of(1993, 8, 5), "UNCOMMON", "https://img/serra.jpg");

    static final SetDto LEA = new SetDto("lea", "Limited Edition Alpha", "core", LocalDate.of(1993, 8, 5), 295);
    static final SetDto RTR = new SetDto("rtr", "Return to Ravnica", "expansion", LocalDate.of(2012, 10, 5), 274);

    @InjectMock
    @org.eclipse.microprofile.rest.client.inject.RestClient
    MtgoDatabaseClient database;

    @jakarta.inject.Inject
    CardNamesCache cache;

    @BeforeEach
    void resetCache() {
        cache.clear();
    }

    @Test
    void random_returnsTheUpstreamCard() {
        when(database.randomCard()).thenReturn(SERRA_ANGEL);

        given().when().get("/api/cards/random")
                .then().statusCode(200)
                .body("name", equalTo("Serra Angel"))
                .body("rarity", equalTo("UNCOMMON"))
                .body("firstPrintedAt", equalTo("1993-08-05"))
                .body("originalPrintedSetName", equalTo("Limited Edition Alpha"))
                .body("originalPrintedSetType", equalTo("core"))
                .body("originalPrintedSet", equalTo("lea"))
                .body("imgUrl", equalTo("https://img/serra.jpg"));
    }

    @Test
    void byName_returnsTheUpstreamCard() {
        when(database.cardByName("Serra Angel")).thenReturn(SERRA_ANGEL);

        given().when().get("/api/cards/Serra Angel")
                .then().statusCode(200)
                .body("id", equalTo("serra-angel"));
    }

    @Test
    void byName_unknownCard_is404() {
        when(database.cardByName("Nope")).thenThrow(new WebApplicationException(404));

        given().when().get("/api/cards/Nope")
                .then().statusCode(404);
    }

    @Test
    void upstreamUnreachable_is502() {
        when(database.randomCard()).thenThrow(new ProcessingException("connection refused"));

        given().when().get("/api/cards/random")
                .then().statusCode(502);
    }

    @Test
    void upstreamServerError_is502() {
        when(database.randomCard()).thenThrow(new WebApplicationException(500));

        given().when().get("/api/cards/random")
                .then().statusCode(502);
    }

    @Test
    void names_returnsTheUpstreamNames() {
        when(database.cardNames()).thenReturn(List.of("Black Lotus", "Serra Angel"));

        given().when().get("/api/cards/names")
                .then().statusCode(200)
                .body("$", contains("Black Lotus", "Serra Angel"));
    }

    @Test
    void names_areFetchedUpstreamOnlyOnce() {
        when(database.cardNames()).thenReturn(List.of("Black Lotus"));

        given().when().get("/api/cards/names").then().statusCode(200);
        given().when().get("/api/cards/names").then().statusCode(200);

        verify(database, times(1)).cardNames();
    }

    @Test
    void names_emptyUpstreamListIsNotCached() {
        when(database.cardNames()).thenReturn(List.of()).thenReturn(List.of("Black Lotus"));

        given().when().get("/api/cards/names").then().statusCode(200).body("$", empty());
        given().when().get("/api/cards/names").then().statusCode(200).body("$", contains("Black Lotus"));
    }

    @Test
    void sets_returnsTheUpstreamSets() {
        when(database.firstPrintingSets()).thenReturn(List.of(LEA, RTR));

        given().when().get("/api/cards/sets")
                .then().statusCode(200)
                .body("code", contains("lea", "rtr"))
                .body("[1].name", equalTo("Return to Ravnica"))
                .body("[1].type", equalTo("expansion"))
                .body("[1].releasedAt", equalTo("2012-10-05"))
                .body("[1].cardCount", equalTo(274));
    }

    @Test
    void sets_areFetchedUpstreamOnlyOnce() {
        when(database.firstPrintingSets()).thenReturn(List.of(LEA));

        given().when().get("/api/cards/sets").then().statusCode(200);
        given().when().get("/api/cards/sets").then().statusCode(200);

        verify(database, times(1)).firstPrintingSets();
    }

    @Test
    void sets_emptyUpstreamListIsNotCached() {
        when(database.firstPrintingSets()).thenReturn(List.of()).thenReturn(List.of(LEA));

        given().when().get("/api/cards/sets").then().statusCode(200).body("$", empty());
        given().when().get("/api/cards/sets").then().statusCode(200).body("code", contains("lea"));
    }

    @Test
    void cors_allowsTheFrontEndOrigin() {
        when(database.randomCard()).thenReturn(SERRA_ANGEL);

        given().header("Origin", "http://localhost:5173")
                .when().get("/api/cards/random")
                .then().statusCode(200)
                .header("Access-Control-Allow-Origin", "http://localhost:5173");
    }
}
