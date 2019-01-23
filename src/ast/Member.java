/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

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