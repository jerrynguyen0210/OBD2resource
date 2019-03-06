package com.adasone.hm320a.canbusanalyzer;

public class YarConverter {
    private static String DigitToChar(int Digit) {
        switch (Digit) {
            case 0:
                return "0";
            case 1:
                return "1";
            case 2:
                return "2";
            case 3:
                return "3";
            case 4:
                return "4";
            case 5:
                return "5";
            case 6:
                return "6";
            case 7:
                return "7";
            case 8:
                return "8";
            case 9:
                return "9";
            case 10:
                return "A";
            case 11:
                return "B";
            case 12:
                return "C";
            case 13:
                return "D";
            case 14:
                return "E";
            case 15:
                return "F";
            default:
                return "X";
        }
    }

    public static String ByteToHex(byte Value) {
        String S = "";
        int V = Value;
        for (int i = 0; i < 2; i++) {
            S = DigitToChar(V & 15) + S;
            V >>= 4;
        }
        return S;
    }

    public static String DWordToHex(int V) {
        String S = "";
        for (int i = 0; i < 8; i++) {
            S = DigitToChar(V & 15) + S;
            V >>= 4;
        }
        return S;
    }

    public static int ByteToUInt(byte V) {
        return V & 255;
    }
}
