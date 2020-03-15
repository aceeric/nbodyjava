# Java N-Body Simulation

An n-body simulation that was inspired by this example: <http://physics.princeton.edu/~fpretori/Nbody/code.htm>. Calculates force - and changes in position - on multiple bodies in one or more threads, and renders them in a graphics engine in a separate thread. [JMonkeyEngine](https://jmonkeyengine.org/) (JME) is used as the graphics engine. The simulation implements elastic collisions in 3D thanks to: https://www.plasmaphysics.org.uk/programs/coll3d_cpp.htm. It supports different behaviors for the bodies when they collide:
1. Subsume - larger radius bodies absorb smaller radius bodies
2. Elastic Collision - bodies bounce off each other (thank you plasmaphysics.org.uk!)
3. Fragment - bodies fragment into smaller bodies based on force of impact
4. None - bodies pass through each other

This has been tested on a 12 core Ubuntu 18.04.3 LTS desktop with an integrated Intel graphics card. With this configuration, about 2000 bodies can be run with the JME frame rate running in the 50's. More bodies (approaching the 3000's) will start to slow the simulation. The number of bodies the sim can compute directly relates to the number of cores, and CPU speed. JMonkey is more influenced, obviously, by the number of bodies it has to render.

### Design:

![Design](additional/images/design.jpg)

Key points:
1. In the diagram, the green items were developed as part of this project, and the blue items are components integrated into this project.
2. A body queue holds all bodies in the simulation. A thread pool continually computes force, position, and collisions on the body queue.
3. Once each compute cycle is complete, the body info is copied into a render queue. The JMonkey Engine renders the render queue in its own thread while the compute thread pool runs another compute cycle on the body queue in parallel.
4. The user can interact with the simulation using a gRPC Java client that talks to a gRPC server. This allows the user to add/remove bodies, change simulation characteristics, and so on while the simulation is running. A simple shell script is provided to wrap the gRPC client.
5. Instrumentation is provided that integrates with Prometheus and Grafana to provide instrumentation on simulation characteristics like thread interaction, etc. So using the gRPC client, you can increase/decrease threads and see the impact on performance, and so on. See https://prometheus.io/ and https://grafana.com/ for more information on those components.
6. The `additional/scripts` directory provides a Bash script (`start-containers`) to start Prometheus and Grafana in Docker containers with configurations that automatically integrate with the simulation.

This is a multi-module Maven project that produces two jars with dependencies:

1. server/target/server.jar is the simulation runner. You start it like: `java -jar server/target/server.jar`
2. client/target/client.jar is the gRPC client.  You start it like: `java -jar client/target/client.jar`. Or, you can use the `additional/scripts/nbcli` Bash script after you tweak it for your environment.

### This is an initial version with some cleanups still to do: 

* Add a guide to running the server and the client
* Review Javadocs
* Windows testing - currently running exclusively on Ubuntu
* Install my Geoforce RTX and see what that offers in terms of performance

### Modules:
TODO

### The following primary classes comprise the N-Body package:
TODO

### The following classes comprise the gRPC Server package:
TODO

### The following classes comprise the Instrumentation package:
TODO

### The following classes comprise the Sim package:
TODO

### The "additional" directory contents
TODO

### To run the application
The latest version of Java that I have tested with is 11.0.6:
```
$ java --version
openjdk 11.0.6 2020-01-14
OpenJDK Runtime Environment (build 11.0.6+10-post-Ubuntu-1ubuntu118.04.1)
OpenJDK 64-Bit Server VM (build 11.0.6+10-post-Ubuntu-1ubuntu118.04.1, mixed mode, sharing)
```
So to build and run:
```
$ mvn package
$ java -jar server/target/server.jar&
```
Or, with the included Prometheus instrumentation to use the included Grafana dashboard:
```
$ java -Dorg.ericace.instrumentation.class=org.ericace.instrumentation.PrometheusInstrumentation \
 -jar server/target/server.jar&
```
The above command (no args) runs the default canned simulation, which starts with four spherical clusters of bodies orbiting a sun. There are five canned sims, controlled via the `--sim-name` arg: `--sim-name=sim1` (the default) through `--sim-name=sim5`. You can also do: `--sim-name=empty` for an empty sim and then use the gRPC client to add bodies.
 
 Finally you can do `--csv=/path/to/a/csv` - will document that in the future. Some examples in `additional/csvs`

Note - the sim is presently hard-coded to run in 2560x1405 resolution. You can override that by supplying the --resolution arg with a value that makes sense for your configuration. E.g.:
```
$ java -jar server/target/server.jar --resolution=2000x1000&
```
With JME (I think due to the underlying libs) there appear to be two options with regard to screen resolution: Option 1 supports full screen but you can't detach/attach the mouse and keyboard. Option 2 supports a windowed display - which is what I use - but then you can't resize the window once it is created (or, I have not figured out how to do so.)

When you run the sim it takes control of the mouse and keyboard. F12 disengages the sim from the mouse and keyboard. So you can use other windows. To give the sim the mouse and the keyboard back, click on the sim window and press F12 and the mouse pointer will disappear - indicating the JMonkey again owns the mouse and keyboard. The sim is defaulted to run with five threads. You can override that and many other settings using command line options and params. Unfortunately, until I get some docs going, I refer you to the `Main` class in the `org.ericace.sim` package for details.

If you have questions, you can reach me at: ericace-at-protonmail-dot-com
