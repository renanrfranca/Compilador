/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class ReturnStat extends Statement {
    private Expr returnExpr;

    public ReturnStat(Expr returnExpr) {
        this.returnExpr = returnExpr;
    }

    public Type getType(){
        return returnExpr.getType();
    }
}
