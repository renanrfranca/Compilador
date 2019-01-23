/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

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

    public abstract boolean isCompatible(Type other);

    public boolean isPrintable(){
        if (name.equals("String") || name.equals("int")){
            return true;
        }
        return false;
    }

    private String name;
}
