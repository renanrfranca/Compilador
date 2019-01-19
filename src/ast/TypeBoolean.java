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
