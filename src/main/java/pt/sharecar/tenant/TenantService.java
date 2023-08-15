package pt.sharecar.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.logging.Logger;
import pt.sharecar.messages.AppMessages;

import java.util.Objects;

@ApplicationScoped
public class TenantService {

    private static final Logger LOG = Logger.getLogger(TenantService.class);

    @Inject
    AppMessages messages;

    @Inject
    TenantRepository tenantRepository;

    @Transactional
    public void add(Tenant tenant) {
        String tenantId = tenant.getId().toString();
        String db = tenantId.replace("-","_");
        String password = RandomStringUtils.randomAlphanumeric(8);

        // Verify if the database name is valid


        /*
        if (!isValidDomainName(tenant.getSubdomain()) ||
                Objects.nonNull(tenantRepository.findBySubdomain(tenant.getSubdomain())) ||
                tenantRepository.schemaExists(tenant.getSubdomain())) {
            throw new IllegalArgumentException(messages.error_invalid_subdomain());
        }

        try {
            tenantRepository.persist(tenant);
            tenantRepository.createSchema(tenant.getSubdomain());
            tenantRepository.createRealm(tenant.getSubdomain());
            tenantRepository.createUser(tenant);
        } catch (Exception e) {
            LOG.error(e);
        }
        */
    }

    private boolean isValidDomainName(String tenantName) {
        return tenantName.matches("^[a-zA-Z0-9]*$");
    }

}
