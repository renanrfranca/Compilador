/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class Qualifiers {
    private boolean qPrivate = false;
    private boolean qPublic = true;
    private boolean qOverride = false;
    private boolean qFinal = false;

    public boolean unchanged = true;

    public void setPrivate(){
        this.qPublic = false;
        this.qPrivate = true;
        this.unchanged = false;
    }

    public void setFinal() {
        this.qFinal = true;
        this.unchanged = false;
    }

    public void setOverride() {
        this.qOverride = true;
        this.unchanged = false;
    }

    public boolean isPrivate() {
        return qPrivate;
    }

    public boolean isPublic() {
        return qPublic;
    }

    public boolean isOverride() {
        return qOverride;
    }

    public boolean isFinal() {
        return qFinal;
    }
}
