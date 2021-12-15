package com.BlockchainProject;


import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static com.BlockchainProject.ECDSAUtils.*;

public class Main {


    public static void main(String[] args) throws Exception {




        System.out.println("\n.....Generate the Key Pairs for A and B.....\n");
        KeyPair keyPair = getKeyPair();
        PublicKey publicKeyMyself = keyPair.getPublic();
        PrivateKey privateKeyMyself = keyPair.getPrivate();
        String myPubKey = DatatypeConverter.printHexBinary(publicKeyMyself.getEncoded());
        String myPriKey = DatatypeConverter.printHexBinary(privateKeyMyself.getEncoded());
        System.out.println("My publicKey: " + myPubKey);
        System.out.println("My privateKey: " + myPriKey+"\n");

        KeyPair keyPairBob = getKeyPair();
        PublicKey publicKeyBob = keyPairBob.getPublic();
        PrivateKey privateKeyBob = keyPairBob.getPrivate();
        String bobPubKey = DatatypeConverter.printHexBinary(publicKeyBob.getEncoded());
        String bobPriKey = DatatypeConverter.printHexBinary(privateKeyBob.getEncoded());
        System.out.println("B publicKey: " + bobPubKey);
        System.out.println("B privateKey: " + bobPriKey+ "\n");

        System.out.println(".....Mining of the first block.....\n");
        Blockchain.chain.add(Blockchain.createGenesisBlock(privateKeyMyself,publicKeyMyself));
        System.out.println("Block Minded and current block height is " + Blockchain.chain.size());
        System.out.println("Previous Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).previousHash);
        System.out.println("New Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).hash);
        System.out.println("My wallet balance: "+ Blockchain.walletBalance(myPubKey));
        System.out.println("B wallet balance: "+ Blockchain.walletBalance(bobPubKey)+"\n");


        System.out.println("....1st Transaction: A wants to send 40 coins to B....\n>>>>  Sufficient balance and valid signature  <<<<\n");
        System.out.println("Sender provides the transaction details and signature, From Address, To Address and Amount.");
        Transaction newTransaction1 = new Transaction(publicKeyMyself,publicKeyBob,40);
        System.out.println("Sender uses signECDSA to sign the transaction string");
        String signature1 = signECDSA(privateKeyMyself, newTransaction1.txnOutputString);
        System.out.println("Signature: "+ signature1+"\n");
        System.out.println("  Validation check on the signature and balance and add to pending transaction list");
        Transaction.addToPendingTransaction(newTransaction1,signature1, privateKeyMyself);

        System.out.println("....2nd Transaction: A wants to send Another 20 coins to B....\n>>>>  Insufficient balance but valid signature  <<<<\n");
        System.out.println("Sender provides the transaction details and signature, From Address, To Address and Amount.");
        Transaction newTransaction2 = new Transaction(publicKeyMyself,publicKeyBob,20);
        System.out.println("Sender uses signECDSA to sign the transaction string");
        String signature2 = signECDSA(privateKeyMyself, newTransaction2.txnOutputString);
        System.out.println("Signature: "+ signature2+"\n");
        System.out.println("  Validation check on the signature and balance and add to pending transaction list");
        Transaction.addToPendingTransaction(newTransaction2,signature2, privateKeyMyself);

        System.out.println("....3rd Transaction: B wants to use my coin and his private key to send 20 coins to himself....\n>>>>  Invalid signature  <<<<\n");
        System.out.println("Sender provides the transaction details and signature, From Address, To Address and Amount.");
        Transaction newTransaction3 = new Transaction(publicKeyMyself,publicKeyBob,20);
        System.out.println("Sender uses signECDSA to sign the transaction string");
        String signature3 = signECDSA(privateKeyBob, newTransaction3.txnOutputString);
        System.out.println("Signature: "+ signature3+"\n");
        System.out.println("  Validation check on the signature and balance and add to pending transaction list");
        Transaction.addToPendingTransaction(newTransaction3,signature3, privateKeyBob);


        System.out.println("....4th Transaction: A wants to send B another 5 coins ....\n>>>>  Sufficient balance and valid signature  <<<<\n");
        System.out.println("Sender provides the transaction details and signature, From Address, To Address and Amount.");
        Transaction newTransaction4 = new Transaction(publicKeyMyself,publicKeyBob,5);
        System.out.println("Sender uses signECDSA to sign the transaction string");
        String signature4 = signECDSA(privateKeyMyself, newTransaction4.txnOutputString);
        System.out.println("Signature: "+ signature4+"\n");
        System.out.println("  Validation check on the signature and balance and add to pending transaction list");
        Transaction.addToPendingTransaction(newTransaction4,signature4, privateKeyMyself);

        System.out.println(".....Data before mining of the second block.....\n");
        System.out.println("No. of balances in the Public Ledger: "+ Blockchain.ledger.size());
        System.out.println("No. of Pending Transactions to be minded: "+ Blockchain.pendingTransaction.size());
        System.out.println("current block height is "+ Blockchain.chain.size()+"\n");

        System.out.println(".....Mining of the second block.....\n");
        System.out.println("addNewBlock method will first calculate the Merkle Root Hash for the coinbase transaction + pending transactions");
        Blockchain.chain.add(Blockchain.addNewBlock(privateKeyMyself,publicKeyMyself));
        System.out.println("\n"+"Block Minded and current block height is " + Blockchain.chain.size());
        System.out.println("Previous Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).previousHash);
        System.out.println("New Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).hash);
        System.out.println("No. of Pending Transactions: "+ Blockchain.pendingTransaction.size());
        System.out.println("My wallet balance: "+ Blockchain.walletBalance(myPubKey));
        System.out.println("Bob wallet balance: "+ Blockchain.walletBalance(bobPubKey)+"\n");

        System.out.println("....Transaction 5: B wants to send 40 coins to me....\n");
        System.out.println("Sender provides the transaction details and signature, From Address, To Address and Amount.");
        Transaction newTransaction5 = new Transaction(publicKeyBob,publicKeyMyself,40);
        System.out.println("Sender uses signECDSA to sign the transaction string");
        String signature5 = signECDSA(privateKeyBob, newTransaction5.txnOutputString);
        System.out.println("Signature: "+ signature5+"\n");
        System.out.println("  Validation check on the signature and balance and add to pending transaction list");
        Transaction.addToPendingTransaction(newTransaction5,signature5, privateKeyBob);

        System.out.println(".....Data before mining of the third block.....\n");
        System.out.println("No. of balances in the Public Ledger: "+ Blockchain.ledger.size());
        System.out.println("No. of Pending Transactions to be minded: "+ Blockchain.pendingTransaction.size());
        System.out.println("current block height is "+ Blockchain.chain.size()+"\n");

        System.out.println(".....Mining of the third block <<B to be miner this time>>.....\n");
        System.out.println("addNewBlock method will first calculate the Merkle Root Hash for the coinbase transaction + pending transactions");
        Blockchain.chain.add(Blockchain.addNewBlock(privateKeyBob, publicKeyBob));
        System.out.println("\n"+"Block Minded and current block height is " + Blockchain.chain.size());
        System.out.println("Previous Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).previousHash);
        System.out.println("New Hash: " + Blockchain.chain.get((Blockchain.chain.size()-1)).hash);
        System.out.println("No. of Pending Transactions: "+ Blockchain.pendingTransaction.size());
        System.out.println("My wallet balance: "+ Blockchain.walletBalance(myPubKey));
        System.out.println("Bob wallet balance: "+ Blockchain.walletBalance(bobPubKey)+"\n");


        System.out.println(".....Show whether the hash values along the chains of different blocks are linked.....\n");
        Blockchain.isChainValid();


        System.out.println(".....Show in case the hash values do not match with previous block, it will be flagged.....\n");
        Blockchain.chain.get(1).hash = "Someone tampered the chain";
        Blockchain.isChainValid();

    }


}
