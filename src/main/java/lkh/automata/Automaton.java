package lkh.automata;

import lkh.utils.Pair;

import java.util.List;
import java.util.Set;

/**
 * An abstract automaton
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
public interface Automaton<State, Symbol> {
    /**
     * Get the initial state of the automaton
     * @return the initial state
     */
    State getInitialState();

    /**
     * Get the set of final states of the automaton
     * @return the set of final states
     */
    Set<State> getFinalStates();

    /**
     * Get the alphabet of the automaton
     * @return the set of symbols in the alphabet
     */
    Set<Symbol> getAlphabet();

    /**
     * Add an initial state to the automaton
     * @param state the state to add
     */
    void setInitialState(State state);

    /**
     * Add a final state to the automaton
     * @param state the state to add
     */
    void addFinalState(State state);

    /**
     * Add multiple final states to the automaton
     * @param states the set of states to add as final states
     */
    void addFinalStates(Set<State> states);

    /**
     * Add a state to the automaton
     * @param state the state to add
     */
    void addState(State state);

    /**
     * Add a transition to the automaton
     * @param source the source state
     * @param target the target state
     * @param symbol the symbol of the transition
     */
    void addTransition(State source, State target, Symbol symbol);

    /**
     * Get the set of states of the automaton
     * @return the set of states
     */
    Set<State> getStates();

    /**
     * Get the set of non-final states of the automaton
     * @return the set of non-final states
     */
    Set<State> getNonFinalStates();

    /**
     * Check if the automaton contains a state
     * @param state the state to check
     * @return true if the state is in the automaton
     */
    boolean containsState(State state);

    /**
     * Return whether the given state is final
     * @param state a State object
     * @return true if the state is final, false otherwise
     */
    boolean isFinal(State state);

    /**
     * Get all outgoing transitions from a state
     * @param state the source state
     * @return set of pairs containing the symbol and target state for each transition
     */
    Set<Pair<Symbol, State>> outgoingTransitions(State state);

    /**
     * Evaluate a string with the automaton
     * @param string a list of symbols to consume
     * @return true if the string is accepted by the automaton
     */
    boolean evaluate(List<Symbol> string);

    /**
     * Complete the automaton
     * For each state and for each symbol, if there isn't an outgoing transition from state through symbol,
     * add one with 'error' as the target.
     * 'error' will be defined such that when an evaluation falls there, it will be non-successful
     * @param error a non-null object that will act as the 'error' state
     * @throws IllegalArgumentException if the given error state is part of the automaton
     */
    void complete(State error);
}
