package pt.sharecar.tenant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import pt.sharecar.messages.AppMessages;

@ApplicationScoped
public class TenantService {

    private static final Logger LOG = Logger.getLogger(TenantService.class);
    private static final String VALID_SCHEMA_NAME_REGEXP = "[A-Za-z0-9_]*";

    @Inject
    AppMessages messages;

    @Inject
    TenantRepository repository;

    public void add(Tenant tenant) throws Exception {

        final String subdomain = tenant.getSubdomain();

        if (!subdomain.matches(VALID_SCHEMA_NAME_REGEXP)) {
            throw new TenantCreationException("Invalid subdomain name: " + subdomain);
        }

        /*
        Criar o esquema - ok
        Criar a estrutura de tabelas no novo esquema - - ok
        Criar o realm no keycloak
        Criar o usuário na base de dados
        * */
        repository.createSchema(subdomain);
        repository.runLiquibase(subdomain);

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

}
