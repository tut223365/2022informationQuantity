package s4.B223312;

public class FastInformationEstimator {
    byte[] myTarget;
    byte[] mySpace;

    double logSpace; // 実行時定数値なので前計算可能
    
    private double iq(int freq) {
        return -Math.log10((double) freq) + logSpace;
    }

    public FastInformationEstimator(byte[] mySpace, byte[] myTarget) {
        this.mySpace = mySpace;
        this.myTarget = myTarget;
        this.logSpace = Math.log10((double) mySpace.length);
    }
    private double estimationInner() {
        /*
         * incremental に二分探索をする
         * 計算量 O(|T|^2log|S|)
         */

        int spaceLength = mySpace.length;
        int targetLength = myTarget.length;
        SuffixArray suffixArray = new SuffixArray(mySpace);
        suffixArray.printSuffixArray();

        double[] dp = new double[targetLength + 1];
        for (int i = 1; i < dp.length; ++i) dp[i] = Double.MAX_VALUE;

        for (int i = 0; i < targetLength; ++i) {
            int lo = 0, hi = spaceLength + 1;
            for (int k = 0; i + k < targetLength; ++k) { // target[i; i + k]
                int j = i + k + 1;

                lo = suffixArray.searchLowerBound_k(myTarget[i + k], k, lo, hi);
                hi = suffixArray.searchUpperBound_k(myTarget[i + k], k, lo, hi);

                int freq = hi - lo;
                if (freq == 0) {
                    break;
                }

                dp[j] = Math.min(dp[j], dp[i] + iq(freq));
            }
        }

        return (dp[targetLength] == Double.MAX_VALUE ? Double.MAX_VALUE : dp[targetLength] / Math.log10((double) 2.0));
    }
    private double estimationBySuffixTree() {
        /*
         * SuffixTree を潜りながら動的計画法を行う
         * 計算量 O(|T|^2log|sigma|)
         */

        SuffixTree suffixTree = new SuffixTree(mySpace);
        // suffixTree.printSuffixTree();
        
        int targetLength = myTarget.length;
        double[] dp = new double[targetLength + 1];
        for (int i = 1; i < dp.length; ++i) dp[i] = Double.MAX_VALUE;

        for (int i = 0; i < targetLength; ++i) {
            SuffixNode node = suffixTree.root;
            for (int k = 0; i + k < targetLength; ++k) { // target[i; i + k]
                int j = i + k + 1;

                node = suffixTree.search(node, myTarget[i + k], k);

                int freq = node.length();
                if (freq == 0) {
                    break;
                }

                dp[j] = Math.min(dp[j], dp[i] + iq(freq));
            }
        }
  
        return (dp[targetLength] == Double.MAX_VALUE ? Double.MAX_VALUE : dp[targetLength] / Math.log10((double) 2.0));
    }
    public double estimation() {
        if (myTarget == null || myTarget.length == 0)
            return 0.0;
        if (mySpace == null || mySpace.length == 0)
            return Double.MAX_VALUE;

        boolean useTree = true;
        if (useTree) return estimationBySuffixTree();
        return estimationInner();
    }
}
