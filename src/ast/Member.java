package ast;

abstract public class Member {
    String name;

    public Member(String name){
        this.name = name;
    }

    abstract public String getName();
}