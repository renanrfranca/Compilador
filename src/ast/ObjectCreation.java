/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class ObjectCreation extends Expr {
    CianetoClass ciaClass;

    public ObjectCreation(CianetoClass ciaClass) {
        this.ciaClass = ciaClass;
    }

    @Override
    public Type getType() {
        return this.ciaClass;
    }
}
