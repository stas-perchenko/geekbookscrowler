package com.alperez.siphash;

/**
 * Created by stanislav.perchenko on 12/18/2018
 */
public class SipHashKey {
    private byte[] keyBytes;
    private final long k0, k1;



    public static SipHashKey ofBytes(byte[] key) {
        if (key == null || key.length != 16) throw new IllegalArgumentException("SipHash key must be 16 bytes long");
        long k0, k1;
        k0 =    ((long) key[0])       |
                ((long) key[1]) << 8  |
                ((long) key[2]) << 16 |
                ((long) key[3]) << 24 |
                ((long) key[4]) << 32 |
                ((long) key[5]) << 40 |
                ((long) key[6]) << 48 |
                ((long) key[7]) << 56;
        k1 =    ((long) key[8])        |
                ((long) key[9])  << 8  |
                ((long) key[10]) << 16 |
                ((long) key[11]) << 24 |
                ((long) key[12]) << 32 |
                ((long) key[13]) << 40 |
                ((long) key[14]) << 48 |
                ((long) key[15]) << 56;
        SipHashKey inst = new SipHashKey(k0, k1);
        inst.keyBytes = key;
        return inst;
    }

    public static SipHashKey ofNumbers(long k0, long k1) {
        SipHashKey inst = new SipHashKey(k0, k1);
        inst.keyBytes = new byte[16];
        inst.keyBytes[0] = (byte) ( k0         & 0xff);
        inst.keyBytes[1] = (byte) ((k0 >>> 8 ) & 0xff);
        inst.keyBytes[2] = (byte) ((k0 >>> 16) & 0xff);
        inst.keyBytes[3] = (byte) ((k0 >>> 24) & 0xff);
        inst.keyBytes[4] = (byte) ((k0 >>> 32) & 0xff);
        inst.keyBytes[5] = (byte) ((k0 >>> 40) & 0xff);
        inst.keyBytes[6] = (byte) ((k0 >>> 48) & 0xff);
        inst.keyBytes[7] = (byte) ((k0 >>> 56) & 0xff);

        inst.keyBytes[8]  = (byte) ( k1         & 0xff);
        inst.keyBytes[9]  = (byte) ((k1 >>> 8 ) & 0xff);
        inst.keyBytes[10] = (byte) ((k1 >>> 16) & 0xff);
        inst.keyBytes[11] = (byte) ((k1 >>> 24) & 0xff);
        inst.keyBytes[12] = (byte) ((k1 >>> 32) & 0xff);
        inst.keyBytes[13] = (byte) ((k1 >>> 40) & 0xff);
        inst.keyBytes[14] = (byte) ((k1 >>> 48) & 0xff);
        inst.keyBytes[15] = (byte) ((k1 >>> 56) & 0xff);
        return inst;
    }

    private SipHashKey(long k0, long k1) {
        this.k0 = k0;
        this.k1 = k1;
    }


    long getK0() {
        return k0;
    }

    long getK1() {
        return k1;
    }

    public byte[] getKeyBytes() {
        byte copy[] = new byte[16];
        System.arraycopy(keyBytes, 0, copy, 0, 16);
        return copy;
    }
}
