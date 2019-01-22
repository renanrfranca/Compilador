package ast;

public class ReturnStat extends Statement {
    private Expr returnExpr;

    public ReturnStat(Expr returnExpr) {
        this.returnExpr = returnExpr;
    }

    public Type getType(){
        return returnExpr.getType();
    }
}
