/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

public class MessageSend {
    ExpressionList parameters;
    String messageName;

    public MessageSend(ExpressionList parameters, String messageName) {
        this.parameters = parameters;
        this.messageName = messageName;
    }
}
