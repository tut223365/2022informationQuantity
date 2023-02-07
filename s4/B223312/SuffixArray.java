package s4.B223312;

// 参考 『プログラミングコンテストチャレンジブック [第2版]　～問題解決のアルゴリズム活用力とコーディングテクニックを鍛える～』(p.336)

public class SuffixArray {
    byte[] str;
    int[] suffixArray;
    int[] rank;

    int suffixCompare_k(int i, int j, int k) {
        if (rank[i] != rank[j]) return rank[i] - rank[j];
        int ri = i + k <= this.str.length ? rank[i + k] : -1;
        int rj = j + k <= this.str.length ? rank[j + k] : -1;
        return ri - rj;
    }

    private void mergeSort(int l, int r, int k) {
        if (l + 1 == r) return;

        int mid = (l + r) >> 1;
        mergeSort(l, mid, k);
        mergeSort(mid, r, k);

        int[] tmp = new int[r - l];
        for (int i = 0, li = l, ri = mid; i < tmp.length; ++i) {
            if (li < mid && ri < r) {
                if (this.suffixCompare_k(this.suffixArray[li], this.suffixArray[ri], k) < 0) {
                    tmp[i] = this.suffixArray[li++];
                } else {
                    tmp[i] = this.suffixArray[ri++];
                }
            } else if (li < mid) {
                tmp[i] = this.suffixArray[li++];
            } else {
                tmp[i] = this.suffixArray[ri++];
            }
        }

        for (int i = 0; i < tmp.length; ++i) this.suffixArray[i + l] = tmp[i];
    }

    public SuffixArray(byte[] str) {
        this.str = str;

        // ダブリングで構築する
        // 各 suffix の長さ k の prefix をソートしたものから長さ 2k の prefix をソートしたものを計算できる。
        // k = 1 -> 2 -> 4 -> ... -> N と順番に計算する。[log(N)] 回
        // ソートは \Theta(N\log(N)) かかるため，構築全体の計算量は \Theta(N\log^2(N))

        this.suffixArray = new int[str.length + 1];
        this.rank = new int[str.length + 1];
        for (int i = 0; i <= str.length; ++i) {
            this.suffixArray[i] = i;
            this.rank[i] = i < str.length ? Byte.toUnsignedInt(str[i]) : -1;
        }

        int[] tmp = new int[str.length + 1];
        for (int k = 1; k <= str.length; k <<= 1) {
            this.mergeSort(0, str.length + 1, k);

            tmp[suffixArray[0]] = 0;
            for (int i = 1; i <= str.length; ++i) {
                int increment = suffixCompare_k(suffixArray[i], suffixArray[i - 1], k) == 0 ? 0 : 1;

                tmp[suffixArray[i]] = tmp[suffixArray[i - 1]] + increment;
            }
            for (int i = 0; i <= str.length; ++i) rank[i] = tmp[i];
        }
    }
    
    private int targetCompare(int idx, byte[] target) {
        int sa = suffixArray[idx];
        for (int i = 0; i < target.length; ++i) {
            if (sa + i >= str.length) return -1;
            if (str[sa + i] != target[i]) return str[sa + i] - target[i];
        }
        return 0;
    }

    public int searchLowerBound(byte[] target) {
        int lo = 0, hi = str.length + 1;
        while (hi - lo > 1) {
            int mid = (hi + lo) >> 1;

            if (targetCompare(mid, target) < 0) lo = mid;
            else hi = mid;
        }
        return lo + 1;
    }

    public int searchUpperBound(byte[] target) {
        int lo = 0, hi = str.length + 1;
        while (hi - lo > 1) {
            int mid = (hi + lo) >> 1;

            if (targetCompare(mid, target) <= 0) lo = mid;
            else hi = mid;
        }
        return lo + 1;
    }

    // The variable, "suffixArray" is the sorted array of all suffixes of mySpace.                                    
    // Each suffix is expressed by a integer, which is the starting position in mySpace. 

    public void printSuffixArray() {
        for(int i=0; i< suffixArray.length; i++) {
            int s = suffixArray[i];
            System.out.printf("suffixArray[%2d]=%2d:", i, s);
            for(int j=s;j<str.length;j++) {
                System.out.write(str[j]);
            }
            System.out.write('\n');
        }
    }


}