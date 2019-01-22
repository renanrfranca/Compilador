package ast;

public class MethodReturn extends Expr {
    private Type returnType;

    public MethodReturn(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public Type getType() {
        return returnType;
    }
}
