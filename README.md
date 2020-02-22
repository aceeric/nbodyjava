# Java N-Body Simulation

An n-body simulation that was inspired by this example: <http://physics.princeton.edu/~fpretori/Nbody/code.htm>. Calculates force - and changes in position - on multiple bodies in one or more threads, and renders them in a graphics engine in a separate thread. Currently, [JMonkeyEngine](https://jmonkeyengine.org/) is being used as the graphics engine.

This has been tested on a 12 core Ubuntu 18.04.3 LTS desktop with 32 gig of RAM and an integrated Intel graphics card. With this configuration, about 2000 bodies can be run with the JME frame rate running in the 50's. More bodies (in the 3000's) will start to slow the JME frame rate though I'm only just getting started with  the engine.

The design is to separate the simulation from the rendering engine as much as possible in the hopes of experimenting with different rendering engines in the future, although, building the sim in Java might limit alternatives. The elastic collision algorithm was adapted from the following URL: https://www.plasmaphysics.org.uk/programs/coll3d_cpp.htm

### This is an initial version with some cleanups still to do: 

* Consider floats universally for performance comparison
* Add ability to set initial params (screen characteristics, etc.) from the command line or a config file
* Can't decrease size of thread pool
* Add Prometheus "HELP" field to metric initializer
* Clean up the scripts directory - right now it's a collection of fragments
* Perhaps a client app in Java or Java + shell
* Add a guide to running the whole app
* Comprehensive Javadocs

### The following classes comprise the N-Body package:

| Class | Purpose |
|-------|---------|
| Body | Contains the values and logic to represent a body in the simulation |
| BodyRenderInfo | light weight info about a body to provide to JME so calculations can be done in one thread on a Body instance, and results rendered in another using a BodyRenderInfo with no thead synchronization needed|
| ComputationRunner | Recomputes force and new positions for all bodies in the simulation using a thread pool running in a perpetual loop|
| Configurables | Defines the interface for modifying simulation properties during runtime. The current version of the simulation supports modifying parameters via a gRPC server|
| JMEApp | Subclassed from the JME library, integrates the simulation with JMonkeyEngine |
| ResultQueueHolder | Holds the results of a computation cycle: all bodies and their new positions. Allows the computation threads to slightly outrun the render thread |
| SimpleVector | Simple x,y,z vector class to avoid bringing the JME `Vector3f` class into the package and introducing that as a dependency with some utility methods |

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
/opt/java-jdk/jdk-10.0.2/bin/java -jar target/n-body-java-1.0-SNAPSHOT-jar-with-dependencies.jar
```

The above command runs canned simulation, which consists of four spherical clusters of bodies orbiting a sun.


