/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.utils;

import java.util.ArrayList;
import java.util.Map;
import static monalisa.addons.tokensimulator.utils.MathematicalExpression.TIME_VAR;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExpressionML {

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

    Map<String, Integer> variables;

    private final static Logger LOGGER = LogManager.getLogger(ExpressionML.class);

    private Function int_div = new Function("int_div", 2) {
        @Override
        public double apply(double... arg0) {
            long a1 = Math.round(arg0[0]);
            long a2 = Math.round(arg0[1]);
            return a1 / a2;
        }
    };

    public ExpressionML(String exp, Map<String, Integer> variables) throws RuntimeException {
        expression = exp;
        this.variables = variables;
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
                tmpConditions.add(new Condition(conditionPart, int_div, variables));
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
        return new ExpressionML(expression, variables);
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
            for (Map.Entry<String, Integer> entry : variables.entrySet()) {
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
            for (Map.Entry<String, Integer> entry : variables.entrySet()) {
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
        for (Map.Entry<String, Integer> entr : variables.entrySet()) {
            expB.variable(entr.getKey());
        }
        expB.variable(TIME_VAR);

        Expression calcExp = expB.build();

        for (Map.Entry<String, Integer> entr : variables.entrySet()) {
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
