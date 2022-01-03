#
# included by java2cs.sh
#

declare -A MODULE_FILES
declare -A MODULE_EXCLUDE
MODULE_FILES[core]="
	de/yard/threed/core/platform
	de/yard/threed/core/resource
	de/yard/threed/core/testutil
	de/yard/threed/core/buffer
	de/yard/threed/core"
MODULE_EXCLUDE[core]=de/yard/threed/core/JavaStringHelper.java

MODULE_FILES[outofbrowser-common]="
	de/yard/threed/outofbrowser"
MODULE_EXCLUDE[outofbrowser-common]=de/yard/threed/core/XXX.java

MODULE_FILES[engine]="
	de/yard/threed/engine
	de/yard/threed/engine/loader
	de/yard/threed/engine/util
	de/yard/threed/engine/platform
	de/yard/threed/engine/platform/common
	de/yard/threed/engine/osm
	de/yard/threed/engine/apps
	de/yard/threed/engine/apps/reference
	de/yard/threed/engine/apps/vr
	de/yard/threed/engine/ecs
	de/yard/threed/engine/graph
	de/yard/threed/engine/mp
	de/yard/threed/engine/avatar
	de/yard/threed/engine/gui
	de/yard/threed/engine/geometry
	de/yard/threed/engine/util
	de/yard/threed/engine/vr
	de/yard/threed/engine/imaging
	de/yard/threed/engine/test"
MODULE_EXCLUDE[engine]=de/yard/threed/platform/HomeBrewRenderer.java,de/yard/threed/platform/SimpleHeadlessPlatform.java

MODULE_FILES[maze]="
	de/yard/threed/maze"
MODULE_EXCLUDE[maze]=de/yard/threed/maze/GridPath.java,de/yard/threed/maze/PathFinder.java

MODULE_FILES[graph]="
	de/yard/threed/graph"
MODULE_EXCLUDE[graph]=de/yard/threed/core/XXX.java

MODULE_FILES[traffic-core]="
	de/yard/threed/trafficcore
	de/yard/threed/trafficcore/model"
MODULE_EXCLUDE[traffic-core]=de/yard/threed/trafficcore/XXX.java

MODULE_FILES[traffic]="
	de/yard/threed/traffic
	de/yard/threed/traffic/apps
	de/yard/threed/traffic/config
	de/yard/threed/traffic/geodesy
	de/yard/threed/traffic/osm
	de/yard/threed/traffic/flight"
MODULE_EXCLUDE[traffic]=de/yard/threed/traffic/XXX.java
