package layer2_802Algorithms.controller;

import layer2_802Algorithms.PhyMinion;
import layer2_802Algorithms.RRMConfig;
import layer2_802Algorithms.RRMInput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created on 10.11.2014.
 *
 * @author Thomas
 */
public class StepResponse {
    public static void main(String[] args) {
        RRMController controller = new STAPhyController();

        File trace = new File("trace.txt");
        try {
            FileWriter writer = new FileWriter(trace);
            writer.write("#STEP, Collisions,QueueSize,TX Power,Phymode, Speed\n");

            double txPower = 800;
            String phymode = "BPSK12";

            int collisions = 0;
            int queueSize = 0;

            int step=0;
            for(; step<100;step++){
                RRMInput input = new RRMInput(0,0,collisions,0,0,queueSize,txPower, phymode);

                RRMConfig output = controller.compute(input);
                txPower = output.getTxPower();
                phymode = output.getPhymode();
                writer.write("" + step + ',' + collisions + ',' + queueSize + ',' + txPower + ","
                 + phymode + "," +  PhyMinion.getSpeed(phymode)  + '\n');
            }

            queueSize = 100;
            for(; step<1000;step++){
                //collisions+=100;
                RRMInput input = new RRMInput(0,0,collisions,0,0,queueSize,txPower, phymode);
                RRMConfig output = controller.compute(input);
                txPower = output.getTxPower();
                phymode = output.getPhymode();
                writer.write("" + step + ',' + collisions + ',' + queueSize + ',' + txPower + ","
                        + phymode + "," +  PhyMinion.getSpeed(phymode)  + '\n');
            }

            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
