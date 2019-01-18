package comp;
import java.util.*;
import ast.*;

public class SymbolTable {

    private HashMap<String, CianetoClass> globalTable;
    private HashMap<String, Member> localTable;
    private HashMap<String, Field> funcTable;


    public SymbolTable(){
        globalTable = new HashMap<String, CianetoClass>();//guarda apenas Classe
        localTable = new HashMap<String, Member>(); //quandao tudo dentro da Classe, exceto dentro da funcao
        funcTable = new HashMap<String, Field>(); //guarda tudo dentro da funcao

    }

    //evelin - metodos de gets
    public CianetoClass getInGlobal (String key){
        return globalTable.get(key);
    }

    public Member getInLocal (String key){
        return localTable.get(key);
    }

    public Field getInFunc (String key){
        return funcTable.get(key);
    }

    //evelin - metodos de sets/put
    public Object putInGlobal(String key, CianetoClass value){
        return globalTable.put(key,value);
    }

    public Member putInlocal(String key, Member value){
        return localTable.put(key,value);
    }

    public Field putInFunc(String name, Field value){
        return funcTable.put(name, value)
    }

    //remover a localTable
    public void removeLocalTable(){
        localTable.clear();
    }

    //remover a funcTable
    public void removeFuncTable(){
        funcTable.clear();
    }

}