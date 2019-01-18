package ast;
/*
 * Open Class
 */
public class CianetoClass extends Type {
   private String name;
   private CianetoClass superclass;
   private MemberList memberList;
   private boolean open = false;

   public CianetoClass(String name ) {
      super(name);
      memberList = new MemberList();
   }

   public void setSuperclass(CianetoClass superclass) {
      this.superclass = superclass;
   }

   public CianetoClass getSuperclass() {
      return superclass;
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
