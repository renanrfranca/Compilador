package ast;

public class ObjectCreation extends Expr {
    CianetoClass ciaClass;

    public ObjectCreation(CianetoClass ciaClass) {
        this.ciaClass = ciaClass;
    }

    @Override
    public Type getType() {
        return this.ciaClass;
    }
}
