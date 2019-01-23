/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class ReadExpr extends Expr {
    Type type;

    public ReadExpr(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }
}
