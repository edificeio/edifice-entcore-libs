package org.entcore.common.configuration;

public class EventBusConfigurationSupplierConfiguration {
    private final String routingFieldName;
    private final String routingValue;
    private final String bodyFieldName;
    private final String address;

    public EventBusConfigurationSupplierConfiguration(String routingFieldName, String routingValue, String bodyFieldName, String address) {
        this.routingFieldName = routingFieldName;
        this.routingValue = routingValue;
        this.bodyFieldName = bodyFieldName;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getRoutingFieldName() {
        return routingFieldName;
    }

    public String getRoutingValue() {
        return routingValue;
    }

    public String getBodyFieldName() {
        return bodyFieldName;
    }
}
