/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class NullExpr extends Expr {

   public Type getType() {
      //# corrija
      return Type.nullType;
   }
}