/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class TypeBoolean extends Type {

   public TypeBoolean() { super("boolean"); }

   @Override
   public boolean isCompatible(Type other) {
      if (other.getName().equals("boolean")){
         return true;
      }

      return false;
   }
}
