package be.dontletit.wordle;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/** REST client for the MTGODatabase microservice (URL: quarkus.rest-client.mtgo-database.url). */
@Path("/cards")
@Produces(MediaType.APPLICATION_JSON)
@RegisterRestClient(configKey = "mtgo-database")
public interface MtgoDatabaseClient {

    @GET
    @Path("/random")
    CardDto randomCard();

    @GET
    @Path("/{name}")
    CardDto cardByName(@PathParam("name") String name);

    @GET
    @Path("/names")
    List<String> cardNames();

    @GET
    @Path("/sets")
    List<SetDto> firstPrintingSets();
}
