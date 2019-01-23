/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class TypeInt extends Type {
    
    public TypeInt() {
        super("int");
    }

    @Override
    public boolean isCompatible(Type other) {
        if (other.getName().equals("int")){
            return true;
        }

        return false;
    }

}