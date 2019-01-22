package ast;

public class WriteStat extends Statement {
    private Expr expression;
    private boolean ln;

    public WriteStat(Expr expression, boolean ln) {
        this.expression = expression;
        this.ln = ln;
    }
}
