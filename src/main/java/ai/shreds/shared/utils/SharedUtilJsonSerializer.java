package ai.shreds.shared.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SharedUtilJsonSerializer {
    private static final Logger logger = LoggerFactory.getLogger(SharedUtilJsonSerializer.class);
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            handleSerializationError(e);
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    public <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            handleSerializationError(e);
            throw new RuntimeException("Failed to deserialize json to " + clazz.getSimpleName(), e);
        }
    }

    public <T> List<T> deserializeList(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (JsonProcessingException e) {
            handleSerializationError(e);
            throw new RuntimeException("Failed to deserialize json to List<" + clazz.getSimpleName() + ">", e);
        }
    }

    public Map<String, Object> serializeToMap(Object object) {
        try {
            return objectMapper.convertValue(object, new TypeReference<Map<String, Object>>() {});
        } catch (IllegalArgumentException e) {
            handleSerializationError(e);
            throw new RuntimeException("Failed to convert object to Map", e);
        }
    }

    private void handleSerializationError(Exception error) {
        logger.error("JSON serialization error: {}", error.getMessage());
        logger.debug("Detailed error: ", error);
    }
}