package lab_bd_DAO;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitário para criação de EntityManagerFactory com carregamento
 * seguro de credenciais via variáveis de ambiente.
 */
public final class JPAUtil {

    private static final String DEFAULT_PERSISTENCE_UNIT = "LabBD";

    private JPAUtil() {
    }

    /**
     * Cria o EntityManagerFactory aplicando credenciais de DB_USER e DB_PASSWORD
     * se estiverem definidas no ambiente.
     *
     * @return EntityManagerFactory configurado
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return getEntityManagerFactory(DEFAULT_PERSISTENCE_UNIT);
    }

    /**
     * Cria o EntityManagerFactory para a unidade especificada aplicando credenciais de ambiente.
     *
     * @param puName Nome da persistence unit
     * @return EntityManagerFactory configurado
     */
    public static EntityManagerFactory getEntityManagerFactory(String puName) {
        Map<String, Object> props = new HashMap<>();

        String dbUser = System.getenv("DB_USER");
        if (dbUser != null && !dbUser.isBlank()) {
            props.put("jakarta.persistence.jdbc.user", dbUser);
        }

        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbPassword != null) {
            props.put("jakarta.persistence.jdbc.password", dbPassword);
        }

        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isBlank()) {
            props.put("jakarta.persistence.jdbc.url", dbUrl);
        }

        return Persistence.createEntityManagerFactory(puName, props);
    }
}
