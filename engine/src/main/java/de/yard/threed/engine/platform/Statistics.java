package de.yard.threed.engine.platform;

/**
 * Created on 11.10.18.
 */
public  class Statistics{
    public int vertices = 0;
    public int normals = 0;
    public int indices = 0;
    public int uvs = 0;
    public int geometries=0;
    public int textures=0;
    public int texturefailures=0;
    
    public int calcGeometryMBs(){
        int bytes=0;
        bytes+=vertices*12;
        bytes+=normals*12;
        bytes+=indices*4;//2??
        bytes+=uvs*8;
        return bytes / (1024*1024);
    }
}