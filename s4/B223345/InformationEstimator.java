package s4.B223345; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;
import s4.specification.*;
import javax.xml.stream.events.StartDocument;

/* What is imported from s4.specification
package s4.specification;
public interface InformationEstimatorInterface {
    void setTarget(byte target[]);  // set the data for computing the information quantities
    void setSpace(byte space[]);  // set data for sample space to computer probability
    double estimation();  // It returns 0.0 when the target is not set or Target's length is zero;
    // It returns Double.MAX_VALUE, when the true value is infinite, or space is not set.
    // The behavior is undefined, if the true value is finete but larger than Double.MAX_VALUE.
    // Note that this happens only when the space is unreasonably large. We will encounter other problem anyway.
    // Otherwise, estimation of information quantity,
}
*/


public class InformationEstimator implements InformationEstimatorInterface {
    private final FrequencerInterface myFrequencer = new Frequencer();  // Object for counting frequency
    private boolean mySpaceNotReady = true;
    private boolean myTargetNotReady = true;

    private static final double C0 = 1/Math.log10(2d);
    private static final int INT_MAX = Integer.MAX_VALUE;
    private static final double DOUBLE_MAX = Double.MAX_VALUE;

    private double C1 = 0;                                         
    private int len;                                               
    private int max_target_len = 0;                   

    private double[] memo;

    private final double iq(int freq) {
        return (-Math.log10((double) freq) + C1) * C0;
    }

    private final double iq_10(int freq) {
        return -Math.log10((double) freq) + C1;
    }

    public void setTarget(byte[] target) {
        len = target.length;
        if (len == 0) {
            myTargetNotReady = true; 
        } else {
            myTargetNotReady = false;
            myFrequencer.setTarget(target);
            if (max_target_len < len) {
                max_target_len = len;
                memo = new double[len - 1];
            }
        }
    }

    public void setSpace(byte[] space) {
        mySpaceNotReady = space.length == 0;
        if (mySpaceNotReady) return;
        myFrequencer.setSpace(space);
        C1 = Math.log10((double) space.length);
    }

    @Override
    public double estimation(){
        if (myTargetNotReady) return 0d;
        if (mySpaceNotReady) return DOUBLE_MAX;

        int freq = myFrequencer.subByteFrequency(0, 1);
        if (len == 1) return (freq == 0) ? DOUBLE_MAX : iq(freq);

        int start = 1, end = 2, mp = 1;
        memo[0] = freq == 0 ? DOUBLE_MAX : iq_10(freq);

        for (;;){
            freq = myFrequencer.subByteFrequency(0, end);
            double min = freq == 0 ? DOUBLE_MAX : iq_10(freq);
            do {
                freq = myFrequencer.subByteFrequency(start, end);
                if (freq == 0) break;
                double iq = iq_10(freq) + memo[--start];
                if (iq < min) { min = iq; }
            } while (start > 0);

            if (end == len){
                return min == DOUBLE_MAX ? DOUBLE_MAX : min * C0;
            }

            memo[mp] = min;

            mp = start = end++;
        }
    }

    private final double slowEstimation(){
        if (myTargetNotReady) return 0d;
        if (mySpaceNotReady) return DOUBLE_MAX;

        boolean [] partition = new boolean[len+1];
        int np;
        np = 1<<(len-1);
        double value = Double.MAX_VALUE;

        for(int p=0; p<np; p++) {
        partition[0] = true;
            for(int i=0; i<len -1;i++) {
            partition[i+1] = (0 !=((1<<i) & p));
            }
            partition[len] = true;

            double value1 = (double) 0.0;
            int end = 0;
            int start = end;
            while(start<len) {
                end++;
                while(partition[end] == false) { 
                    end++;
                }
                int freq = myFrequencer.subByteFrequency(start, end);
                value1 = freq == 0 ? Double.MAX_VALUE : value1 + iq(freq);
                start = end;
            }
            if(value1 < value) value = value1;
        }
        return value;
    }

    private final double check() {
        double fast = estimation();
        double slow = slowEstimation();
        if (Math.abs(fast - slow) > 1e-15) System.out.println("ERROR: WRONG\nfast:" + fast + "\nslow:" + slow);
        return fast;
    }

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
        myObject = new InformationEstimator();
        myObject.setSpace("3210321001230123".getBytes());
        myObject.setTarget("0".getBytes());
        value = myObject.check();
        System.out.println(">0 "+value);
        myObject.setTarget("01".getBytes());
        value = myObject.check();
        System.out.println(">01 "+value);
        myObject.setTarget("0123".getBytes());
        value = myObject.check();
        System.out.println(">0123 "+value);
        myObject.setTarget("00".getBytes());
        value = myObject.check();
        System.out.println(">00 "+value);
        myObject.setTarget("4".getBytes());
        value = myObject.check();
        System.out.println(">4 "+value);
        myObject.setTarget("321".getBytes());
        value = myObject.check();
        System.out.println(">321 "+value);
        myObject.setTarget("3210".getBytes());
        value = myObject.check();
        System.out.println(">3210 "+value);
        myObject.setTarget("32121".getBytes());
        value = myObject.check();
        System.out.println(">32121 "+value);
        myObject.setTarget("31313131".getBytes());
        value = myObject.check();
        System.out.println(">31313131 "+value);
        byte[] space = new byte[10240];
        byte[] target = new byte[10240];
        java.security.SecureRandom rnd = new java.security.SecureRandom();
        
        rnd.setSeed(System.nanoTime());
        rnd.nextBytes(target);

        for (int i = 0, j = 0; i < space.length; i++, j++) {
            if (j >= target.length) j = 0;
            space[i] = target[j];
        }
        
        myObject = new InformationEstimator();
        long t1 = System.nanoTime();
        myObject.setSpace(space);
        long t2 = System.nanoTime();
        System.out.println(">setSpace in " + (t2 - t1) + "ns");
        myObject.setTarget(target);
        value = myObject.estimation();
        System.out.println(">space(" + (space.length >> 10) + "k), target(" + (target.length >> 10) + "k) "  + value);
    }
}

//参考：https://github.com/D3879/2022informationQuantity/blob/main/s4/B223323/InformationEstimater.java