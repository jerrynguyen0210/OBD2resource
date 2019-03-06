package com.adasone.hm320a.canbusanalyzer;

public class CTools {
    public static int strToInt(String S, int DefValue, int Base) {
        int res = DefValue;
        if (S != null) {
            S = S.trim();
            if (S.length() > 0) {
                try {
                    res = Integer.parseInt(S, Base);
                } catch (NumberFormatException e) {
                }
            }
        }
        return res;
    }

    public static int strToInt(String S, int DefValue) {
        return strToInt(S, DefValue, 10);
    }

    public static float strToFloat(String S, float DefValue) {
        float res = DefValue;
        if (!(S == null || S == "")) {
            try {
                res = Float.parseFloat(S);
            } catch (NumberFormatException e) {
            }
        }
        return res;
    }

    public static int ByteToUInt(byte V) {
        return V & 255;
    }

    public static int getUWordFromBytes(byte[] arr, int offset) {
        return (ByteToUInt(arr[offset + 1]) << 8) + ByteToUInt(arr[offset]);
    }

    public static int getUWordFromBytesHf(byte[] arr, int offset) {
        return (ByteToUInt(arr[offset]) << 8) + ByteToUInt(arr[offset + 1]);
    }

    public static void setUWordToBytes(int Value, byte[] arr, int offset) {
        arr[offset] = (byte) (Value & 255);
        arr[offset + 1] = (byte) ((Value >> 8) & 255);
    }

    public static void setUWordToBytesHf(int Value, byte[] arr, int offset) {
    }

    public static void setDWordToBytes(int Value, byte[] arr, int offset) {
        arr[offset] = (byte) (Value & 255);
        arr[offset + 1] = (byte) ((Value >> 8) & 255);
        arr[offset + 2] = (byte) ((Value >> 16) & 255);
        arr[offset + 3] = (byte) ((Value >> 24) & 255);
    }

    public static int getDWordFromBytes(byte[] arr, int offset) {
        return (((ByteToUInt(arr[offset + 3]) << 24) + (ByteToUInt(arr[offset + 2]) << 16)) + (ByteToUInt(arr[offset + 1]) << 8)) + ByteToUInt(arr[offset]);
    }

    public static int hexStrToInt(String s, int defValue) {
        if (s.length() == 0) {
            return defValue;
        }
        int res = 0;
        for (char c : s.trim().toUpperCase().toCharArray()) {
            res <<= 4;
            switch (c) {
                case '0':
                    res += 0;
                    break;
                case '1':
                    res++;
                    break;
                case '2':
                    res += 2;
                    break;
                case '3':
                    res += 3;
                    break;
                case '4':
                    res += 4;
                    break;
                case '5':
                    res += 5;
                    break;
                case '6':
                    res += 6;
                    break;
                case '7':
                    res += 7;
                    break;
                case '8':
                    res += 8;
                    break;
                case '9':
                    res += 9;
                    break;
                case 'A':
                    res += 10;
                    break;
                case 'B':
                    res += 11;
                    break;
                case 'C':
                    res += 12;
                    break;
                case 'D':
                    res += 13;
                    break;
                case 'E':
                    res += 14;
                    break;
                case CCanBusInterface.RATE_100 /*70*/:
                    res += 15;
                    break;
                default:
                    return defValue;
            }
        }
        return res;
    }
}
