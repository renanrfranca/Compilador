package ast;

public class TypeString extends Type {
    
    public TypeString() {
        super("String");
    }

    @Override
    public boolean isCompatible(Type other) {
        if (other.getName().equals("String") || other.getName().equals("NullType")){
            return true;
        }
        return false;
    }
}