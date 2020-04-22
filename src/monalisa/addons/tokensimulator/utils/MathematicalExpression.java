/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A mathematical expression (equation) which can be solved. An expression is
 * parsed from a string. It can contain several case differentiations, separated
 * by ";". Each case can be either a direct instruction, such as "A + B / C", or
 * represent a logical condition in the form "if A &lt= 3 then B + C".
 *
 * @author Pavel Balazki.
 */
public final class MathematicalExpression {

    //BEGIN VARIABLES DECLARATION
    /**
     * Constant name for variable Pi.
     */
    public static final String PI = "pi";
    /**
     * String which is used for variable "time".
     */
    public static final String TIME_VAR = "Time";
    private final Lock lock = new ReentrantLock();
    /**
     * If the expression has no variables, it is constant.
     */
    private boolean isConstant = false;
    /**
     * If an expression is constant, its value can be calculated upon creating.
     */
    private double constantValue = 0;
    /*
     * Expression with all sub-expressions (cases)
     */
    private String wholeExp = "";
    private final ArrayList<ExpressionML> expressions = new ArrayList<>();
    /**
     * Keys are names of variables, values are IDs of places.
     */
    private Map<String, Integer> variables = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger(MathematicalExpression.class);
    //END VARIABLES DECLARATION

    //BEGIN CONSTRUCTORS
    private MathematicalExpression() {
    }

    public MathematicalExpression(String exp, Map<String, Integer> variables) throws RuntimeException {
        this.wholeExp = exp;
        this.variables = new HashMap<>(variables);
        /*
         * Expression cases are separated by a ';'. Iterate through all cases and create for each an expression object.
         */
        for (String expression : exp.split(";")) {
            expressions.add(new ExpressionML(expression.trim(), variables));
        }
        /*
        If the variables-map is empty and the expression does not depend on time, its value can be pre-evaluated.
         */
        if (variables.isEmpty() && !exp.contains(TIME_VAR)) {
            this.constantValue = this.evaluateML(new HashMap<Integer, Double>(), 0);
            this.isConstant = true;
        }
    }

    public MathematicalExpression(String exp) throws RuntimeException {
        this(exp.trim(), new HashMap<String, Integer>());
    }

    public MathematicalExpression(MathematicalExpression exp) throws RuntimeException {
        this.wholeExp = exp.wholeExp;
        this.variables = new HashMap<>(exp.variables);

        for (ExpressionML expr : exp.expressions) {
            expressions.add(expr.copy());
        }
        if (exp.isConstant()) {
            this.isConstant = true;
            this.constantValue = exp.constantValue;
        }
    }

    /**
     * Evaluate the expression. If it has more than one cases, the first case
     * which is valid will be evaluated. If no valid case exists, 0 is returned.
     *
     * @param concentrations A map of place IDs and number of tokens on them or
     * concentrations of respective compounds.
     * @param time Simulation time.
     * @return Normally returns concentration value in M.
     */
    public double evaluateML(Map<Integer, Double> concentrations, double time) {
        /*
        If the expression does not depend on variables, return its pre-calculated value.
         */
        if (this.isConstant) {
            return this.constantValue;
        }
        for (ExpressionML exp : this.expressions) {
            try {
                /*
                 * If expressions condition holds, evaluate the expression and return its value.
                 */
                if (exp.conditionHolds(concentrations, time)) {
                    return exp.evaluateML(concentrations, time);
                }
            } catch (RuntimeException ex) {
                LOGGER.error("Error during evaluting mathematical expression", ex);
                return 0;
            }
        }
        return 0;
    }

    public Map<String, Integer> getVariables() {
        lock.lock();
        try {
            return this.variables;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Copies the content of vars to a new map.
     *
     * @param vars
     */
    public void setVariables(Map<String, Integer> vars) {
        lock.lock();
        try {
            /*
            If the variables-map is empty and the expression does not depend on time, its value can be pre-evaluated.
             */
            if (vars.isEmpty() && !this.wholeExp.contains(TIME_VAR)) {
                this.constantValue = this.evaluateML(new HashMap<Integer, Double>(), 0);
                this.isConstant = true;
            }
            if (!vars.isEmpty()) {
                this.isConstant = false;
            }
            this.variables = new HashMap<>(vars);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return this.wholeExp;
    }

    /**
     * Returns true if this expression is constant, that means it does not
     * depend on variables or time. Its value is calculated only once and must
     * not be re-calculated.
     *
     * @return
     */
    public boolean isConstant() {
        return this.isConstant;
    }
}
