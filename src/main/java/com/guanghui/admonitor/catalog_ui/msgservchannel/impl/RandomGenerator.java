package com.guanghui.admonitor.catalog_ui.msgservchannel.impl;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Random;

/**
 * Created by cuiju on 2015/12/13.
 *
 */
 class RandomGenerator {
    static String getRandom(){
        return Base64.getEncoder().encodeToString(getRandbytes());
    }

    private static byte[] getRandbytes(){
        Random random = new Random(System.currentTimeMillis());
        long[] rLongs = new long[4];
        rLongs[0] = random.nextLong();
        rLongs[1] = random.nextLong();
        rLongs[2] = random.nextLong();
        rLongs[3] = random.nextLong();
        byte[] rbytes = new byte[Long.BYTES * 2];
        getXorByte(rLongs[0], rLongs[3], rbytes, 0);
        getXorByte(rLongs[1], rLongs[2], rbytes, Long.BYTES);
        return  rbytes;
    }

    private static void getXorByte(long l1, long l2, byte[] bytes, int offset){
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l1);
        buffer.flip();
        byte[] b1 = buffer.array();
        buffer.clear();
        buffer.putLong(l2);
        buffer.flip();
        byte[] b2 = buffer.array();
        for(int i = 0; i < Long.BYTES; i++){
            bytes[i + offset] = (byte) (b1[i] ^ b2[Long.BYTES - 1 - i]);
        }
    }


}
