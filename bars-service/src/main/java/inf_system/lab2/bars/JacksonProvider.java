package inf_system.lab2.bars;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JacksonProvider extends JacksonJsonProvider implements ContextResolver<ObjectMapper> {

    private final ObjectMapper mapper;

    public JacksonProvider() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        setMapper(mapper);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return mapper;
    }
}
