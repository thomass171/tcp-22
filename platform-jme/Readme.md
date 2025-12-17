Notes about using JmonkeyEngine

# The IllegalStateException issue
In certain circumstances the exception

```
com.jme3.app.LegacyApplication handleError
SEVERE: Uncaught exception thrown in Thread[jME3 Main,5,main]
java.lang.IllegalStateException: Scene graph is not properly updated for rendering.
State was changed after rootNode.updateGeometricState() call.
Make sure you do not modify the scene from another thread!
Problem spatial name: ...
```

can occur, where the hint to multithreading might be misleading. However the
reason can be the sequence in the same frame.
1) Create a new scene node (spatial) without attaching it to a parent
2) Transform that node (which dirties its refresh state)

The renderer does a state refresh from root and gui node and will miss that node. But then it will detect the dirty state and throw the exception.

# Resource Loading
We have JmeFileLocator.
We also have JmeShaderLocator and JmeBundleFileLocator. And we have JavaBundleHelper for textures to load textures eg. from the web
 without involving any JME locator at all.

But JME insists on using the assetmanager (needed to do internal conversion of png and wav files), so
we need FileLocator even it is not for file loading any more.

Now also JmeUrlFileLocator