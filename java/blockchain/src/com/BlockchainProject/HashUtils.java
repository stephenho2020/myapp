package com.BlockchainProject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {


    /**
     * Generate SHA-256 hash of a string.
     *
     * @param   data   the string to generate SHA-256 hash
     * @return         a SHA-256 hash
     */
    public static String getHashForStr(String data) {
        try {
            MessageDigest algo = MessageDigest.getInstance("SHA-256");
            algo.update(data.getBytes());
            byte[] hashOfData = algo.digest();
            return byteToHex(hashOfData);

        }catch (NoSuchAlgorithmException ex) {
            return "";
        }
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array       the byte array to convert
     * @return              a length*2 character string encoding the byte array
     */
    private static String byteToHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }



    /**
     * PoW Demo
     * @param diff number of Prefix0
     * @param str string
     * @return satisfied hash value
     */
    public static String powDemo(int diff, String str){
        String prefix0 = getPrefix0(diff);
        System.out.println("prefix0: " + prefix0);
        int nonce = 0;

        String hash = getHashForStr(str);
        while(true){
            assert prefix0 != null;
            if(hash.startsWith(prefix0)){
                System.out.println("Find target!");
                System.out.println("hash: " + hash);
                System.out.println("nonce: " + nonce);
                return hash;
            }else {
                nonce++;
                hash = getHashForStr(str + nonce);
            }
        }
    }

    private static String getPrefix0(int diff){
        if(diff <= 0){
            return null;
        }
        return String.format("%0"+diff+"d", 0);
    }


}
