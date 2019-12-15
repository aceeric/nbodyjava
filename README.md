# Java N-Body Simulation

An n-body simulation that was inspired by [this example](http://physics.princeton.edu/~fpretori/Nbody/code.htm). Calculates force - and changes in position - on multiple bodies in one or more threads, and renders them in a graphics engine in a separate thread. Currently, [JMonkeyEngine](https://jmonkeyengine.org/) is being used as the graphics engine.

This has been tested on a 12 core Ubuntu 18.04.3 LTS desktop with 32 gig of RAM and an integrated Intel graphics card. With this configuration, about 2000 bodies can be run with the JME frame rate running in the 50's. More bodies (in the 3000's) will start to slow the JME frame rate though I'm only just getting started with  the engine.

The design is to separate the simulation from the rendering engine as much as possible in the hopes of experimenting with different rendering engines in the future, although, building the sim in Java might limit alternatives.

### This is an initial version with some cleanups still to do: 

* Get clear on the proper way to use radius for collision detection and replace `Body.accumulateForceFrom dist > 0.61F` hack
* Tie light source to sun - allow multiple suns / multiple light sources
* Handle light source (sun) moving
* Implement different options for collision behavior. Currently have subsume. Add bounce and fragment
* Add Prometheus "HELP" field to metric initializer
* clean up the scripts directory - right now it's a collection of fragments
* Consider init with only the sun and add all other bodies via gRPS
* Add a guide to running the whole app

### The following classes comprise the N-Body package:

| Class | Purpose |
|-------|---------|
| Body | Contains the values and logic to represent a body in the simulation |
| BodyRenderInfo | light weight info about a body to provide to JME so calculations can be done in one thread on a Body instance, and results rendered in another using a BodyRenderInfo with no thead synchronization needed|
| ComputationRunner | Recomputes force and new positions for all bodies in the simulation using a thread pool running in a perpetual loop|
| JMEApp | Subclassed from the JME library, integrates the simulation with JME |
| NBodySim | Main class |
| ResultQueueHolder | Holds the results of a computation cycle: all bodies and their new positions. Allows the computation threads to slightly outrun the render thread |
| Vector | Simple x,y,z vector class to avoid bringing the JME `Vector3f` class into the package and introducing that as a dependency |

### The following classes comprise the gRPC Server package:

TODO

### The following classes comprise the Instrumentation package:

TODO

### To run the application

```
mvn package
java -jar target/n-body-java-1.0-SNAPSHOT-jar-with-dependencies.jar
```


