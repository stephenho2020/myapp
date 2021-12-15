package com.BlockchainProject;


import javax.xml.bind.DatatypeConverter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Random;

import static com.BlockchainProject.ECDSAUtils.*;


public class DifficultyDemo {

    public static void main(String[] args) throws Exception {

        //Create the key pairs for myself

        KeyPair keyPair = getKeyPair();
        PublicKey publicKeyMyself = keyPair.getPublic();
        PrivateKey privateKeyMyself = keyPair.getPrivate();

        int rand_min = 3000;
        int rand_max = 3010;
        int random_int = (int)Math.floor(Math.random()*(rand_max-rand_min+1)+rand_min);


        NodeMachine nm = new NodeMachine("localhost", 3000);
        nm.execute();
        nm.setUserName("Node ID: " + random_int );
        nm.bc("Node ID: " + random_int);

        // Mining the first block generated coin base transaction
        System.out.println(".....Mining of the Genesis Block.....\n");
        Blockchain.chain.add(Blockchain.createGenesisBlock(privateKeyMyself, publicKeyMyself));
        System.out.println("Block Minded and current block height is " + Blockchain.chain.size());
        System.out.println("New Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).hash);
        System.out.println("Time Stamp of current block: "+ Blockchain.chain.get((Blockchain.chain.size()-1)).timestamp);
        System.out.println("Difficulty of current block: "+ Blockchain.chain.get((Blockchain.chain.size()-1)).difficulty +"\n");


        String newHash;
        Long timeStamp;
        int diff;

        for (int i = 2; i <= 100; i++){
            System.out.println(".....Mining of the block "+ i +".....\n");
            Blockchain.chain.add(Blockchain.addNewBlock(privateKeyMyself,publicKeyMyself));
            nm.bc(".....Mining of the block "+ i +".....");

            newHash = Blockchain.chain.get(i-1).hash;
            System.out.println("New Hash: " + newHash);
            nm.bc("New Hash: " + newHash);

            timeStamp = Blockchain.chain.get((i-1)).timestamp;
            System.out.println("Time Stamp of current block: "+ timeStamp);
            nm.bc("Time Stamp of current block: "+ timeStamp);

            diff = Blockchain.chain.get((i-1)).difficulty;
            System.out.println("Difficulty of current block: "+ diff + "\n");
            nm.bc("Difficulty of current block: "+ diff );
        }


    }
}