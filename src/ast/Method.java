/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class Method extends Member {
    private Qualifiers qualifiers;
    private Type returnType = Type.nullType;
    private FieldList params;
    private StatementList statementList;


    public Method(String name) {
        super(name);
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setQualifiers(Qualifiers qualifiers) {
        this.qualifiers = qualifiers;
    }

    public void addParam(Field f){
        params.addField(f);
    }

    public Field getParam(String name){
        return params.getField(name);
    }

    public void setParams(FieldList params){
        this.params = params;
    }

    public FieldList getParamList(){
        return params;
    }

    public Qualifiers getQualifiers() {
        return qualifiers;
    }

    public void setStatementList(StatementList sl){
        this.statementList = sl;
    }

    @Override
    public Type getType() {
        return returnType;
    }

    /*  Possible returns
            -1: Incorrect number of parameters
             0: Parameters are correct
          1..n: Position of the incompatible parameter
     */
    public int checkParams(ExpressionList exprList){
        if (exprList.size() != this.params.size())
            return -1;

        int i;
        Field f;
        Expr e;

        for (i=0;i<params.size();i++){
            f = params.getField(i);
            e = exprList.getElement(i);

            if (! f.getType().isCompatible(e.getType())){
                return i + 1;
            }
        }
        return 0;
    }

    public boolean hasSameSignature(Method other){
        if (this.returnType != other.getType()){
            return false;
        }

        if (! this.getParamList().isSameAs(other.getParamList())){
            return false;
        }

        return true;
    }
}
