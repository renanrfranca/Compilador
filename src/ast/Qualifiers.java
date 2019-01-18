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
    }

    public void setFinal() {
        this.qFinal = true;
    }

    public void setOverride() {
        this.qOverride = true;
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
