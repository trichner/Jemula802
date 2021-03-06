package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by trichner on 10/27/14.
 */
public class AggregateIntTracker {
    private List<Integer> list = new ArrayList<>(Collections.singletonList(0));
    private int last = 0;

    public void pushAggregated(int i){
        list.add(i-last);
        last = i;
    }

    public void push(int i){
        list.add(i);
    }

    public List<Integer> getList(){
        return Collections.unmodifiableList(list);
    }

    public int getLast(){
        return list.get(list.size()-1);
    }

    public int get(int i){
        if(Math.abs(i)>=list.size()){
            throw new IndexOutOfBoundsException();
        }
        int ret;
        if(i<0){
            ret = list.get(list.size()+i);
        }else {
            ret = list.get(i);
        }
        return ret;
    }

    public int getLast(int ref){
        return getLast()-ref;
    }

    public int diffLast(){
        return getLast() - list.get(list.size()-2);
    }

    public int intLast(int window,int ref){
        int min = Math.min(window,list.size()-1);
        int integrated = 0;
        List<Integer> sublist = list.subList(list.size()-min,list.size());
        for(int i : sublist){
            integrated += i-ref;
        }
        return integrated;
    }

    public int intLast(int window){
        return intLast(window,0);
    }
}
