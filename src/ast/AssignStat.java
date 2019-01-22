package ast;

public class AssignStat extends Statement {
    private Expr left;
    private boolean hasAssign;
    private Expr right;

    public AssignStat(Expr left, boolean hasAssign, Expr right) {
        this.left = left;
        this.hasAssign = hasAssign;
        this.right = right;
    }
}
