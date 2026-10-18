package inf_system.lab2.bars.config;

public final class ServiceConfig {

    private static final String DEFAULT_BASE_URL = "https://localhost:8181";

    private ServiceConfig() {
    }

    public static String labWorkBaseUrl() {
        String fromEnv = System.getProperty("labwork.service.url");
        if (fromEnv == null || fromEnv.isBlank()) {
            fromEnv = System.getenv("LABWORK_SERVICE_URL");
        }
        return (fromEnv == null || fromEnv.isBlank()) ? DEFAULT_BASE_URL : fromEnv;
    }
}
