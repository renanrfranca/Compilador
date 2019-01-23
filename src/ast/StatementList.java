/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

import java.util.ArrayList;

public class StatementList {
    private ArrayList<Statement> statementList;

    public StatementList() {
        this.statementList = new ArrayList<>();
    }

    public void addElement(Statement s){
        this.statementList.add(s);
    }

    public Statement getElement(int position){
        return this.statementList.get(position);
    }

    public int size(){
        return this.statementList.size();
    }
}
