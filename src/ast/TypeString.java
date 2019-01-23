/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

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