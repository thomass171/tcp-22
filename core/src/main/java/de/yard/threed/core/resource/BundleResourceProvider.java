package de.yard.threed.core.resource;

import de.yard.threed.core.resource.BundleResource;

/**
 * Created by thomass on 12.04.17.
 */
public interface BundleResourceProvider {
    BundleResource resolve(String resource/*, Bundle currrentbundle*/);
    boolean isAircraftSpecific();
}
