/*
 *
 *  This file is part of the software MonaLisa.
 *  MonaLisa is free software, dependent on non-free software. For more information read LICENCE and README.
 *
 *  (c) Department of Molecular Bioinformatics, Institute of Computer Science, Johann Wolfgang
 *  Goethe-University Frankfurt am Main, Germany
 *
 */
package monalisa.addons.tokensimulator;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
    private static final String PI = "pi";
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

    //BEGIN INNER CLASSES
    /**
     * Function of integer division.
     */
    private Function int_div = new Function("int_div", 2) {
        @Override
        public double apply(double... arg0) {
            long a1 = Math.round(arg0[0]);
            long a2 = Math.round(arg0[1]);
            return a1 / a2;
        }
    };

    private class ExpressionML {

        /**
         * ExpressionBuilder is used to create and parse an expression.
         */
        ExpressionBuilder expB;
        Expression calcExp;
        /**
         * String representation of expression.
         */
        String expression;
        /**
         * Condition of the expression (string representation).
         */
        String conditionStr;
        /**
         * Variable which indicates whether the expression has a condition or
         * not. True only if the string "condition" is not empty.
         */
        boolean hasCondition;
        /**
         * Array of conditions for this expression.
         */
        Condition[] conditions;
        /**
         * Instruction
         */
        String instruction;

        /**
         * A class which represents a single condition of the expression.
         */
        private class Condition {

            /**
             * Operator '&lt'
             */
            final static short OP_LESS = 0;
            /**
             * Operator '&lt='
             */
            final static short OP_LESS_EQ = 1;
            /**
             * Operator '='
             */
            final static short OP_EQ = 2;
            /**
             * Operator '&gt'
             */
            final static short OP_GREAT = 3;
            /**
             * Operator '&gt='
             */
            final static short OP_GREAT_EQ = 4;

            private Expression leftPart = null, rightPart = null;

            final private short operator;

            public Condition(String condition) {
                String opString = "";
                if (condition.matches("(.+)<(.+)")) {
                    if (condition.matches("(.+)<=(.+)")) {
                        opString = "<=";
                        operator = OP_LESS_EQ;
                    } else {
                        opString = "<";
                        operator = OP_LESS;
                    }
                } else if (condition.matches("(.+)>(.+)")) {
                    if (condition.matches("(.+)>=(.+)")) {
                        opString = ">=";
                        operator = OP_GREAT_EQ;
                    } else {
                        opString = ">";
                        operator = OP_GREAT;
                    }
                } else if (condition.matches("(.+)=(.+)")) {
                    opString = "=";
                    operator = OP_EQ;
                } else {
                    operator = -1;
                }
                try {
                    ExpressionBuilder expBLeft = new ExpressionBuilder(condition.split(opString)[0])
                            .variable(MathematicalExpression.PI)
                            .function(int_div).variable(TIME_VAR);

                    for (String v : variables.keySet()) {
                        expBLeft.variable(v);
                    }

                    Expression leftPart = expBLeft.build();
                    leftPart.setVariable(MathematicalExpression.PI, Math.PI);
                    leftPart.setVariable(TIME_VAR, 0);

                    for (String v : variables.keySet()) {
                        leftPart.setVariable(v, 0);
                    }

                } catch (RuntimeException exLeft) {
                    LOGGER.error("Unknown Function or Unparseable Expression found while trying to parse the first part of a mathematical expression", exLeft);
                }
                try {
                    ExpressionBuilder expBRight = new ExpressionBuilder(condition.split(opString)[0])
                            .variable(MathematicalExpression.PI)
                            .function(int_div).variable(TIME_VAR);

                    for (String v : variables.keySet()) {
                        expBRight.variable(v);
                    }

                    Expression rightPart = expBRight.build();
                    rightPart.setVariable(MathematicalExpression.PI, Math.PI);
                    rightPart.setVariable(TIME_VAR, 0);

                    for (String v : variables.keySet()) {
                        rightPart.setVariable(v, 0);
                    }

                } catch (RuntimeException exRight) {
                    LOGGER.error("Unknown Function or Unparseable Expression found while trying to parse the second part of a mathematical expression", exRight);
                }

            }

            /**
             * Get the calculable of the part left to the operator.
             *
             * @return
             */
            public Expression getLeftCalculable() {
                return this.leftPart;
            }

            /**
             * Get the calculable of the part right to the operator.
             *
             * @return
             */
            public Expression getRightCalculable() {
                return this.rightPart;
            }

            public short getOperator() {
                return this.operator;
            }
        }

        ExpressionML(String exp) throws RuntimeException {
            expression = exp;
            /*
             * Check whether the expression is a direct instruction or a "if then" condition.
             */
            if (expression.toLowerCase().matches("(if\\s)(.+)(\\sthen\\s)(.+)")) {
                instruction = expression.substring(expression.lastIndexOf("then") + 4);
                instruction = instruction.replaceAll("\\s+", "");
                conditionStr = expression.substring(expression.indexOf("if") + 2, expression.lastIndexOf("then"));
                conditionStr = conditionStr.replaceAll("\\s+", "");
            } /*
             * If it is a direct instruction, try to create a new calculable with from it, using given variables.
             * Take "0" as a value for each variable. This allows to test whether the expression can be parsed and interpreted by ext4j.
             */ else {
                instruction = expression;
                instruction = instruction.replaceAll("\\s+", "");
                conditionStr = "";
            }
            /*
            Check whether the expression has a condition.
             */
            this.hasCondition = !conditionStr.isEmpty();
            if (this.hasCondition) {
                ArrayList<Condition> tmpConditions = new ArrayList<>();
                for (String conditionPart : conditionStr.split("and")) {
                    tmpConditions.add(new Condition(conditionPart));
                }
                this.conditions = tmpConditions.toArray(new Condition[0]);
            }

            expB = new ExpressionBuilder(instruction);
            /*
             * Assign values for all variables.
             */
            for (String variable : variables.keySet()) {
                expB.variable(variable).build().setVariable(variable, 0);
            }
            expB.variable(TIME_VAR);
            expB.variable(MathematicalExpression.PI);
            try {
                expB.function(int_div);
            } catch (RuntimeException ex) {
                LOGGER.error("Invalid custom function found while trying to build an exception message", ex);
            }
            /*
             * Try to build a calculable.
             */
            calcExp = expB.build();
            calcExp.setVariable(TIME_VAR, 0);
            calcExp.setVariable(MathematicalExpression.PI, Math.PI);
        }

        public ExpressionML copy() throws RuntimeException {
            return new ExpressionML(expression);
        }

        /**
         * Check whether current marking suffices the condition of this
         * expression.
         *
         * @param concentrations Concentrations of variables. Make sure they are
         * in M and not in molecule number!
         * @param time Simulation time. Each mathematical expression can have
         * time as variable.
         * @return true if the condition is given by current marking or no
         * condition exists.
         * @throws UnknownFunctionException
         * @throws UnparsableExpressionException
         */
        public boolean conditionHolds(Map<Integer, Double> concentrations, double time) throws RuntimeException {
            /*
            If the expression has no condition, it holds automatically.
             */
            if (!this.hasCondition) {
                return true;
            }

            /*
            * Check whether all conditions hold.
             */
            for (Condition condition : conditions) {
                double leftPartDouble;
                double rightPartDouble;
                /*
                Calculate the value of the left part.
                 */
                Expression calc = condition.getLeftCalculable();
                /*
                * Assign values for all variables.
                 */
                for (Entry<String, Integer> entry : variables.entrySet()) {
                    double value = concentrations.get(entry.getValue());
                    calc.setVariable(entry.getKey(), value);
                }
                /*
                * Add the value of variable "time".
                 */
                calc.setVariable(TIME_VAR, time);
                leftPartDouble = calc.evaluate();

                /*
                Calculate the value of the right part.
                 */
                calc = condition.getRightCalculable();
                /*
                * Assign values for all variables.
                 */
                for (Entry<String, Integer> entry : variables.entrySet()) {
                    double value = concentrations.get(entry.getValue());
                    calc.setVariable(entry.getKey(), value);
                }
                /*
                * Add the value of variable "time".
                 */
                calc.setVariable(TIME_VAR, time);
                rightPartDouble = calc.evaluate();

                /*
                * Compare resulsts according to the operator.
                 */
                switch (condition.getOperator()) {
                    case Condition.OP_LESS:
                        if (leftPartDouble >= rightPartDouble) {
                            return false;
                        }
                        break;
                    case Condition.OP_LESS_EQ:
                        if (leftPartDouble > rightPartDouble) {
                            return false;
                        }
                        break;
                    case Condition.OP_GREAT:
                        if (leftPartDouble <= rightPartDouble) {
                            return false;
                        }
                        break;
                    case Condition.OP_GREAT_EQ:
                        if (leftPartDouble < rightPartDouble) {
                            return false;
                        }
                        break;
                    case Condition.OP_EQ:
                        if (leftPartDouble != rightPartDouble) {
                            return false;
                        }
                        break;
                }
            }
            return true;
        }

        /**
         * Evaluate the instruction of expression.
         *
         * @param concentrations Concentrations of variables, should be in M and
         * not in molecule numbers!
         * @return
         */
        public double evaluateML(Map<Integer, Double> concentrations, double time) throws RuntimeException {
            /*
            * Assign values for all variables.
             */
            for (Entry<String, Integer> entr : variables.entrySet()) {
                expB.variable(entr.getKey());
            }
            expB.variable(TIME_VAR);

            Expression calcExp = expB.build();

            for (Entry<String, Integer> entr : variables.entrySet()) {
                Double tmp = concentrations.get(entr.getValue());
                double value = tmp == null ? 0 : (double) tmp;
                calcExp.setVariable(entr.getKey(), value);
            }
            /*
            * Assign value for "time" variable
             */
            calcExp.setVariable(TIME_VAR, time);

            double val = calcExp.evaluate();
            return val;
        }

        @Override
        public String toString() {
            return this.expression;
        }
    }
    //END INNER CLASSES

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
            expressions.add(new ExpressionML(expression.trim()));
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
