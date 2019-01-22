package ast;

public class AssertStat extends Statement {
    private int lineNumber;
    private Expr expr;
    private LiteralString string;

    public AssertStat(Expr expr, LiteralString string, int lineNumber) {
        this.expr = expr;
        this.string = string;
        this.lineNumber = lineNumber;
    }
}
