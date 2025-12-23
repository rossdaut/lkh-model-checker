package lkh.modelchecker;

import lkh.expression.Expression;
import lombok.NonNull;

import java.util.Iterator;
import java.util.List;

/**
 * Interface for model checking systems against lkh expressions.
 * This interface defines operations for verifying if a system satisfies
 * specified logical properties and finding witness plans that demonstrate
 * the satisfaction of knowing-how (KH) properties.
 *
 * @param <State> The type representing states in the system
 * @param <Action> The type representing actions in the system
 */
public interface ModelChecker<State, Action> {
    /**
     * Check if the system M satisfies the given expression expr at its pointed state s. ((M, s) |= expr)
     *
     * @param expr a non-null logic expression
     * @return true if the system satisfies the expression, false otherwise
     */
    boolean check(@NonNull Expression expr);

    /**
     * Check if the system satisfies the expression at the specified state.
     *
     * @param expr a non-null logic expression
     * @param state the state to check
     * @return true if the state satisfies the expression, false otherwise
     */
    boolean check(@NonNull Expression expr, State state);

    /**
     * Return the plans that witness a knowledge-how property kh(initExpr, endExpr)
     * with length at most lengthLimit.
     * 
     * @param initExpression the expression that source states must satisfy
     * @param endExpression the expression that end states must satisfy
     * @param lengthLimit the maximum plan length
     * @return an iterator over the witness plans
     */
    Iterator<List<Action>> witnesses(Expression initExpression, Expression endExpression, int lengthLimit);

    /**
     * Set whether automata should be minimized during computation.
     * 
     * @param minimize true to enable minimization, false otherwise
     */
    void setMinimize(boolean minimize);

    /**
     * Get the current minimization setting.
     * 
     * @return true if minimization is enabled, false otherwise
     */
    boolean isMinimize();
}
