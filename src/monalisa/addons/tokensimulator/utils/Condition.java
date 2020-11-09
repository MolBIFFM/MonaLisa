/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package monalisa.addons.tokensimulator.utils;

import java.util.Map;
import static monalisa.addons.tokensimulator.utils.MathematicalExpression.TIME_VAR;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class which represents a single condition of the expression.
 */
public class Condition {

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

    private final static Logger LOGGER = LogManager.getLogger(Condition.class);

    public Condition(String condition, Function int_div, Map<String, Integer> variables) {
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
