# Java N-Body Simulation

An n-body simulation that was inspired by this example: <http://physics.princeton.edu/~fpretori/Nbody/code.htm>. Calculates force - and changes in position - on multiple bodies in one or more threads, and renders them in a graphics engine in a separate thread. Currently, [JMonkeyEngine](https://jmonkeyengine.org/) (JME) is being used as the graphics engine. Implements elastic collisions in 3D thanks to: https://www.plasmaphysics.org.uk/programs/coll3d_cpp.htm. Supports different behaviors for the bodies when they collide:
1. Subsume - larger radius bodies absorb smaller radius bodies
2. Elastic Collision - bodies bounce off each other (thank you plasmaphysics.org.uk!)
3. Fragment - bodies fragment into smaller bodies based on force of impact
4. None - bodies pass through each other

This has been tested on a 12 core Ubuntu 18.04.3 LTS desktop with an integrated Intel graphics card. With this configuration, about 2000 bodies can be run with the JME frame rate running in the 50's. More bodies (approaching the 3000's) will start to slow the simulation. The number of bodies the sim can compute directly relates to the number of cores, and CPU speed. JMonkey is more influenced, obviously, by the number of bodies it has to render. So a sim with lots of bodies but with many of them off screen runs plenty fast. 

The design is to separate the simulation from the rendering engine as much as possible in the hopes of experimenting with different rendering engines in the future, although, building the sim in Java might limit alternatives there.

The sim can be run with Prometheus monitoring and a Grafana dashboard to get visibility into performance, and the interaction between the body computation thread(s) and the JME rendering thread. See the `start-containers` script in the `additional/scripts` directory.

### This is an initial version with some cleanups still to do: 

* Clean up the scripts directory - right now it's a collection of fragments
* Complete client app and wrap with shell script
* Add a guide to running the whole app
* Comprehensive Javadocs
* Windows testing - currently running exclusively on Ubuntu
* Install my Geoforce RTX and see what that offers in terms of performance

### The following primary classes comprise the N-Body package:

| Class | Purpose |
|-------|---------|
| Body | Contains the values and logic to represent a body in the simulation |
| BodyRenderInfo | light weight info about a body to provide to JME so calculations can be done in one thread on a Body instance, and results rendered in another using a BodyRenderInfo with no thead synchronization needed|
| ComputationRunner | Recomputes force and new positions for all bodies in the simulation using a thread pool running in a perpetual loop|
| Configurables | Defines the interface for modifying simulation properties during runtime. The current version of the simulation supports modifying parameters via a gRPC server|
| JMEApp | Subclassed from the JME library, integrates the simulation with JMonkeyEngine |
| ResultQueueHolder | Holds the results of a computation cycle: all bodies and their new positions. Allows the computation threads to be de-coupled from the render thread |
| SimpleVector | Simple x,y,z vector class to avoid bringing the JME `Vector3f` class into the package and introducing that as a dependency. Also has some utility methods (thank you https://karthikkaranth.me!)|

### The following classes comprise the gRPC Server package:

TODO

### The following classes comprise the Instrumentation package:

TODO

### The following classes comprise the Sim package:

TODO

### To run the application
The latest successful version of Java that I have tested with is 10.0.2. I have not gotten the app to run under any later version. On my system, Java 10 is installed in `/opt/java-jdk/jdk-10.0.2`. So for me:
```
mvn package
/opt/java-jdk/jdk-10.0.2/bin/java -jar server/target/server.jar&
```
Or, with the included Prometheus instrumentation to use the included Grafana dashboard:
```
/opt/java-jdk/jdk-10.0.2/bin/java \
 -Dorg.ericace.instrumentation.class=org.ericace.instrumentation.PrometheusInstrumentation \
 -jar server/target/server.jar&
```
The above command (no args) runs the default canned simulation, which consists of four spherical clusters of bodies orbiting a sun. There are five canned sims, controlled via the `--sim-name` arg: `--sim-name=sim1` (the default) through `--sim-name=sim5`. You can also do: `--sim-name=none` for an empty sim and then use the gRPC CLI to add bodies. There are plenty of (messy) examples in `additional/scripts/temp-file`. It's a TODO to clean that up. (You will need to install the gRCP CLI)
 
 Finally you can do `--csv=/path/to/a/csv` - will document that in the future. Some examples in `additional/csvs`

Note - the sim is presently hard-coded to run in 2560x1405 resolution. You can override that by supplying the --resolution arg with a value that makes sense for your configuration. E.g.:
```
/opt/java-jdk/jdk-10.0.2/bin/java -jar server/target/server.jar --resolution=2000x1000&
```
If you have a dual-monitor configuration, JMonkey seems to have a mind of its own regarding which monitor to use, depending on the resolution you specify and so far I have not had success making this explicit. With JME (I think due to the underlying libs) there appear to be two options with regard to screen resolution: Option 1 supports full screen but you can't detach/attach the mouse and keyboard. Option 2 supports a windowed display - which is what I use - but then you can't resize the window once it is created (or, I have not figured out how to do so.)

When you run the sim it takes control of the mouse and keyboard. F12 disengages the sim from the mouse and keyboard. So you can use other windows. To give the sim the mouse and the keyboard back, click on the sim window and press F12 and the mouse pointer will disappear - indicating the JMonkey again owns the mouse and keyboard. The sim is defaulted to run with five threads. You can override that and many other settings using commandline options and params. Unfortunately, until I get some docs going, I refer you to the `Main` class in the `org.ericace.sim` package for details.

If you have questions, you can reach me at: ericace-at-protonmail-dot-com
