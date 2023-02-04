package s4.B223312;

import java.util.Arrays;
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

public class InformationEstimator implements InformationEstimatorInterface {
    byte[] myTarget; // data to compute its information quantity
    byte[] mySpace; // Sample space to compute the probability
    FrequencerInterface myFrequencer; // Object for counting frequency

    byte[] subBytes(byte[] x, int start, int end) {
        // corresponding to substring of String for byte[] ,
        // It is not implement in class library because internal structure of byte[]
        // requires copy.

        return Arrays.copyOfRange(x, start, end);
    }

    // IQ: information quantity for a count, -log2(count/sizeof(space))
    double iq(int freq) {
        return -Math.log10((double) freq / (double) mySpace.length) / Math.log10((double) 2.0);
    }

    public void setTarget(byte[] target) {
        myTarget = target;
    }

    public void setSpace(byte[] space) {
        myFrequencer = new Frequencer();
        mySpace = space;
        myFrequencer.setSpace(space);
    }

    public double estimation() {
        if (myTarget == null || myTarget.length == 0) return 0.0;
        if (mySpace == null || mySpace.length == 0) return Double.MAX_VALUE;

        int targetLength = myTarget.length;

        // 動的計画法で求める
        // 配列 dp[i] := target[0; i] までの解
        // 計算 dp[j] := min(dp[i] + iq(target[i; j]) | 0 <= i < j)
        // 最終的な解 = dp[targetLength]
        // iq の計算に \Theta(N\log(N))
        // dp 配列の計算に \Theta(N^2) かかるので
        // 全体の計算量は \Theta(N^3\log(N))

        double[] dp = new double[targetLength + 1];
        dp[0] = 0;
        for (int i = 1; i <= targetLength; ++i) dp[i] = Double.MAX_VALUE;
        
        for (int i = 0; i < targetLength; ++i) {
            for (int j = i + 1; j <= targetLength; ++j) {
                myFrequencer.setTarget(subBytes(myTarget, i, j));
                double value = dp[i] + iq(myFrequencer.frequency());

                if (value < dp[j]) dp[j] = value;
            }
        }

        return dp[targetLength];
    }

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
        myObject = new InformationEstimator();
        myObject.setSpace("3210321001230123".getBytes());
        myObject.setTarget("0".getBytes());
        value = myObject.estimation();
        System.out.println(">0 " + value);
        myObject.setTarget("01".getBytes());
        value = myObject.estimation();
        System.out.println(">01 " + value);
        myObject.setTarget("0123".getBytes());
        value = myObject.estimation();
        System.out.println(">0123 " + value);
        myObject.setTarget("00".getBytes());
        value = myObject.estimation();
        System.out.println(">00 " + value);
    }
}
