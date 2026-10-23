package be.dontletit.wordle;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

/** Front-end facing API: bridges the Enchant Wordle web app to MTGODatabase. */
@Path("/api/cards")
@Produces(MediaType.APPLICATION_JSON)
public class CardResource {

    @Inject
    @RestClient
    MtgoDatabaseClient database;

    @Inject
    CardNamesCache namesCache;

    @GET
    @Path("/random")
    public CardDto random() {
        return database.randomCard();
    }

    @GET
    @Path("/names")
    public List<String> names() {
        return namesCache.get();
    }

    @GET
    @Path("/sets")
    public List<SetDto> sets() {
        return namesCache.sets();
    }

    @GET
    @Path("/{name}")
    public CardDto byName(@PathParam("name") String name) {
        try {
            return database.cardByName(name);
        } catch (jakarta.ws.rs.WebApplicationException e) {
            if (e.getResponse() != null && e.getResponse().getStatus() == 404) {
                throw new NotFoundException("Card not found: " + name);
            }
            throw e;
        }
    }
}
