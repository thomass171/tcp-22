29.6.20: Not alle JSON serializer are that powerful like Javas, eg. Unity. So keep models really simple:
- no list and Map
- no "deep" outside structures (eg. LatLon) in properties. Only references to models located here.
- public will be added by J2C#

