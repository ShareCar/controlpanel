package pt.sharecar.registration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import pt.sharecar.messages.AppMessages;

import java.util.Objects;

@ApplicationScoped
public class RegistrationService {

    private static final Logger LOG = Logger.getLogger(RegistrationService.class);

    @Inject
    AppMessages messages;

    @Inject
    CustomerRepository customerRepository;

    @Transactional
    public void addCustomer(Customer customer) {
        if (!isValidDomainName(customer.getSubdomain()) ||
                Objects.nonNull(customerRepository.findBySubdomain(customer.getSubdomain())) ||
                customerRepository.schemaExists(customer.getSubdomain())) {
            throw new IllegalArgumentException(messages.error_invalid_subdomain());
        }

        try {
            customerRepository.persist(customer);
            customerRepository.createSchema(customer.getSubdomain());
            customerRepository.createRealm(customer.getSubdomain());
            customerRepository.createUser(customer);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private boolean isValidDomainName(String tenantName) {
        return tenantName.matches("^[a-zA-Z0-9]*$");
    }

}
