package s4.B223323; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;
import s4.specification.*;

/* What is imported from s4.specification
package s4.specification;
public interface InformationEstimatorInterface{
    void setTarget(byte target[]); // set the data for computing the information quantities
    void setSpace(byte space[]); // set data for sample space to computer probability
    double estimation(); // It returns 0.0 when the target is not set or Target's length is zero;
// It returns Double.MAX_VALUE, when the true value is infinite, or space is not set.
// The behavior is undefined, if the true value is finete but larger than Double.MAX_VALUE.
// Note that this happens only when the space is unreasonably large. We will encounter other problem anyway.
// Otherwise, estimation of information quantity, 
}                        
*/

public class InformationEstimator implements InformationEstimatorInterface{
    // Code to tet, *warning: This code condtains intentional problem*
    byte [] myTarget; // data to compute its information quantity
    byte [] mySpace;  // Sample space to compute the probability
    final FrequencerInterface myFrequencer = new Frequencer();  // Object for counting frequency
    boolean mySpaceNotReady = true;

    private static final double C0 = Math.log10(2d);
    private static final int IMAX = Integer.MAX_VALUE;
    private static final double DMAX = Double.MAX_VALUE;
    private double C1 = 0;

    byte [] subBytes(byte [] x, int start, int end) {
        // corresponding to substring of String for  byte[] ,
        // It is not implement in class library because internal structure of byte[] requires copy.
        byte [] result = new byte[end - start];
        for(int i = 0; i<end - start; i++) { result[i] = x[start + i]; };
        return result;
    }

    // IQ: information quantity for a count,  -log2(count/sizeof(space))
    double iq(int freq) {
        return -Math.log10((double) freq / (double) mySpace.length) / Math.log10(2d);
    }

    public void setTarget(byte [] target) { myTarget = target;}
    public void setSpace(byte []space) {
        mySpace = space; myFrequencer.setSpace(space);
        C1 = Math.log10((double) mySpace.length);
        mySpaceNotReady = false; 
    }

    public double estimation() {
        if (mySpaceNotReady) return Double.MAX_VALUE;
        final int len = myTarget.length;
        if (len == 0) return 0d;
        myFrequencer.setTarget(myTarget);

        int min = myFrequencer.subByteFrequency(0, 1);
        if (len == 1) return (min == 0) ? DMAX : (C1 - Math.log10(min)) / C0;

        int end = 2, mp = 1, start = 1;
        int[] memo = new int[(len << 1) - 1];

        if (min == 0) min = IMAX;
        memo[0] = min;
        for (;;) {
            min = myFrequencer.subByteFrequency(0, end);
            int p = 0;
            if (min == 0) min = IMAX;

            if (end == len) {
                for (;;) {
                    int freq = myFrequencer.subByteFrequency(start, end);
                    if (freq == 0) return min == IMAX ? DMAX: (C1 - Math.log10(min)) / C0;
                    int iq = freq * memo[--start];
                    if (iq < min) { 
                        min = iq;
                        p = start;
                        for (;;) {
                            if (start == 0) return (C1 * (memo[p + len] + 2) - Math.log10(min)) / C0;
                            freq = myFrequencer.subByteFrequency(start, end);
                            if (freq == 0) return (C1 * (memo[p + len] + 2) - Math.log10(min)) / C0;
                            iq = freq * memo[--start];
                            if (iq < min) { min = iq; p = start; }
                        }
                    }
                    if (start == 0) return min == IMAX ? DMAX: (C1 - Math.log10(min)) / C0;
                }
            } 
            
            do {
                int freq = myFrequencer.subByteFrequency(start, end);
                if (freq == 0) break;
                int iq = freq * memo[--start];
                if (iq < min) { min = iq; p = start; }
            } while (start > 0);

            memo[mp + len] = memo[p + len] + 1;
            memo[mp] = min;
            mp = start = end++;
        }
    }

    private double slowEstimation(){
        boolean [] partition = new boolean[myTarget.length+1];
        int np;
        np = 1<<(myTarget.length-1);
        // System.out.println("np="+np+" length="+myTarget.length);
        double value = DMAX; // value = mininimum of each "value1".

        for(int p=0; p<np; p++) { // There are 2^(n-1) kinds of partitions.
            // binary representation of p forms partition.
            // for partition {"ab" "cde" "fg"}
            // a b c d e f g   : myTarget
            // T F T F F T F T : partition:
            partition[0] = true; // I know that this is not needed, but..
            for(int i=0; i<myTarget.length -1;i++) {
            partition[i+1] = (0 !=((1<<i) & p));
            }
            partition[myTarget.length] = true;

            // Compute Information Quantity for the partition, in "value1"
            // value1 = IQ(#"ab")+IQ(#"cde")+IQ(#"fg") for the above example
            double value1 = (double) 0.0;
            int end = 0;
            int start = end;
            while(start<myTarget.length) {
                // System.out.write(myTarget[end]);
                end++;
                while(partition[end] == false) { 
                    // System.out.write(myTarget[end]);
                    end++;
                }
                // System.out.print("("+start+","+end+")");
                myFrequencer.setTarget(subBytes(myTarget, start, end));
                int freq = myFrequencer.frequency();
                value1 = freq == 0 ? Double.MAX_VALUE : value1 + iq(freq);
                
                start = end;
            }
            // System.out.println(" "+ value1);

            // Get the minimal value in "value"
            if(value1 < value) value = value1;
        }
        return value;
    }

    private double check() {
        double fast = estimation();
        double slow = slowEstimation();
        if (fast != slow) System.out.println("ERROR: WRONG\nfast:" + fast + "\nslow:" + slow);
        return slow;
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
    }
}