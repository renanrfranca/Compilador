package ast;
/*
 * Open Class
 */
public class CianetoClass extends Type {
   private String className;
   private CianetoClass superClass;
   private MemberList memberList;
   private FieldList fieldList;
   private MethodList publicMethodList;
   private MethodList privateMethodList;

   private boolean open = false;

   public CianetoClass(String name) {
      super("CiaClass");
      this.className = name;
      memberList = new MemberList();
      fieldList = new FieldList();
      publicMethodList = new MethodList();
      privateMethodList = new MethodList();
   }

   @Override
   public boolean isCompatible(Type other) {
      CianetoClass ciaClass;

      if (other.getName().equals("CiaClass")){
         ciaClass = (CianetoClass) other;

         if (ciaClass.className.equals(this.className)){
            return true;
         } else {
            if (ciaClass.superClass != null) {
               return this.isCompatible(ciaClass.superClass);
            }
         }
      }

      if (other == Type.nullType){
         return true;
      }

      return false;
   }

   public String getClassName() {
      return className;
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

   public void addMethod(Method m, boolean isPrivate){
      if (isPrivate)
         privateMethodList.addMethod(m);
      else
         publicMethodList.addMethod(m);
   }

   public Method getMethod(String name){
      Method m;

      m = getPublicMethod(name);

      if (m == null)
         m = this.privateMethodList.getMethod(name);

      if (m == null) {
         if (superClass != null)
            m = superClass.getMethod(name);
      }

      return m;
   }

   public Method getPublicMethod(String name){
      Method m;

      m = this.publicMethodList.getMethod(name);

      if (m == null){
         if (superClass != null)
            m = superClass.getPublicMethod(name);
      }

      return m;
   }

   public void addField(Field f){
      this.fieldList.addField(f);
   }

   public Field getField(String name){
      return this.fieldList.getField(name);
   }

   public Member getMember(String name){
      Member m = this.getMethod(name);
      if (m == null)
         m = this.getField(name);
      return m;
   }

}
