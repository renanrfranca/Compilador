package ast;

public class IfStat extends Statement{
    Expr ifExpr;
    StatementList ifList;
    StatementList elseList;

    public IfStat(Expr ifExpr) {
        this.ifExpr = ifExpr;
    }

    public void setIfList(StatementList ifList) {
        this.ifList = ifList;
    }

    public void setElseList(StatementList elseList) {
        this.elseList = elseList;
    }
}
