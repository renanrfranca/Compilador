/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class LocalDecStat extends Statement {
    private FieldList fieldList;

    public LocalDecStat(FieldList fieldList) {
        this.fieldList = fieldList;
    }
}
