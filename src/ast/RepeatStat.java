package ast;

public class RepeatStat extends Statement {
    private StatementList statementList;
    private Expr repeatExpression;

    public RepeatStat(StatementList statementList, Expr repeatExpression) {
        this.statementList = statementList;
        this.repeatExpression = repeatExpression;
    }
}
