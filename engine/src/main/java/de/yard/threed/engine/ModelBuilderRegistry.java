package de.yard.threed.engine;

/**
 * Abstraction of model builder.
 *
 * Needed in client/server mode where clients and server need to use/agree on the same model builder by a unique key
 */
public interface ModelBuilderRegistry {

    /**
     * Returns a model builder for 'key' and null if 'key' is not registered in the registry.
     */
    ModelBuilder lookupModelBuilder(String key);
}
