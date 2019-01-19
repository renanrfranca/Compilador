package ast;

import java.util.ArrayList;

public class ExpressionList {
    private ArrayList<Expr> exprList;

    public ExpressionList() {
        this.exprList = new ArrayList<>();
    }

    public void addElement(Expr e){
        this.exprList.add(e);
    }

    public Expr getElement(int position){
        return this.exprList.get(position);
    }

    public int size(){
        return this.exprList.size();
    }
}
