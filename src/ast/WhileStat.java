package ast;

public class WhileStat extends Statement {
    private Expr whileExpression;
    private StatementList statementList;

    public WhileStat(Expr whileExpression) {
        this.whileExpression = whileExpression;
    }

    public void setStatementList(StatementList statementList) {
        this.statementList = statementList;
    }
}
