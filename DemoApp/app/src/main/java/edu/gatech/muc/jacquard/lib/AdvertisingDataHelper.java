package edu.gatech.muc.jacquard.lib;

import android.util.Log;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

class AdvertisingDataHelper {
    public static String getIDFromData(byte[] bytes) {
        throw new RuntimeException("NOT IMPLEMENTED");
        /*
        int accumulator = 0;
        int bitsLeft = 0;
        int bytesUsed = 0;
        List<Integer> finalList = new LinkedList<>();

        for (int i = 2; i < bytes.length; i++) {
            if (bitsLeft < 6) {
                accumulator += bytes[bytesUsed] << bitsLeft;
                bytesUsed += 1;
                bitsLeft += 8;
            }

            int sixBitCode = accumulator & 0x3f;
            if (sixBitCode <= 0x09) {
                sixBitCode += (int)'0';
            } else if (sixBitCode <= 0x22) {
                sixBitCode += (int)'A' - 0x0a;
            } else if (sixBitCode <= 0x3b) {
                sixBitCode += (int)'a' - 0x23;
            } else if (sixBitCode == 0x3c) {
                sixBitCode = 0x2d;
            } else if (sixBitCode == 0x3f) {
                break;
            } else {
                continue;
            }

            accumulator >>= 6;
            bitsLeft -= 6;
            finalList.add(sixBitCode);
        }
        byte[] endBytes = new byte[finalList.size()];
        int i = 0;
        for (Integer integer : finalList) {
            endBytes[i] = integer.byteValue();
        }
        return new String(endBytes, StandardCharsets.US_ASCII);*/
    }
}
