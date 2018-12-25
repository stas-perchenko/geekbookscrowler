package com.alperez.siphash;

/**
 * Created by stanislav.perchenko on 12/18/2018
 */
public class SipHash {
    public static long calculateHash(SipHashKey key, byte[] data) {
        long m;
        State s = new State(key);
        int nBlocks = data.length / 8;

        for(int i=0, off=0; i < nBlocks; i++, off += 8) {
            m =     ((long) data[off    ])       |
                    ((long) data[off + 1]) << 8  |
                    ((long) data[off + 2]) << 16 |
                    ((long) data[off + 3]) << 24 |
                    ((long) data[off + 4]) << 32 |
                    ((long) data[off + 5]) << 40 |
                    ((long) data[off + 6]) << 48 |
                    ((long) data[off + 7]) << 56;
            s.processBlock(m);
        }

        m = buildLastBlock(data, nBlocks*8);
        s.processBlock(m);
        s.finish();
        return s.digest();
    }

    private static long buildLastBlock(byte[] data, int offset) {
        long last = ((long) data.length) << 56;

        switch (data.length % 8) {
            case 7:
                last |= ((long) data[offset + 6]) << 48;
            case 6:
                last |= ((long) data[offset + 5]) << 40;
            case 5:
                last |= ((long) data[offset + 4]) << 32;
            case 4:
                last |= ((long) data[offset + 3]) << 24;
            case 3:
                last |= ((long) data[offset + 2]) << 16;
            case 2:
                last |= ((long) data[offset + 1]) << 8;
            case 1:
                last |= (long) data[offset];
                break;
            case 0:
                break;
        }
        return last;
    }


    /******************************************************************************************************************/
    private static class State {
        private long v0;
        private long v1;
        private long v2;
        private long v3;

        public State(SipHashKey key) {
            long k0 = key.getK0();
            long k1 = key.getK1();

            v0 = k0 ^ 0x736f6d6570736575L;
            v1 = k1 ^ 0x646f72616e646f6dL;
            v2 = k0 ^ 0x6c7967656e657261L;
            v3 = k1 ^ 0x7465646279746573L;
        }


        public void processBlock(long m) {
            v3 ^= m;
            compress();
            compress();
            v0 ^= m;
        }

        public void finish() {
            v2 ^= 0xff;
            compress();
            compress();
            compress();
            compress();
        }

        public long digest() {
            return v0 ^ v1 ^ v2 ^ v3;
        }


        private void compress() {
            v0 += v1;
            v2 += v3;
            v1 = rotateLeft(v1, 13);
            v3 = rotateLeft(v3, 16);
            v1 ^= v0;
            v3 ^= v2;
            v0 = rotateLeft(v0, 32);
            v2 += v1;
            v0 += v3;
            v1 = rotateLeft(v1, 17);
            v3 = rotateLeft(v3, 21);
            v1 ^= v2;
            v3 ^= v0;
            v2 = rotateLeft(v2, 32);
        }

        private long rotateLeft(long l, int shift) {
            return (l << shift) | l >>> (64 - shift);
        }

    }
}
