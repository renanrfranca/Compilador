/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class MethodReturn extends Expr {
    private Type returnType;

    public MethodReturn(Type returnType) {
        this.returnType = returnType;
    }

    @Override
    public Type getType() {
        return returnType;
    }
}
