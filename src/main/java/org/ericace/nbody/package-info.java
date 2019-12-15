/**
 * Contains the classes that comprise the n-body simulation.
 * </p>
 * The following classes comprise the package:
 * <ol>
 * <li>{@link org.ericace.nbody.Body} - Encapsulates the properties of a body in the simulation: position, velocity,
 * mass, and behaviors associated with handling collisions
 * <li>{@link org.ericace.nbody.BodyRenderInfo} - A value class that holds those elements of a body that are needed
 * to render it by the graphics engine
 * <li>{@link org.ericace.nbody.ComputationRunner} - Runs the simulation calculations continuously - updating position
 * and velocity, etc.
 * <li>{@link org.ericace.nbody.Configurables} - Specifies the elements of the simulation that can be configured (i.e.
 * modified) while the simulation is running
 * <li>{@link org.ericace.nbody.JMEApp} - The JMonkeyEngine rendering engine
 * <li>{@link org.ericace.nbody.Main} - Entry point for the simulation: instantiates an instance of the
 * {@link org.ericace.nbody.NBodySim} class and hands control to it
 * <li>{@link org.ericace.nbody.NBodySim} - The main simulation class - performs all initialization and starts the
 * various threads that comprise the simulation
 * <li>{@link org.ericace.nbody.ResultQueueHolder} - Holds the results of each computation cycle
 * <li>{@link org.ericace.nbody.Vector} - a simple vector class
 * </ol>
 */
package org.ericace.nbody;