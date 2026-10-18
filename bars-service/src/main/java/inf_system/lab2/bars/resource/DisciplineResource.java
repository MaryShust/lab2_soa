package inf_system.lab2.bars.resource;

import inf_system.lab2.bars.model.LabWork;
import inf_system.lab2.bars.service.BarsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/discipline/{discipline-id}/labwork")
@Produces(MediaType.APPLICATION_JSON)
public class DisciplineResource {

    @Inject
    private BarsService barsService;

    @POST
    @Path("/{labwork-id}/add")
    public LabWork addLabWorkToDiscipline(@PathParam("discipline-id") Long disciplineId,
                                          @PathParam("labwork-id") Long labWorkId) {
        return barsService.addLabWorkToDiscipline(disciplineId, labWorkId);
    }
}
