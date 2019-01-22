package ast;

public class LocalDecStat extends Statement {
    private FieldList fieldList;

    public LocalDecStat(FieldList fieldList) {
        this.fieldList = fieldList;
    }
}
