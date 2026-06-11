package org.entcore.test.preparation;

public class StructureTest {

    private final String id;
    private final String name;
    private final boolean defaultAuthFederated;

    public StructureTest(final String id, final String name) {
        this.id = id;
        this.name = name;
        this.defaultAuthFederated = false;
    }

    public StructureTest(final String id, final String name, boolean defaultAuthFederated) {
        this.id = id;
        this.name = name;
        this.defaultAuthFederated = defaultAuthFederated;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDefaultAuthFederated() {
        return defaultAuthFederated;
    }


}
