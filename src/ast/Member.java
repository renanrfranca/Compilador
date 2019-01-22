package ast;

abstract public class Member extends Expr {
    String name;

    public Member(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }
}