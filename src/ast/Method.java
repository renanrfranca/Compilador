package ast;

import java.util.ArrayList;

public class Method extends Member {
    private Qualifiers qualifiers;
    private String name;
    private Type returnType;
    private FieldList params;


    public Method(String name) {
        super(name);
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
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

    public FieldList getParamList(){
        return params;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public Qualifiers getQualifiers() {
        return qualifiers;
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
}
