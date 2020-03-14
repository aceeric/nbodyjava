/**
 * Contains the classes that start up the simulation
 * </p>
 * The following classes comprise the package:
 * <ol>
 * <li>{@link org.ericace.sim.Main} - Entry point for the simulation: creates an instance of the
 * {@link org.ericace.sim.NBodySim} class and hands control to it
 * <li>{@link org.ericace.sim.NBodySim} - The main simulation class - performs all initialization and starts the
 * various threads that comprise the simulation
 * <li>{@link org.ericace.sim.SimGenerator} - Generates various canned simulations, or loads a simulation from an
 * input stream. The resulting simulation is passed to the {@code NBodySim} instance
 * </ol>
 */
package org.ericace.sim;