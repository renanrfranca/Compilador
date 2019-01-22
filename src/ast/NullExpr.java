package ast;

public class NullExpr extends Expr {

   public Type getType() {
      //# corrija
      return Type.nullType;
   }
}