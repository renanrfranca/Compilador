package ast;

import java.util.ArrayList;

public class MemberList {
    public ArrayList<Member> list;

    public MemberList() {
        list = new ArrayList<>();
    }

    public void addMember(Member m){
        list.add(m);
    }

    public Member getMember(String name){
        int i;
        int len = list.size();

        for (i=0;i<len;i++){
            if (list.get(i).getName().equals(name)){
                return list.get(i);
            }
        }
        
        return null;
    }
}
