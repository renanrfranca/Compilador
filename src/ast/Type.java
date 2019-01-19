package ast;

abstract public class Type {

    public Type( String name ) {
        this.name = name;
    }

    public static Type booleanType = new TypeBoolean();
    public static Type intType = new TypeInt();
    public static Type stringType = new TypeString();
    public static Type nullType = new TypeNull();

    public String getName() {
        return name;
    }

    public abstract boolean isCompatible (Type other);
//    {
//        if (this == booleanType)
//            return other == booleanType;
//        else if (this == intType)
//            return other == intType;
//        else if (this == stringType)
//            return other == stringType;
//        else if (this == nullType)
//            return false; // pois nao deve ter retorno
//            //else if (this instanceof KraClass) // tem q ser classe ou subclasse
//            //return this == other ((KraClass) this).isSubClassOf(other);
//        else {
//            return false;
//        }
//    }

    private String name;
}
