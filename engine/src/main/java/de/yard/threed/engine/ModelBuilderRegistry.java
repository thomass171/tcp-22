package de.yard.threed.engine;

/**
 * Abstraction of model builder.
 *
 * Needed in client/server mode where clients and server need to use/agree on the same model builder by a unique key
 */
public interface ModelBuilderRegistry {

    ModelBuilder lookupModelBuilder(String key);
}
