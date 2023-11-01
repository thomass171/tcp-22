package de.yard.threed.traffic;

@FunctionalInterface
public interface AbstractTrafficGraphFactory {
    TrafficGraph buildGraph();
}
