package ast;

public class ReadExpr extends Expr {
    Type type;

    public ReadExpr(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }
}
