package s4.B223312;


public class SuffixArray {
    byte[] str;

    int[] suffixArray;
    int[] lcp;

    private int suffixCompare_k(int i, int j, int k, int[] rank) {
        if (rank[i] != rank[j]) return rank[i] - rank[j];
        int ri = i + k <= str.length ? rank[i + k] : -1;
        int rj = j + k <= str.length ? rank[j + k] : -1;
        return ri - rj;
    }
    private void mergeSort(int l, int r, int k, int[] rank) {
        if (l + 1 == r) return;

        int mid = (l + r) >> 1;
        mergeSort(l, mid, k, rank);
        mergeSort(mid, r, k, rank);

        int[] tmp = new int[r - l];
        for (int i = 0, li = l, ri = mid; i < tmp.length; ++i) {
            if (li < mid && ri < r) {
                if (this.suffixCompare_k(this.suffixArray[li], this.suffixArray[ri], k, rank) < 0) {
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
    private void constructSuffixArraySlow() {
        // 参考：『プログラミングコンテストチャレンジブック [第2版]　～問題解決のアルゴリズム活用力とコーディングテクニックを鍛える～』(p.336)
        // ダブリングで構築する
        // 各 suffix の長さ k の prefix をソートしたものから長さ 2k の prefix をソートしたものを計算できる。
        // k = 1 -> 2 -> 4 -> ... -> N と順番に計算する。[log(N)] 回
        // ソートは \Theta(N\log(N)) かかるため，構築全体の計算量は \Theta(N\log^2(N))

        this.suffixArray = new int[str.length + 1];

        int[] rank = new int[str.length + 1];
        for (int i = 0; i <= str.length; ++i) {
            suffixArray[i] = i;
            rank[i] = i < str.length ? Byte.toUnsignedInt(str[i]) : -1;
        }

        int[] tmp = new int[str.length + 1];
        for (int k = 1; k <= str.length; k <<= 1) {
            this.mergeSort(0, str.length + 1, k, rank);

            tmp[suffixArray[0]] = 0;
            for (int i = 1; i <= str.length; ++i) {
                int increment = suffixCompare_k(suffixArray[i], suffixArray[i - 1], k, rank) == 0 ? 0 : 1;

                tmp[suffixArray[i]] = tmp[suffixArray[i - 1]] + increment;
            }
            for (int i = 0; i <= str.length; ++i) rank[i] = tmp[i];
        }
    }
    private int getstr(int idx) {
        return (idx < str.length ? str[idx] : 0);
    }
    private void constructSuffixArray() {
        // 参考：https://wk1080id.hatenablog.com/entry/2018/12/25/005926
        // 計算量 O(NlogN)

        this.suffixArray = new int[str.length + 1]; // p

        int sigma = Byte.toUnsignedInt(Byte.MAX_VALUE);
        int[] classes = new int[suffixArray.length];
        int[] counts = new int[Math.max(sigma, suffixArray.length + 1)];

        { // k = 0
            for (int i = 0; i < suffixArray.length; ++i) {
                ++counts[getstr(i)];
            }
            for (int i = 0; i < sigma - 1; ++i) {
                counts[i + 1] += counts[i];
            }
            for (int i = 0; i < suffixArray.length; ++i) {
                suffixArray[--counts[getstr(i)]] = i;
            }

            int cur = 0;
            for (int i = 0; i < suffixArray.length; ++i) {
                if (i != 0 && getstr(suffixArray[i]) != getstr(suffixArray[i - 1])) {
                    ++cur;
                }
                classes[suffixArray[i]] = cur;
            }
        }
        
        for (int k = 0; (1 << k) < suffixArray.length; ++k) {
            int[] nextArray = new int[suffixArray.length];
            int[] nextClasses = new int[classes.length];
            for (int i = 0; i < suffixArray.length; ++i) {
                int next = suffixArray[i] - (1 << k);

                nextArray[i] = (next < 0 ? next + suffixArray.length : next);
            }

            for (int i = 0; i < counts.length; ++i) counts[i] = 0;
            for (int i = 0; i < suffixArray.length; ++i) {
                int idx = nextArray[i];

                ++counts[classes[idx]];
            }
            for (int i = 0; i < counts.length - 1; ++i) counts[i + 1] += counts[i];
            for (int i = suffixArray.length - 1; i >= 0; --i) {
                int idx = --counts[classes[nextArray[i]]];
                suffixArray[idx] = nextArray[i];
            }

            int cur = 0;
            for (int i = 0; i < suffixArray.length; ++i) {
                if (i != 0) {
                    int firstL = classes[suffixArray[i]];
                    int firstR = classes[suffixArray[i - 1]];
                    int secondL = classes[(suffixArray[i] + (1 << k)) % suffixArray.length];
                    int secondR = classes[(suffixArray[i - 1] + (1 << k)) % suffixArray.length];
                    
                    if (firstL != firstR || secondL != secondR) ++cur;
                }
                nextClasses[suffixArray[i]] = cur;
            }
            classes = nextClasses;
        }
    }
    private void constructLCPArray() {
        // LCP 配列の構築
        // 参考：https://qiita.com/kgoto/items/9e28e37b8a4b15ea7230

        this.lcp = new int[suffixArray.length];
        int[] rank = new int[suffixArray.length];
        for (int i = 0; i < suffixArray.length; ++i) {
            int sa = suffixArray[i];

            rank[sa] = i;
        }

        int l = 0;
        for (int i = 0; i < str.length; ++i) {
            int i1 = i;
            int i2 = suffixArray[rank[i] - 1];

            while (i1 + l < str.length && i2 + l < str.length && str[i1 + l] == str[i2 + l]) {
                ++l;
            }

            lcp[rank[i]] = l;
            l = (l == 0 ? 0 : l - 1);
        }
    }
    public SuffixArray(byte[] str) {
        this.str = str;

        constructSuffixArray();
        constructLCPArray();
    }
    public SuffixArray(byte[] str, boolean slow) {
        this.str = str;

        if (slow) {
            constructSuffixArraySlow();
        } else {
            constructSuffixArray();
        }
        constructLCPArray();
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

    // strIdx 文字目だけで比較
    private int targetCompare_k(int saIdx, int strIdx, byte c) {
        int sa = suffixArray[saIdx];
        if (sa + strIdx >= str.length) return -1;
        return str[sa + strIdx] - Byte.toUnsignedInt(c);
    }
    public int searchLowerBound_k(byte c) {
        return searchLowerBound_k(c, 0, 0, suffixArray.length);
    }
    public int searchLowerBound_k(byte c, int strIdx, int lo, int hi) {
        lo = (lo == 0 ? lo : lo - 1);
        while (hi - lo > 1) {
            int mid = (hi + lo) >> 1;

            if (targetCompare_k(mid, strIdx, c) < 0) lo = mid;
            else hi = mid;
        }
        return lo + 1;
    }
    public int searchUpperBound_k(byte c) {
        return searchUpperBound_k(c, 0, 0, suffixArray.length);
    }
    public int searchUpperBound_k(byte c, int strIdx, int lo, int hi) {
        lo = (lo == 0 ? lo : lo - 1);
        while (hi - lo > 1) {
            int mid = (hi + lo) >> 1;

            if (targetCompare_k(mid, strIdx, c) <= 0) lo = mid;
            else hi = mid;
        }
        return lo + 1;
    }

    public void printSuffixArray() {
        for(int i=0; i< suffixArray.length; i++) {
            int s = suffixArray[i];
            int l = lcp[i];
            System.out.printf("suffixArray[%2d]=%2d:lcpArray[%2d]=%2d:", i, s, i, l);
            for(int j=s;j<str.length;j++) {
                System.out.write(str[j]);
            }
            System.out.write('\n');
        }
    }
}