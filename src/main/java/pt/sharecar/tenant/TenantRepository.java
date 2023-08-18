package pt.sharecar.tenant;

import io.agroal.api.AgroalDataSource;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.liquibase.LiquibaseFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import pt.sharecar.exceptions.KeycloakUserCreationException;
import pt.sharecar.exceptions.LiquibaseExecutionException;
import pt.sharecar.exceptions.SchemaCreationException;
import pt.sharecar.exceptions.TenantCreationException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

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

    public boolean schemaExists(String schema) {
        String queryStr = "SELECT COUNT(schema_name) FROM information_schema.schemata WHERE schema_name = :schema";
        Query query = em.createNativeQuery(queryStr).setParameter("schema", schema);
        Long result = (Long) query.getSingleResult();
        return result != null && result > 0;
    }

    @Transactional
    public void createSchema(String schema) {
        String query = "CREATE SCHEMA IF NOT EXISTS " + schema;
        try {
            em.createNativeQuery(query).executeUpdate();
            LOG.info(String.format("Schema '%s' created in the database", schema));
        } catch (Exception e) {
            LOG.error(String.format("Error creating schema %s: %s", schema, e.getMessage()));
            throw new SchemaCreationException("Error creating schema", e);
        }
    }

    //TODO Verificar se precisa voltar para o esquema default
    public void runLiquibase(String schema) {
        LOG.info(String.format("Running Liquibase for the new schema: %s", schema));
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(schema);
            Liquibase liquibase = new Liquibase("db/db.changelog-tenant.yaml", new ClassLoaderResourceAccessor(), database);
            liquibase.update(new Contexts(), new LabelExpression());
            LOG.info(String.format("Liquibase update complete for schema: %s", schema));
        } catch (SQLException | LiquibaseException e) {
            LOG.error(String.format("Error running Liquibase for schema [%s]: %s", schema, e.getMessage()));
            throw new LiquibaseExecutionException("Error running Liquibase", e);
        }
    }

    public void createRealm(String realm) {
        LOG.info(String.format("Creating realm in Keycloak: [%s]", realm));
        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setRealm(realm);
        realmRepresentation.setEnabled(true);
        try {
            keycloak.realms().create(realmRepresentation);
            LOG.info(String.format("Realm [%s] created successfully", realm));
        } catch (Exception e) {
            LOG.error(String.format("Error creating realm [%s]: %s", realm, e.getMessage()));
            throw new TenantCreationException("Error creating realm on Keycloak", e);
        }
    }

    //TODO: Receber senha e criptografar
    public void createUser(Tenant tenant) {
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant cannot be null");
        }
        LOG.info(String.format("Creating user in Keycloak for tenant: %s", tenant.getSubdomain()));

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
        try {
            Response response = keycloak.realm("master").users().create(userRepresentation);
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                LOG.info(String.format("User created successfully for tenant: %s", tenant.getSubdomain()));
            } else {
                LOG.warn(String.format("User creation for tenant [%s] returned status code: %s", tenant.getSubdomain(), response.getStatus()));
            }
        } catch (Exception e) {
            LOG.error(String.format("Error creating user for tenant [%s]: %s", tenant.getSubdomain(), e.getMessage()));
            throw new KeycloakUserCreationException("Erro creating user on realm master", e);
        }
    }

}
