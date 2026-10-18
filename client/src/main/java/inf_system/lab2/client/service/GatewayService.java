package inf_system.lab2.client.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import inf_system.lab2.client.model.LabWork;
import inf_system.lab2.client.model.LabWorkForm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class GatewayService {

    private final String labWorkUrl;
    private final String barsUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public GatewayService(@Value("${services.labwork.url}") String labWorkUrl,
                          @Value("${services.bars.url}") String barsUrl) {
        this.labWorkUrl = trim(labWorkUrl);
        this.barsUrl = trim(barsUrl);
        this.httpClient = insecureClient();
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<LabWork> getLabWorks(int page, int size, String sort, String filter) {
        List<String> params = new ArrayList<>();
        params.add("page=" + page);
        params.add("size=" + size);
        if (sort != null && !sort.isBlank()) {
            params.add("sort=" + encode(sort));
        }
        if (filter != null && !filter.isBlank()) {
            params.add("filter=" + encode(filter));
        }
        HttpResponse<String> response = get(labWorkUrl + "/labworks?" + String.join("&", params));
        requireSuccess(response);
        return readList(response.body());
    }

    public LabWork getLabWork(Long id) {
        HttpResponse<String> response = get(labWorkUrl + "/labworks/" + id);
        requireSuccess(response);
        return read(response.body(), LabWork.class);
    }

    public LabWork createLabWork(LabWorkForm form) {
        String query = buildFormQuery(form);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(labWorkUrl + "/labworks?" + query))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = execute(request);
        requireSuccess(response);
        return read(response.body(), LabWork.class);
    }

    public LabWork updateLabWork(Long id, LabWorkForm form) {
        String query = buildFormQuery(form);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(labWorkUrl + "/labworks/" + id + "?" + query))
                .timeout(Duration.ofSeconds(30))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = execute(request);
        requireSuccess(response);
        return read(response.body(), LabWork.class);
    }

    public void deleteLabWork(Long id) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(labWorkUrl + "/labworks/" + id))
                .timeout(Duration.ofSeconds(30))
                .DELETE()
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = execute(request);
        requireSuccess(response);
    }

    public LabWork getMinimalAuthor() {
        HttpResponse<String> response = get(labWorkUrl + "/labworks/by-max-person-min");
        requireSuccess(response);
        return read(response.body(), LabWork.class);
    }

    public long countByDifficulty(String difficulty) {
        HttpResponse<String> response = get(labWorkUrl + "/labworks/by-difficulty?difficulty="
                + encode(difficulty));
        requireSuccess(response);
        return Long.parseLong(response.body().trim());
    }

    public List<LabWork> greaterThanMinimalPoint(Double value) {
        HttpResponse<String> response = get(labWorkUrl
                + "/labworks/by-minimal-point-greater-than?minimal-point=" + value);
        requireSuccess(response);
        return readList(response.body());
    }

    public LabWork addToDiscipline(Long disciplineId, Long labWorkId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(barsUrl + "/discipline/" + disciplineId + "/labwork/"
                        + labWorkId + "/add"))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = execute(request);
        requireSuccess(response);
        return read(response.body(), LabWork.class);
    }

    public LabWork increaseDifficulty(Long labWorkId, int steps) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(barsUrl + "/labwork/" + labWorkId
                        + "/difficulty/increase?steps-count=" + steps))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.noBody())
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = execute(request);
        requireSuccess(response);
        return read(response.body(), LabWork.class);
    }

    private String buildFormQuery(LabWorkForm form) {
        List<String> params = new ArrayList<>();
        add(params, "name", form.getName());
        add(params, "x", form.getX());
        add(params, "y", form.getY());
        add(params, "minimal-point", form.getMinimalPoint());
        add(params, "personal-qualities-minimum", form.getPersonalQualitiesMinimum());
        add(params, "average-point", form.getAveragePoint());
        add(params, "difficulty", form.getDifficulty());
        add(params, "author-name", form.getAuthorName());
        add(params, "author-birthday", form.getAuthorBirthday());
        add(params, "author-height", form.getAuthorHeight());
        add(params, "author-weight", form.getAuthorWeight());
        add(params, "author-passport-id", form.getAuthorPassportID());
        return String.join("&", params);
    }

    private void add(List<String> params, String key, Object value) {
        if (value == null) {
            return;
        }
        String text = String.valueOf(value);
        if (text.isEmpty()) {
            return;
        }
        params.add(key + "=" + encode(text));
    }

    private HttpResponse<String> get(String url) {
        return execute(HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .header("Accept", "application/json")
                .build());
    }

    private HttpResponse<String> execute(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new GatewayException(502, "Сервис недоступен: " + ex.getMessage());
        }
    }

    private void requireSuccess(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code >= 200 && code < 300) {
            return;
        }
        String message = "Ошибка сервиса (" + code + ")";
        String body = response.body();
        if (body != null && !body.isBlank()) {
            try {
                JsonNode node = mapper.readTree(body);
                if (node.hasNonNull("message")) {
                    message = node.get("message").asText();
                }
            } catch (Exception ignored) {
                message = body;
            }
        }
        throw new GatewayException(code, message);
    }

    private LabWork read(String body, Class<LabWork> type) {
        try {
            return mapper.readValue(body, type);
        } catch (Exception ex) {
            throw new GatewayException(502, "Некорректный ответ сервиса");
        }
    }

    private List<LabWork> readList(String body) {
        try {
            return mapper.readValue(body, mapper.getTypeFactory()
                    .constructCollectionType(List.class, LabWork.class));
        } catch (Exception ex) {
            throw new GatewayException(502, "Некорректный ответ сервиса");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String trim(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static HttpClient insecureClient() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{new X509TrustManager() {
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
            }}, new SecureRandom());

            SSLParameters parameters = new SSLParameters();
            parameters.setEndpointIdentificationAlgorithm("");

            return HttpClient.newBuilder()
                    .sslContext(context)
                    .sslParameters(parameters)
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось настроить HTTPS клиент", ex);
        }
    }

    public static class GatewayException extends RuntimeException {
        private final int status;

        public GatewayException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int getStatus() {
            return status;
        }
    }
}
