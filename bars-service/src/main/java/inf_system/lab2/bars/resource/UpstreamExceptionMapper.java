package inf_system.lab2.bars.resource;

import inf_system.lab2.bars.client.LabWorkClient;
import inf_system.lab2.bars.model.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UpstreamExceptionMapper implements ExceptionMapper<LabWorkClient.UpstreamFailureException> {

    @Override
    public Response toResponse(LabWorkClient.UpstreamFailureException exception) {
        ErrorResponse body = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                exception.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
