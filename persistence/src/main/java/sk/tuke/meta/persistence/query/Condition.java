package sk.tuke.meta.persistence.query;

import data.WhereOperator;

public class Condition {
    private String operand1;
    private String operand2;
    private WhereOperator operator;

    public Condition(String operand1, String operand2, WhereOperator operator) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    public String operatorToString() {
        switch (this.operator) {
            case equals -> {
                return "=";
            }
            case lessThan -> {
                return "<";
            }
            case biggerThan -> {
                return ">";
            }
        }
        return "";
    }

    public String getOperand1() {
        return operand1;
    }

    public void setOperand1(String operand1) {
        this.operand1 = operand1;
    }

    public String getOperand2() {
        return operand2;
    }

    public void setOperand2(String operand2) {
        this.operand2 = operand2;
    }

    public WhereOperator getOperator() {
        return operator;
    }

    public void setOperator(WhereOperator operator) {
        this.operator = operator;
    }
}
