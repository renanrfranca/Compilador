package ast;
/*
 * Open Class
 */
public class CianetoClass extends Type {
   private String className;
   private CianetoClass superClass;
   private MemberList memberList;
   private boolean open = false;

   public CianetoClass(String name) {
      super("CiaClass");
      this.className = name;
      memberList = new MemberList();
   }

   @Override
   public boolean isCompatible(Type other) {
      CianetoClass ciaClass;

      if (other.getName().equals("CiaClass")){
         ciaClass = (CianetoClass) other;

         if (ciaClass.className.equals(this.className)){
            return true;
         } else {
            if (this.superClass != null) {
               return this.superClass.isCompatible(other);
            }
         }
      }
      return false;
   }

   public void setSuperClass(CianetoClass superClass) {
      this.superClass = superClass;
   }

   public CianetoClass getSuperClass() {
      return superClass;
   }

   public boolean isOpen() {
      return open;
   }

   public void setOpen(boolean open) {
      this.open = open;
   }

   public void setMemberList(MemberList memberList) {
      this.memberList = memberList;
   }
// private FieldList fieldList;
   // private MethodList publicMethodList, privateMethodList;
   // m�todos p�blicos get e set para obter e iniciar as vari�veis acima,
   // entre outros m�todos
}
