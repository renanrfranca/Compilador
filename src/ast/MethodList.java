/********* Compilador de Cianeto *********
 *Evelin Priscila Ferreira Soares 489832 *
 *Renan Rossignatti de França     489697 *
 *****************************************/

package ast;

import java.util.ArrayList;

public class MethodList {
    private ArrayList<Method> methodList;

    public MethodList() {
        methodList = new ArrayList<>();
    }

    public void addMethod(Method m){
        methodList.add(m);
    }

    public Method getMethod(String name){
        int i;

        for (i=0;i<methodList.size();i++){
            if (methodList.get(i).getName().equals(name)){
                return methodList.get(i);
            }
        }

        return null;
    }
}
