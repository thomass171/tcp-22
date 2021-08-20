Hier (platform/common) sind die Klassen, die eigentlich zur Engine gehören, weil sie platformübergreifend sind,
andererseits aber praktischerweise auch aus der Platform verwendet werden.
9.3.16: Darum gehören sie aber auch nicht zur Engine. Die Plattform stellt sie der
Engine zur Verfügung. Die Klassen müssen alle Platformunabhänig sein, z.B. GWT fähig.

30.6.21: Aber ist das fuer Klassen, die sowohl innerhalb wie ausserhalb verwendet werden, überhaupt sinnvoll?
  SimpleFeometry ist in der Platform, Material nicht. Warum? Hat das mit den Interfaces in EnginePlatform zu tun?