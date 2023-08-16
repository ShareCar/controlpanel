package pt.sharecar.tenant;

import io.agroal.api.AgroalDataSource;
import io.quarkus.datasource.common.runtime.DataSourceUtil;
import io.quarkus.datasource.runtime.DataSourceRuntimeConfig;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.liquibase.LiquibaseFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class TenantRepository implements PanacheRepository<Tenant> {

    private static final Logger LOG = Logger.getLogger(TenantRepository.class);

    @Inject
    EntityManager em;

    @Inject
    Keycloak keycloak;

    @Inject
    LiquibaseFactory liquibaseFactory;

    @Inject
    AgroalDataSource dataSource;

    public Tenant findBySubdomain(String subdomain) {
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

    //TODO: Pensar em como melhorar esse código para tratar as possíveis exceções
    public void runLiquibase(String schema) throws Exception {
        Map<String, Object> scopeValues = new HashMap<>();
        Scope.child(scopeValues, () -> {
            try (Connection connection = dataSource.getConnection()) {
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                database.setDefaultSchemaName(schema);
                Liquibase liquibase = new Liquibase("db/db.changelog-tenant.yaml", new ClassLoaderResourceAccessor(), database);
                liquibase.update(""); // This will update the database to the latest changelog version
            } catch (Exception e) {
                // Handle exceptions
                throw new RuntimeException("Error running Liquibase", e);
            }
        });
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
    public void createUser(Tenant tenant) {
        LOG.info("Creating user in Keycloak");
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setTemporary(true);
        credential.setValue("123456");

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(tenant.getFirstName());
        userRepresentation.setLastName(tenant.getLastName());
        userRepresentation.setEmail(tenant.getEmail());
        userRepresentation.setCredentials(Arrays.asList(credential));
        Response response = keycloak.realm("master").users().create(userRepresentation);
        LOG.debug(response);
    }

}
