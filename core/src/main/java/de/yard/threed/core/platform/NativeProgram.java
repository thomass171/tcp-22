package de.yard.threed.core.platform;

public interface NativeProgram {
    void addSampler2DUniform(String name);
    // mat3 not supported in JME, a workaround will apply
    void addMatrix3Uniform(String name);
    void addFloatVec4Uniform(String name);
    void addFloatVec3Uniform(String name);
    void addBooleanUniform(String name);
    void addFloatUniform(String name);

    void compile();

}
