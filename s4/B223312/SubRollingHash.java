package s4.B223312;

public class SubRollingHash implements RollingHashInterface {
    RollingHash rollingHash;
    int l;
    int r;

    SubRollingHash(RollingHash rollingHash, int l, int r) {
        this.rollingHash = rollingHash;
        this.l = l;
        this.r = r;
    }

    @Override
    public int get_hash(int l, int r) {
        if (this.l + r > this.r) return -1;
        return rollingHash.get_hash(this.l + l, this.l + r);
    }
    @Override
    public byte[] get_str() {
        return this.rollingHash.str;
    }
}
