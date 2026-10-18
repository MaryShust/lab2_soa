package inf_system.lab2.bars.resource;

import inf_system.lab2.bars.model.ErrorResponse;
import inf_system.lab2.bars.service.BarsService;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BarsExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException exception) {
        if (exception instanceof BarsService.BadRequestException) {
            return build(Response.Status.BAD_REQUEST, exception.getMessage());
        }
        if (exception instanceof BarsService.NotFoundException) {
            return build(Response.Status.NOT_FOUND, exception.getMessage());
        }
        if (exception instanceof BarsService.ConflictException) {
            return build(Response.Status.CONFLICT, exception.getMessage());
        }
        return build(Response.Status.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    private Response build(Response.Status status, String message) {
        ErrorResponse body = new ErrorResponse(status.getStatusCode(), message);
        return Response.status(status).entity(body).build();
    }
}
