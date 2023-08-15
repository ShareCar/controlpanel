package pt.sharecar.registration;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Arrays;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

    private static final Logger LOG = Logger.getLogger(CustomerRepository.class);

    @Inject
    EntityManager em;

    @Inject
    Keycloak keycloak;

    public Customer findBySubdomain(String subdomain) {
        return find("subdomain", subdomain).firstResult();
    }

    public boolean schemaExists(String subdomain) {
        String queryStr = "SELECT COUNT(schema_name) FROM information_schema.schemata WHERE schema_name = :subdomain";
        Query query = em.createNativeQuery(queryStr).setParameter("subdomain", subdomain);
        Long result = (Long) query.getSingleResult();
        return result != null && result > 0;
    }

    @Transactional
    public void createSchema(String subdomain) {
        LOG.info("Creating schema in database");
        String query = "CREATE SCHEMA " + subdomain;
        em.createNativeQuery(query).executeUpdate();
    }

    //TODO: Tratar quando não conseguir criar realms
    public void createRealm(String subdomain) {
        LOG.info("Creating realm in Keycloak");
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(subdomain);
        realmRepresentation.setEnabled(true);
        keycloak.realms().create(realmRepresentation);
    }

    //TODO: Tratar quando não conseguir criar usuário
    public void createUser(Customer customer) {
        LOG.info("Creating user in Keycloak");
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(true);
        credential.setValue("123456");

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(customer.getFirstName());
        userRepresentation.setLastName(customer.getLastName());
        userRepresentation.setEmail(customer.getEmail());
        userRepresentation.setCredentials(Arrays.asList(credential));
        Response response = keycloak.realm("master").users().create(userRepresentation);
        LOG.debug(response);
    }

}
