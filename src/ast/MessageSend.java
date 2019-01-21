package ast;

public class MessageSend {
    ExpressionList parameters;
    String messageName;

    public MessageSend(ExpressionList parameters, String messageName) {
        this.parameters = parameters;
        this.messageName = messageName;
    }
}
