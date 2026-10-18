package inf_system.lab2.bars.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import inf_system.lab2.bars.model.LabWork;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Optional;

public class LabWorkClient {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final String baseUrl;
    private final HttpClient httpClient;

    public LabWorkClient(String baseUrl) {
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.httpClient = buildInsecureHttpClient();
    }

    public Optional<LabWork> getById(Long id) {
        HttpResponse<String> response = sendGet("/labworks/" + id);
        if (response.statusCode() == 200) {
            return Optional.of(readBody(response.body(), LabWork.class));
        }
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        if (response.statusCode() == 400) {
            throw new BadUpstreamRequestException(extractMessage(response.body()));
        }
        throw new UpstreamFailureException(response.statusCode(), extractMessage(response.body()));
    }

    public LabWork update(LabWork labWork) {
        String query = new LabWorkQueryBuilder().build(labWork);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/labworks/" + labWork.getId() + "?" + query))
                .timeout(Duration.ofSeconds(30))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = execute(request);
        if (response.statusCode() == 200) {
            return readBody(response.body(), LabWork.class);
        }
        if (response.statusCode() == 404) {
            throw new NotFoundUpstreamException(extractMessage(response.body()));
        }
        if (response.statusCode() == 400) {
            throw new BadUpstreamRequestException(extractMessage(response.body()));
        }
        throw new UpstreamFailureException(response.statusCode(), extractMessage(response.body()));
    }

    public long countByDifficulty(String difficulty) {
        HttpResponse<String> response = sendGet("/labworks/by-difficulty?difficulty=" + difficulty);
        if (response.statusCode() == 200) {
            return Long.parseLong(response.body().trim());
        }
        throw new UpstreamFailureException(response.statusCode(), extractMessage(response.body()));
    }

    private HttpResponse<String> sendGet(String path) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .header("Accept", "application/json")
                .build();
        return execute(request);
    }

    private HttpResponse<String> execute(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new UpstreamFailureException(502,
                    "Сбой вызова первого сервиса: " + ex.getMessage());
        }
    }

    private <T> T readBody(String body, Class<T> type) {
        try {
            return MAPPER.readValue(body, type);
        } catch (Exception ex) {
            throw new UpstreamFailureException(502, "Некорректный ответ первого сервиса");
        }
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return "Ошибка первого сервиса";
        }
        try {
            return MAPPER.readTree(body).path("message").asText("Ошибка первого сервиса");
        } catch (Exception ex) {
            return "Ошибка первого сервиса";
        }
    }

    private static String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static HttpClient buildInsecureHttpClient() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            sslContext.init(null, trustAll, new SecureRandom());

            return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .sslParameters(buildSslParameters())
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось настроить HTTPS клиент", ex);
        }
    }

    private static javax.net.ssl.SSLParameters buildSslParameters() {
        javax.net.ssl.SSLParameters parameters = new javax.net.ssl.SSLParameters();
        parameters.setEndpointIdentificationAlgorithm("");
        return parameters;
    }

    public static class UpstreamFailureException extends RuntimeException {
        private final int status;

        public UpstreamFailureException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }

    public static class BadUpstreamRequestException extends RuntimeException {
        public BadUpstreamRequestException(String message) {
            super(message);
        }
    }

    public static class NotFoundUpstreamException extends RuntimeException {
        public NotFoundUpstreamException(String message) {
            super(message);
        }
    }
}
