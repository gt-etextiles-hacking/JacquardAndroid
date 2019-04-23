package edu.gatech.jacquardtoolkit;

class JacquardThreadHelper {

    public static double[] parseThreadDataIntoForces(String hex) {
        if (hex.length() < 36) {
            throw new IllegalArgumentException("Invalid thread data string");
        }
        String trueHex = hex.substring(21, 36);
        double[] output = new double[trueHex.length()];
        for (int i = 0; i < output.length; i++) {
            switch (trueHex.charAt(i)) {
                case '0':
                    output[i] = 0.0 / 15.0;
                    break;
                case '1':
                    output[i] = 1.0 / 15.0;
                    break;
                case '2':
                    output[i] = 2.0 / 15.0;
                    break;
                case '3':
                    output[i] = 3.0 / 15.0;
                    break;
                case '4':
                    output[i] = 4.0 / 15.0;
                    break;
                case '5':
                    output[i] = 5.0 / 15.0;
                    break;
                case '6':
                    output[i] = 6.0 / 15.0;
                    break;
                case '7':
                    output[i] = 7.0 / 15.0;
                    break;
                case '8':
                    output[i] = 8.0 / 15.0;
                    break;
                case '9':
                    output[i] = 9.0 / 15.0;
                    break;
                case 'A':
                    output[i] = 10.0 / 15.0;
                    break;
                case 'B':
                    output[i] = 11.0 / 15.0;
                    break;
                case 'C':
                    output[i] = 12.0 / 15.0;
                    break;
                case 'D':
                    output[i] = 13.0 / 15.0;
                    break;
                case 'E':
                    output[i] = 14.0 / 15.0;
                    break;
                case 'F':
                    output[i] = 15.0 / 15.0;
                    break;
                default:
                    output[i] = 0.0 / 14.0;
                    break;
            }
        }
        return output;
    }
}
