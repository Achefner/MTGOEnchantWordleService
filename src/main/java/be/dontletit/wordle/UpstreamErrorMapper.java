package be.dontletit.wordle;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

/** Turns MTGODatabase failures into 502 Bad Gateway so the front end can tell them apart from its own errors. */
public class UpstreamErrorMapper {

    private static final Logger LOG = Logger.getLogger(UpstreamErrorMapper.class);

    @ServerExceptionMapper
    public Response mapUnreachable(ProcessingException e) {
        LOG.warnf("MTGODatabase unreachable: %s", e.getMessage());
        return badGateway("Card database unreachable");
    }

    @ServerExceptionMapper
    public Response mapUpstreamFailure(WebApplicationException e) {
        int status = e.getResponse() != null ? e.getResponse().getStatus() : 500;
        // 4xx raised by this service itself (e.g. NotFoundException) pass through untouched.
        if (status >= 400 && status < 500) return e.getResponse();
        LOG.warnf("MTGODatabase answered %d", status);
        return badGateway("Card database failed with status " + status);
    }

    private static Response badGateway(String message) {
        return Response.status(Response.Status.BAD_GATEWAY)
                .entity("{\"error\": \"" + message + "\"}")
                .type("application/json")
                .build();
    }
}
