package org.jentrata.ebms.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
/**
 * Expose access to the Jentrata Repository via a REST API
 *
 * @author aaronwalker
 */
@Path("/repository")
public class RepositoryService {

    @GET
    @Path("/message/{id}")
    public Response findMessageById(@PathParam("id") String messageId) {
        return null;
    }

    @GET
    @Path("/payload/{id}/")
    public Response findPayloadById(@PathParam("id") String messageId) {
        return null;
    }
}
