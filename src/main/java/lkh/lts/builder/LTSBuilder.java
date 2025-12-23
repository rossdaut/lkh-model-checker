package lkh.lts.builder;

import lkh.lts.LTS;

/**
 * Interface for building Labeled Transition Systems (LTS).
 * 
 * This interface defines a contract for classes that can construct an LTS from
 * various input sources like PDDL files, DOT format, or other model
 * representations. Implementations of this interface are responsible for
 * handling the construction details and returning a properly initialized LTS.
 * 
 * The LTS uses integers as state identifiers and strings as action labels, which
 * provides a standard representation format regardless of the source data format.
 */
public interface LTSBuilder {
    /**
     * Builds and returns a Labeled Transition System.
     * 
     * @return A fully constructed LTS with Integer states and String actions
     */
    LTS<Integer, String> buildLTS();
}
