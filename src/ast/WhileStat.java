/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class WhileStat extends Statement {
    private Expr whileExpression;
    private StatementList statementList;

    public WhileStat(Expr whileExpression) {
        this.whileExpression = whileExpression;
    }

    public void setStatementList(StatementList statementList) {
        this.statementList = statementList;
    }
}
