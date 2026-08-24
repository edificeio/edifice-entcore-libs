package org.entcore.broker.nats.utils;

import org.entcore.broker.nats.model.NATSContract;
import org.entcore.broker.nats.model.NATSEndpoint;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates an AsyncAPI v3.0.0 document from a NATSContract.
 */
public class AsyncApiGenerator {

  private static final Pattern SUBJECT_PARAM_PATTERN = Pattern.compile("\\{([^}]+)}");
  private static final String[] TAG_SUFFIXES = {"listener", "broker", "publisher", "proxy"};

  private final SchemaGeneratorUtil schemaGeneratorUtil;

  public AsyncApiGenerator(SchemaGeneratorUtil schemaGeneratorUtil) {
    this.schemaGeneratorUtil = schemaGeneratorUtil;
  }

  public Map<String, Object> generate(NATSContract contract) {
    Map<String, Object> doc = new LinkedHashMap<>();
    doc.put("asyncapi", "3.0.0");
    doc.put("info", buildInfo(contract));
    doc.put("defaultContentType", "application/json");
    doc.put("servers", buildServers());

    Map<String, Object> channels = new LinkedHashMap<>();
    Map<String, Object> operations = new LinkedHashMap<>();
    Map<String, Object> componentMessages = new LinkedHashMap<>();
    Map<String, Object> componentSchemas = new LinkedHashMap<>();

    for (NATSEndpoint endpoint : contract.getEndpoints()) {
      String channelId = toChannelId(endpoint.getSubject(), endpoint.getMethodName());
      String operationId = endpoint.getMethodName();
      boolean hasReply = endpoint.getResponseType() != null && !"void".equalsIgnoreCase(endpoint.getResponseType());

      // Build request message
      String requestMessageId = operationId + "Request";
      if (endpoint.getRequestType() != null) {
        String schemaName = simplifyTypeName(endpoint.getRequestType());
        componentMessages.put(requestMessageId, buildMessage(requestMessageId, schemaName, endpoint.getDescription(), hasReply));
        if (endpoint.getRequestSchema() != null) {
          Map<String, Object> schema = toSchemaObject(endpoint.getRequestSchema());
          componentSchemas.put(schemaName, schema);
          extractNestedSchemas(schema, componentSchemas);
        }
      }

      // Build response message (if any)
      String responseMessageId = null;
      if (hasReply) {
        responseMessageId = operationId + "Response";
        String schemaName = simplifyTypeName(endpoint.getResponseType());
        componentMessages.put(responseMessageId, buildMessage(responseMessageId, schemaName, null, false));
        if (endpoint.getResponseSchema() != null) {
          Map<String, Object> schema = toSchemaObject(endpoint.getResponseSchema());
          componentSchemas.put(schemaName, schema);
          extractNestedSchemas(schema, componentSchemas);
        }
      }

      // Build channel
      Map<String, Object> channel = new LinkedHashMap<>();
      channel.put("address", endpoint.getSubject());
      if (endpoint.getDescription() != null && !endpoint.getDescription().isEmpty()) {
        channel.put("description", endpoint.getDescription());
      }
      Map<String, Object> parameters = extractParameters(endpoint.getSubject());
      if (!parameters.isEmpty()) {
        channel.put("parameters", parameters);
      }
      Map<String, Object> channelMessages = new LinkedHashMap<>();
      channelMessages.put(requestMessageId, ref("#/components/messages/" + requestMessageId));
      if (responseMessageId != null) {
        channelMessages.put(responseMessageId, ref("#/components/messages/" + responseMessageId));
      }
      channel.put("messages", channelMessages);
      channels.put(channelId, channel);

      // Build operation
      Map<String, Object> operation = new LinkedHashMap<>();
      operation.put("action", "receive");
      operation.put("channel", ref("#/channels/" + channelId));
      if (endpoint.getDescription() != null && !endpoint.getDescription().isEmpty()) {
        operation.put("summary", endpoint.getDescription());
      }
      operation.put("tags", buildTags(endpoint.getClassName()));
      List<Map<String, Object>> opMessages = new ArrayList<>();
      opMessages.add(ref("#/channels/" + channelId + "/messages/" + requestMessageId));
      operation.put("messages", opMessages);
      if (responseMessageId != null) {
        Map<String, Object> reply = new LinkedHashMap<>();
        reply.put("channel", ref("#/channels/" + channelId));
        List<Map<String, Object>> replyMessages = new ArrayList<>();
        replyMessages.add(ref("#/channels/" + channelId + "/messages/" + responseMessageId));
        reply.put("messages", replyMessages);
        operation.put("reply", reply);
      }
      Map<String, Object> natsBinding = new LinkedHashMap<>();
      natsBinding.put("queue", "entcore");
      operation.put("bindings", Collections.singletonMap("nats", natsBinding));
      operation.put("x-proxy", endpoint.isProxy());
      operation.put("x-broadcast", endpoint.isBroadcast());
      operations.put(operationId, operation);
    }

    doc.put("channels", channels);
    doc.put("operations", operations);

    Map<String, Object> components = new LinkedHashMap<>();
    components.put("messages", componentMessages);
    components.put("schemas", componentSchemas);
    doc.put("components", components);

    return doc;
  }

  private Map<String, Object> buildServers() {
    Map<String, Object> servers = new LinkedHashMap<>();
    Map<String, Object> defaultServer = new LinkedHashMap<>();
    defaultServer.put("host", "nats://localhost:4222");
    defaultServer.put("protocol", "nats");
    defaultServer.put("description", "Default NATS broker");
    servers.put("default", defaultServer);
    return servers;
  }

  private Map<String, Object> buildInfo(NATSContract contract) {
    Map<String, Object> info = new LinkedHashMap<>();
    info.put("title", contract.getServiceName());
    info.put("version", contract.getVersion());
    return info;
  }

  private Map<String, Object> buildMessage(String messageId, String schemaName, String description, boolean withCorrelationId) {
    Map<String, Object> message = new LinkedHashMap<>();
    message.put("name", messageId);
    if (description != null && !description.isEmpty()) {
      message.put("description", description);
    }
    if (withCorrelationId) {
      Map<String, Object> correlationId = new LinkedHashMap<>();
      correlationId.put("location", "$message.header#/nats-reply-to");
      message.put("correlationId", correlationId);
    }
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("$ref", "#/components/schemas/" + schemaName);
    message.put("payload", payload);
    return message;
  }

  /**
   * Extracts {param} placeholders from a subject into AsyncAPI channel parameters.
   */
  private Map<String, Object> extractParameters(String subject) {
    Map<String, Object> parameters = new LinkedHashMap<>();
    Matcher matcher = SUBJECT_PARAM_PATTERN.matcher(subject);
    while (matcher.find()) {
      String paramName = matcher.group(1);
      Map<String, Object> paramDef = new LinkedHashMap<>();
      paramDef.put("description", "Value for " + paramName);
      parameters.put(paramName, paramDef);
    }
    return parameters;
  }

  /**
   * Builds tags from the enclosing class name: always "entcore" + a domain tag
   * derived by stripping trailing suffixes (listener, broker, publisher, proxy).
   */
  private List<Map<String, Object>> buildTags(String className) {
    List<Map<String, Object>> tags = new ArrayList<>();
    Map<String, Object> entcoreTag = new LinkedHashMap<>();
    entcoreTag.put("name", "entcore");
    tags.add(entcoreTag);

    if (className != null && !className.isEmpty()) {
      String simpleName = className;
      int lastDot = className.lastIndexOf('.');
      if (lastDot >= 0) {
        simpleName = className.substring(lastDot + 1);
      }
      String lower = simpleName.toLowerCase();
      boolean stripped = true;
      while (stripped) {
        stripped = false;
        for (String suffix : TAG_SUFFIXES) {
          if (lower.endsWith(suffix)) {
            lower = lower.substring(0, lower.length() - suffix.length());
            stripped = true;
          }
        }
      }
      if (!lower.isEmpty()) {
        Map<String, Object> domainTag = new LinkedHashMap<>();
        domainTag.put("name", lower);
        tags.add(domainTag);
      }
    }
    return tags;
  }

  private String toChannelId(String subject, String methodName) {
    return methodName + "Channel";
  }

  private String simplifyTypeName(String fullyQualifiedName) {
    if (fullyQualifiedName == null) {
      return "Unknown";
    }
    int lastDot = fullyQualifiedName.lastIndexOf('.');
    if (lastDot >= 0) {
      return fullyQualifiedName.substring(lastDot + 1);
    }
    return fullyQualifiedName;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> toSchemaObject(Object schema) {
    if (schema instanceof Map) {
      return new LinkedHashMap<>((Map<String, Object>) schema);
    }
    Map<String, Object> fallback = new LinkedHashMap<>();
    fallback.put("type", "object");
    return fallback;
  }

  @SuppressWarnings("unchecked")
  private void extractNestedSchemas(Map<String, Object> schema, Map<String, Object> componentSchemas) {
    Object properties = schema.get("properties");
    if (properties instanceof Map) {
      Map<String, Object> props = (Map<String, Object>) properties;
      for (Map.Entry<String, Object> entry : props.entrySet()) {
        if (entry.getValue() instanceof Map) {
          Map<String, Object> propSchema = (Map<String, Object>) entry.getValue();
          String title = (String) propSchema.get("title");
          if (title != null && !title.isEmpty()) {
            String simpleName = simplifyTypeName(title);
            componentSchemas.put(simpleName, new LinkedHashMap<>(propSchema));
            Map<String, Object> refMap = new LinkedHashMap<>();
            refMap.put("$ref", "#/components/schemas/" + simpleName);
            props.put(entry.getKey(), refMap);
            extractNestedSchemas(propSchema, componentSchemas);
          }
        }
      }
    }
    schema.remove("$id");
    schema.remove("title");
  }

  private Map<String, Object> ref(String refPath) {
    Map<String, Object> refMap = new LinkedHashMap<>();
    refMap.put("$ref", refPath);
    return refMap;
  }
}
