package ast;

import java.util.ArrayList;

public class Method extends Member {
    private Qualifiers qualifiers;
    private String name;
    private Type returnType;

    public Method(String name) {
        super(name);
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public void setQualifiers(Qualifiers qualifiers) {
        this.qualifiers = qualifiers;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Qualifiers getQualifiers() {
        return qualifiers;
    }

    public Type getReturnType() {
        return returnType;
    }
}
