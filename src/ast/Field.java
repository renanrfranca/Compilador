package ast;

public class Field extends Member {
    private Qualifiers qualifiers;
    private String name;
    private Type type;

    public Field(String name, Type type) {
        super(name);
        this.type = type;
    }

    public String getName() {
        return name;
    }

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
