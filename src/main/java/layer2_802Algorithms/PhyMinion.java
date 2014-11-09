package layer2_802Algorithms;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 02.11.2014.
 *
 * @author Thomas
 */
public class PhyMinion {
    private static List<String> phymodes = Arrays.asList("BPSK12","BPSK34","QPSK12","QPSK34","16QAM12","16QAM34","64QAM23" ,"64QAM34");
    private static List<Integer> speed = Arrays.asList(6,9,12,18,24,36,48,54);

    public static String increase(String phy){
        return offset(phy,1);
    }

    public static String decrease(String phy){
        return offset(phy,-1);
    }
    public static String offset(String phy,int offset){
        if(!phymodes.contains(phy)){
            throw new IllegalArgumentException();
        }
        int i = phymodes.indexOf(phy);
        return phymodes.get(bounded(i+offset));
    }
    public static int getIndex(String phy){
        return phymodes.indexOf(phy);
    }
    public static int getSpeed(String phy){
        return speed.get(getIndex(phy));
    }
    public static String max(){
        return phymodes.get(phymodes.size()-1);
    }

    private static int bounded(int i){
        if(i<0) return  0;
        if(i>=phymodes.size()) return phymodes.size()-1;
        return i;
    }

    public static String min() {
        return phymodes.get(0);
    }
}
