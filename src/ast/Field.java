package ast;

public class Field extends Member {
    private Qualifiers qualifiers;
    private Type type;

    public Field(String name, Type type) {
        super(name);
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setQualifiers(Qualifiers q) {
        this.qualifiers = qualifiers;
    }

    public Qualifiers getQualifiers() {
        return qualifiers;
    }
}
