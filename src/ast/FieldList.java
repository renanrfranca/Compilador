package ast;

import java.util.ArrayList;

public class FieldList {
    private ArrayList<Field> fieldList;

    public FieldList() {
        fieldList = new ArrayList<>();
    }

    public void addField(Field f){
        fieldList.add(f);
    }

    public Field getField(String name){
        int i;

        for (i=0;i<fieldList.size();i++){
            if (fieldList.get(i).getName().equals(name)){
                return fieldList.get(i);
            }
        }

        return null;
    }

    public Field getField(int i){
        return fieldList.get(i);
    }

    public int size(){
        return fieldList.size();
    }

    public boolean isSameAs(FieldList fl){
        if (fieldList.size() != fl.size()){
            return false;
        }

        int i;

        for (i=0;i<fieldList.size();i++){
            if (fl.getField(i).getType() != fieldList.get(i).getType()){
                return false;
            }
        }

        return true;
    }
}
