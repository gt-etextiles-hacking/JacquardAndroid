package edu.gatech.muc.jacquard.lib;

public class JacketGlowHelper {
    public static final String RAINBOW_1 = "801308001008180BDA060A0810107830013801";
    public static final String RAINBOW_2 = "414000";

    public static byte[] convertHexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }

    private static String convertByteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
