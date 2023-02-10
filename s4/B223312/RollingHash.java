package s4.B223312;

public class RollingHash implements RollingHashInterface {
    byte[] str;

    int MOD = (1 << 30) - 1;
    long base = 12345678;

    long[] pows;
    long[] hash;

    int apply(long x) {
        x = x % MOD;
        return (int)(x < 0 ? x + MOD : x);
    }

    RollingHash(byte[] str) {
        this.str = str;
        
        this.pows = new long[str.length + 1];
        this.hash = new long[str.length + 1];
    
        this.pows[0] = 1;
        this.hash[0] = 0;
        for (int i = 0; i < str.length; ++i) {
            this.pows[i + 1] = apply(this.pows[i] * base);
            this.hash[i + 1] = apply(this.hash[i] * base + str[i]);
        }
    }

    @Override
    public int get_hash(int l, int r) {
        return apply(this.hash[r] - this.hash[l] * this.pows[r - l]);
    }

    @Override
    public byte[] get_str() {
        return this.str;
    }
}
