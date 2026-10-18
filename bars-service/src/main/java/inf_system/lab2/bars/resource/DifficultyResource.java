package inf_system.lab2.bars.resource;

import inf_system.lab2.bars.model.LabWork;
import inf_system.lab2.bars.service.BarsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/labwork/{labwork-id}/difficulty")
@Produces(MediaType.APPLICATION_JSON)
public class DifficultyResource {

    @Inject
    private BarsService barsService;

    @POST
    @Path("/increase")
    public LabWork increaseDifficulty(@PathParam("labwork-id") Long labWorkId,
                                      @QueryParam("steps-count") Integer stepsCount) {
        return barsService.increaseDifficulty(labWorkId, stepsCount);
    }
}
