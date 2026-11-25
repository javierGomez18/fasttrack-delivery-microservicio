package org.javierGomez18.fasttrack.delivery.microservicio.infrastructure.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MetadataMapper {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public String map(Map<String, Object> metadata) {
    if (metadata == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.writeValueAsString(metadata);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("No se pudo serializar metadata a JSON", e);
    }
  }

  public Map<String, Object> map(String metadata) {
    if (metadata == null) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readValue(metadata, new TypeReference<Map<String, Object>>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("No se pudo deserializar metadata desde JSON", e);
    }
  }
}
