/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class RepeatStat extends Statement {
    private StatementList statementList;
    private Expr repeatExpression;

    public RepeatStat(StatementList statementList, Expr repeatExpression) {
        this.statementList = statementList;
        this.repeatExpression = repeatExpression;
    }
}
