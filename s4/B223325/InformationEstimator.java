package s4.B223325; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;
import s4.specification.*;

/* What is imported from s4.specification
package s4.specification;
public interface InformationEstimatorInterface {
    void setTarget(byte target[]);  // set the data for computing the information quantities
    void setSpace(byte space[]);  // set data for sample space to compute probability
    double estimation();  // It returns 0.0 when the target is not set or Target's length is zero;
    // It returns Double.MAX_VALUE, when the true value is infinite, or space is not set.
    // The behavior is undefined, if the true value is finete but larger than Double.MAX_VALUE.
    // Note that this happens only when the space is unreasonably large. We will encounter other problem anyway.
    // Otherwise, estimation of information quantity,
}
*/


public class InformationEstimator implements InformationEstimatorInterface {
    static boolean debugMode = false;
    static boolean useSlow = false;
    // Code to test, *warning: This code is slow, and it lacks the required test
    byte[] myTarget; // data to compute its information quantity
    byte[] mySpace;  // Sample space to compute the probability
    boolean targetReady = false;
    boolean spaceReady = false;
    Frequencer myFrequencer;  // Object for counting frequency
    // FrequencerInterface myFrequencer;  // Object for counting frequency

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
        for(int i = 0; i<end - start; i++) { 
            result[i] = x[start + i]; 
        }
        return result;
    }

    // IQ: information quantity for a count, -log2(count/sizeof(space))
    double iq(int freq) {
        return  - Math.log10((double)freq/(double)mySpace.length);
        // return  - Math.log2((double)freq/(double)mySpace.length);
    }

    @Override
    public void setTarget(byte[] target) {
        myTarget = target;
        targetReady = true;
    }

    @Override
    public void setSpace(byte[] space) {
        myFrequencer = new Frequencer();
        mySpace = space; 
        myFrequencer.setSpace(space);
        spaceReady = true;
    }

    @Override
    public double estimation(){
        // use slow の場合は slowEstimation を使用
        if (useSlow){
            return slowEstimation();
        }
        // 入力が正しく設定されていないときは，以下を返す（仕様）
        if (!targetReady || (myTarget.length == 0)){
            return 0.0;
        } else if (!spaceReady || (mySpace.length == 0)) {
            return Double.MAX_VALUE;
        }

        int sl = mySpace.length;
        
        // // Frequency を一部のみ予め計算する．
        // double[] iqs = new double[myTarget.length];
        // int[] freqs = new int[myTarget.length];
        // for (int i = 0; i < myTarget.length; i++) {
        //     myFrequencer.setTarget(subBytes(myTarget, i, i+1));
        //     freqs[i] = myFrequencer.frequency();
        //     iqs[i] = iq(freqs[i]);
        // }
        // // 最終的に log を取る直前の中身を計算していく
        // for(int i = 1; i < myTarget.length ; i++){
        //     for (int j = 0; j+i < myTarget.length; j++){
        //         // 前の freqs[j] が 0 なら，以降の freqs[j] も 0 となるはず．
        //         // そうであるなら計算しない
        //         if (freqs[j] > 0) {
        //             myFrequencer.setTarget(subBytes(myTarget, j, j+i+1));
        //             freqs[j] = myFrequencer.frequency();
        //             iqs[j] = Math.min(
        //                 iq(freqs[j]), 
        //                 iqs[j]+iqs[j+1]
        //             ); 
        //         } else {
        //             iqs[j] = iqs[j]*iqs[j+1];
        //         }   
        //     }
        // }

        // Frequency を一部のみ予め計算する．
        double[] iqs = new double[myTarget.length];
        int[][] freqs = new int[myTarget.length][2];
        myFrequencer.setTarget(myTarget);
        for (int j = 0; j < myTarget.length; j++) {
            myFrequencer.setSpaceOffset(0);
            myFrequencer.setTargetIndex(j);
            freqs[j][0] = myFrequencer.subByteStartIndex2(0, mySpace.length);
            freqs[j][1] = myFrequencer.subByteEndIndex2(freqs[j][0], mySpace.length);
            iqs[j] = iq(freqs[j][1]-freqs[j][0]);
        }
        for(int i = 1; i < myTarget.length ; i++){
            for (int j = 0; j+i < myTarget.length; j++){
                if (freqs[j][1]-freqs[j][0] > 0) {
                    myFrequencer.setSpaceOffset(i);
                    myFrequencer.setTargetIndex(i+j);
                    freqs[j][0] = myFrequencer.subByteStartIndex2(freqs[j][0], freqs[j][1]);
                    freqs[j][1] = myFrequencer.subByteEndIndex2(freqs[j][0], freqs[j][1]);
                    iqs[j] = Math.min(
                        iq(freqs[j][1]-freqs[j][0]), 
                        iqs[j]+iqs[j+1]
                    ); 
                } else {
                    iqs[j] = iqs[j]*iqs[j+1];
                }   
            }
        }

        // 0 で log を取ると無限になるので，そのときは以下を返す（仕様）
        if (Double.isInfinite(iqs[0])) {
            return Double.MAX_VALUE;
        }
        // 最小の情報量
        double min_iq = iqs[0]/Math.log10((double)2.0);
        // 表示
        if(debugMode) { 
            showVariables(); 
            System.out.printf("length=%d \n", myTarget.length);
            System.out.printf("miniq %10.5f\n", min_iq);
            System.out.println("~ dp table ~");
            for (int i = 0; i < myTarget.length; i++){
                System.out.printf("%.3f ", iqs[i]);
            }
            System.out.println();
            // System.out.println("~ suffixArray ~");
            // myFrequencer.printSuffixArray();
        }
        return min_iq;
    }

    public double slowEstimation(){
        boolean [] partition = new boolean[myTarget.length+1];
        int np = 1<<(myTarget.length-1);
        double value = Double.MAX_VALUE; // value = mininimum of each "value1".
        if(debugMode) {
            showVariables(); 
        }
        if(debugMode) { System.out.printf("np=%d length=%d ", np, +myTarget.length); }

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
                end++;;
                while(partition[end] == false) {
                    // System.out.write(myTarget[end]);
                    end++;
                }
                // System.out.print("("+start+","+end+")");
                // 情報量を実際に求める部分
                myFrequencer.setTarget(subBytes(myTarget, start, end));
                value1 = value1 + iq(myFrequencer.frequency());
                start = end;
            }
            // System.out.println(" "+ value1);

            // Get the minimal value in "value"
            if(value1 < value) value = value1;
        }
        if(debugMode) { System.out.printf("%10.5f\n", value); }
        return value;
    }

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
        debugMode = false;
        useSlow = false;
        myObject = new InformationEstimator();
        myObject.setSpace("3210321001230123".getBytes());
        myObject.setTarget("0".getBytes());
        value = myObject.estimation();
        System.out.println(">0 "+value);
        myObject.setTarget("01".getBytes());
        value = myObject.estimation();
        System.out.println(">01 "+value);
        myObject.setTarget("0123".getBytes());
        value = myObject.estimation();
        System.out.println(">0123 "+value);
        myObject.setTarget("00".getBytes());
        value = myObject.estimation();
        System.out.println(">00 "+value);

        // 3210321001230123 0 np=1 length=1    2.00000
        // >0 2.0
        // 3210321001230123 01 np=2 length=2    3.00000
        // >01 3.0
        // 3210321001230123 0123 np=8 length=4    3.00000
        // >0123 3.0
        // 3210321001230123 00 np=2 length=2    4.00000
        // >00 4.0
    }
}

