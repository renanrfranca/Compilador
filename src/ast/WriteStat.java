/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class WriteStat extends Statement {
    private Expr expression;
    private boolean ln;

    public WriteStat(Expr expression, boolean ln) {
        this.expression = expression;
        this.ln = ln;
    }
}
