tzdataservice
=============

Java program and service to provide timezone names for given latitude and longitude based on data from an ESRI shapefile.

prerequisites
-------------

* Java JDK 1.7, Ant, Ivy

* Download tz\_world.zip from <http://efele.net/maps/tz/> and unpack it into an empty directory.

building
--------

        ant package
    

running the service
-------------------

        java -jar tzdataservice.jar /path/to/tz_world.shp


querying the REST service
-------------------------

E.g. with curl:

        curl http://localhost:28100/tz/bylonlat/9/50


More information
----------------        

The blog post [Zeitzone mit Geotools aus Shapefiles bestimmen](https://www.kompf.de/java/tzdata.html) explains the background and implementation of the program (in german language).


License
-------

Copyright 2015 Martin Kompf

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
