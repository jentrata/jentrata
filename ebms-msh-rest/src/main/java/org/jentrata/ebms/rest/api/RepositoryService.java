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
    @Path("/{direction}/message/{id}")
    public Response findMessageById(@PathParam("id") String messageId, @PathParam("direction") String direction) {
        return null;
    }

    @GET
    @Path("/{direction}/message/status/{status}/")
    public Response findMessageByStatus(@PathParam("direction") String direction, @PathParam("status") String messageStatus) {
        return null;
    }

    @GET
    @Path("/{direction}/payload/{id}/")
    public Response findPayloadById(@PathParam("id") String messageId, @PathParam("direction") String direction) {
        return null;
    }
}
