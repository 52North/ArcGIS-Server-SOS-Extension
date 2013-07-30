# ArcGIS-Server-SOS-Extension

The Sensor Observation Service (SOS) extension for ArcGIS Server (10.1) is implemented as a 'Server Object Extension' (SOE).
<br>
It allows querying of <i>observations</i>, metadata about <i>procedures/sensors</i>, as well as descriptions of <i>features (of interest)</i> observed by the sensors.
<br>
<br>
The SOE's interface provides compliance with the <a href='http://www.opengeospatial.org/standards/sos'>SOS 2.0 standard</a>
from <a href='http://www.opengeospatial.org'>OGC</a>
as well as the new <a href='http://help.arcgis.com/en/arcgisserver/10.0/apis/rest/index.html'>GeoServices REST API</a> developed by <a href='http://www.esri.com'>ESRI</a>.
<br>
<br>
More information to this project can be found on its <a href='http://52north.org/communities/sensorweb/sosSOE/index.html'>website</a>.

## Response Content-Types

In order to retrieve responses with the HTTP-Header `Content-Type` set to `application/xml`, a request must define the URL parameter `f=xml` (instead of e.g. `f=pjson`). 