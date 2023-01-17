package s4.B223365; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;
import s4.specification.*;

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
    static boolean debugMode = false;
    // Code to test, *warning: This code is slow, and it lacks the required test
    byte[] myTarget; // data to compute its information quantity
    byte[] mySpace;  // Sample space to compute the probability
    FrequencerInterface myFrequencer;  // Object for counting frequency
    boolean targetReady = false;
    boolean spaceReady = false;

    private void showVariables() {
	for(int i=0; i< mySpace.length; i++) { System.out.write(mySpace[i]); }
	System.out.write(' ');
	for(int i=0; i< myTarget.length; i++) { System.out.write(myTarget[i]); }
	System.out.write(' ');
    }

    byte[] subBytes(byte[] x, int start, int end) {
        // corresponding to substring of String for byte[],
        // It is not implement in class library because internal structure of byte[] requires copy.
        byte[] result = new byte[end - start];
        for(int i = 0; i<end - start; i++) { result[i] = x[start + i]; };
        return result;
    }

    // IQ: information quantity for a count, -log2(count/sizeof(space))
    double iq(int freq) {
        return  - Math.log10((double) freq / (double) mySpace.length)/ Math.log10((double) 2.0);
    }

    @Override
    public void setTarget(byte[] target) {
        myTarget = target;
    }

    @Override
    public void setSpace(byte[] space) {
        myFrequencer = new Frequencer();
        mySpace = space;
        myFrequencer.setSpace(space);
    }

    @Override
    public double estimation(){
        double[] DP_array = new double[myTarget.length+1]; 
        double value = Double.MAX_VALUE; // value = mininimum of each "value1".
        double ETA = 0.00000000000001;

	if(debugMode) { showVariables(); }
        if( myTarget.length == 0 ) return (double) 0.0; //ターゲットの文字列の長さが0

        DP_array[0] = (double) 0.0;
        for(int end=1; end <= myTarget.length; end++){
            double value1 = Double.MAX_VALUE;
            for(int start = 0; start < end; start++){
                double value_innner = (double) 0.0;
                value_innner = value_innner + DP_array[start];

                myFrequencer.setTarget(subBytes(myTarget, start, end));
                value_innner = value_innner + iq(myFrequencer.frequency());

                if( value_innner < value1 ) value1 = value_innner;
            }
            DP_array[end] = value1;
            if( end == myTarget.length ){ //　最後のループの時の情報量
                if( value1 < value ) value = value1;
            }
        }
	    if(debugMode) { System.out.printf("%10.5f\n", value); }

        return value;
    }

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
	debugMode = true;
        myObject = new InformationEstimator();
        myObject.setSpace("3210321001230123".getBytes());
        myObject.setTarget("0".getBytes());
        value = myObject.estimation();
        myObject.setTarget("01".getBytes());
        value = myObject.estimation();
        myObject.setTarget("0123".getBytes());
        value = myObject.estimation();
        myObject.setTarget("00".getBytes());
        value = myObject.estimation();
    }
}

