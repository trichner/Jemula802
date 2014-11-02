package util;

import java.util.List;

/**
 * Created by trichner on 10/27/14.
 */
public class Minions {
    public static int subtract(List<Integer> list,int i1,int i2){
        int max = list.size();
        if(!inRange(max,i1,i2)){
            throw new IndexOutOfBoundsException();
        }
        return list.get(i1)-list.get(i2);
    }

    public static int subtract(List<Integer> list){
        return subtract(list,list.size()-1,list.size()-2);
    }

    public static int diff(List<Integer> list){
        int last = subtract(list);
        int second = subtract(list,list.size()-2,list.size()-3);
        return last-second;
    }

    public static boolean inRange(int max,int... x){
        for(int i : x){
            if(!(i>=0 && i<max)){
                return false;
            }
        }
        return true;
    }
}
