package pt.sharecar.registration;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.net.URI;

@Path("/registration")
public class RegistrationResource {

    private static final Logger LOG = Logger.getLogger(RegistrationResource.class);

    @Inject
    RegistrationService registrationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(CustomerDTO customerDTO) {
        try {
            Customer customer = new Customer(customerDTO.company, customerDTO.subdomain, customerDTO.firstName,
                    customerDTO.lastName, customerDTO.email);
            registrationService.addCustomer(customer);
            return Response.created(URI.create("/customers/".concat(customer.getId().toString()))).build();
        } catch (Exception e) {
            LOG.error(e);
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
