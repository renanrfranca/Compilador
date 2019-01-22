package ast;

public class ObjectReturn extends Expr {
    private Type objectClass;

    public ObjectReturn(Type objectClass) {
        this.objectClass = objectClass;
    }

    @Override
    public Type getType() {
        return objectClass;
    }
}
