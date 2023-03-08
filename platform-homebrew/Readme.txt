21.1.2023 Hasn't been used for a long time with OpenGL. Some updates to latest design of engine might be missing.

LightedRotatingQuad and LwjglDuoQuadColored both work. Also 'Main' can run ReferenceScene.

The renderer is optional (OpenGl for having a display), so this platform can be used without any renderer or with a custom (like sceneserver
does).

On the glcontext level GlImplDummyForTests can be used if no opengl is used

Known issues:
- in ReferenceScene the picking ray only works from start position. not after 't'.