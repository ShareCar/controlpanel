package pt.sharecar.tenant;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.logging.Logger;

import java.net.URI;

@Path("/tenants")
public class TenantResource {

    private static final Logger LOG = Logger.getLogger(TenantResource.class);

    @Inject
    TenantService tenantService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid TenantDTO tenantDTO) {
        try {
            Tenant tenant = new Tenant(tenantDTO.company, tenantDTO.subdomain,
                    tenantDTO.firstName, tenantDTO.lastName, tenantDTO.email);
            tenantService.add(tenant);
            /*URI tenantUri = UriBuilder.fromPath("/tenants/")
                    .path(tenant.getId().toString())
                    .build();*/
            //return Response.created(tenantUri).build();
            return Response.ok().build();
//        }
//        catch (ValidationException ve) {
//            LOG.error("Validation error: " + ve.getMessage());
//            return Response.status(Response.Status.BAD_REQUEST).entity(ve.getMessage()).build();
//        } catch (TenantCreationException tse) {
//            LOG.error("Error adding tenant: " + tse.getMessage());
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to add tenant").build();
        } catch (Exception e) {
            LOG.error("Unexpected error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An unexpected error occurred").build();
        }
    }
}
