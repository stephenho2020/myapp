package com.BlockchainProject;

import sun.security.mscapi.CPublicKey;

import javax.xml.bind.DatatypeConverter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.BlockchainProject.ECDSAUtils.signECDSA;
import static com.BlockchainProject.ECDSAUtils.verifyECDSA;

public class Transaction {
    public String fromAddress;
    public String toAddress;
    public float amount;
    public String txnOutputString;
    public String hashTxnOutputString;
    private PublicKey fromPK;
    private PublicKey toPK;
    public String signature = "";
    public ArrayList<TxIn> txIns = new ArrayList<TxIn>();
    public ArrayList<TxOut> txOuts = new ArrayList<TxOut>();

    public Transaction(PublicKey fromPK, PublicKey toPK, float amount) {
        this.fromPK = fromPK;
        this.toPK = toPK;
        this.fromAddress = pkToString(fromPK);
        this.toAddress = pkToString(toPK);
        this.amount = amount;
        this.txnOutputString = fromAddress + toAddress + Float.toString(amount);
        this.hashTxnOutputString = calculateHash();

    }
    public Transaction(PublicKey toPK, float amount) {
        this.fromAddress = "";
        this.toAddress = pkToString(toPK);

        this.amount = amount;
        this.txnOutputString = fromAddress + toAddress + Float.toString(amount);
        this.hashTxnOutputString = calculateHash();

    }

    public Transaction(PublicKey fromPK, PublicKey toPK, float amount, TxIn txIn) {
        this.fromPK = fromPK;
        this.toPK = toPK;
        this.fromAddress = pkToString(fromPK);
        this.toAddress = pkToString(toPK);
        this.amount = amount;
        this.txIns.add(txIn);
        this.txnOutputString = txIn.txOudId + txIn.txOutIndex + toAddress + Float.toString(amount);
        this.hashTxnOutputString = calculateHash();
    }


    private static boolean validateTransaction(Transaction newTransaction, String signature){

        boolean verifySignature =  verifyECDSA(newTransaction.fromPK,signature,newTransaction.txnOutputString);

        if (verifySignature==true)
            System.out.println("  Signature verification: PASSED");
        else
            System.out.println("  Signature verification: FAILED");


        float balance = 0;
        boolean sufficient_balance = false;
        for (Map.Entry i : Blockchain.ledger.entrySet()){
            if (i.getKey().equals(newTransaction.fromAddress)){
                balance = balance + (float) i.getValue();
            }
        }
        for (Map.Entry i : Blockchain.pendingPayment.entrySet()){
            if (i.getKey().equals(newTransaction.fromAddress)){
                balance = balance + (float) i.getValue();
            }
        }
        if (balance >= newTransaction.amount)
            sufficient_balance = true;

        if (sufficient_balance==true)
            System.out.println("  Available balance verification: PASSED");
        else
            System.out.println("  Available balance verification: FAILED");
        return (verifySignature && sufficient_balance);
    }

    public static void addToPendingTransaction(Transaction newTransaction, String signature,
                                               PrivateKey privateKey){
        newTransaction.sendFunds(privateKey);

        if (validateTransaction(newTransaction, signature)) {
            if (Blockchain.pendingPayment.containsKey(newTransaction.fromAddress)){
                float originalValue = Blockchain.pendingPayment.get(newTransaction.fromAddress);
                Blockchain.pendingPayment.put(newTransaction.fromAddress, originalValue-newTransaction.amount);
            }else Blockchain.pendingPayment.put(newTransaction.fromAddress, -newTransaction.amount);

            if (Blockchain.pendingReceipt.containsKey(newTransaction.toAddress)){
                float originalValue = Blockchain.pendingReceipt.get(newTransaction.toAddress);
                Blockchain.pendingReceipt.put(newTransaction.toAddress, originalValue+newTransaction.amount);
            }else Blockchain.pendingReceipt.put(newTransaction.toAddress, +newTransaction.amount);

            Blockchain.pendingTransaction.add(newTransaction.hashTxnOutputString);

            Blockchain.pendingTXList.add(newTransaction);
            newTransaction.signature = signature;


            System.out.println("  Result: GOOD TRANSACTION, added to pending list"+"\n");
        }
        else
            System.out.println("  Result: BAD TRANSACTION, no action taken"+"\n");

    }


    public void sendFunds(PrivateKey privateKey)
    {
        HashMap<String, UTXO> tmpUTXOs = new HashMap<String, UTXO>();
        HashMap<String, UTXO> pendingUTXOs = new HashMap<String, UTXO>();

        float total = 0;
        tmpUTXOs = Blockchain.getTxOutByAddress(Blockchain.gPendUTXOs, this.fromAddress);
        boolean hasTxOut = false;
        for (Map.Entry<String, UTXO> item: tmpUTXOs.entrySet())
        {
            UTXO tmpUTXO = new UTXO();
            hasTxOut = false;
            String TXID = item.getKey();
            ArrayList<TxOut> tmpTxOuts = item.getValue().txOuts;
            if (!tmpTxOuts.isEmpty())
            {
                hasTxOut = true;
            }
            for (int i = 0; i< tmpTxOuts.size(); i++){

                total += tmpTxOuts.get(i).amount;
                String signature=signECDSA(privateKey, tmpTxOuts.get(i).txOutId);
                tmpUTXO.moveTxIn2UTXO(new TxIn(TXID, tmpTxOuts.get(i).txOutIndex,
                        tmpTxOuts.get(i).address, tmpTxOuts.get(i).amount, signature));
                if (total >= this.amount)
                    break;
            }
            if (hasTxOut)
            {
                pendingUTXOs.put(TXID, tmpUTXO);
            }
            if (total >= this.amount)
                break;
        }



        UTXO tmpUTXO = new UTXO();
        Blockchain.addTxInFromUTXOs2UTXO(pendingUTXOs, tmpUTXO);

        TxOut output = new TxOut(this.toPK, this.amount);
        tmpUTXO.addNewTxOut(output);

        float leftover = Blockchain.getAmountFromUTXO(pendingUTXOs) - this.amount;
        if (leftover > 0){
            output = new TxOut(this.fromPK, leftover);
            tmpUTXO.addNewTxOut(output);

        }
        tmpUTXO.id = tmpUTXO.getTransactionID();
        tmpUTXO.syncTransactionID();

        if(!processTransaction(pendingUTXOs, tmpUTXO)){
            System.out.println("[!][Transaction] FAIL to proceed current transaction");
            return;
        }

        Blockchain.add2PendRemoveUTXOs(pendingUTXOs);
        UTXO tmpPendAddUTXO = new UTXO();
        tmpPendAddUTXO.moveUTXOfromUTXO(tmpUTXO);
        Blockchain.gPendAddUTXOs.put(tmpPendAddUTXO.id, tmpPendAddUTXO);
        Blockchain.gPendUTXOs.put(tmpUTXO.id, tmpUTXO);
        System.out.println("[o][Transaction]----------------------------UTXOs pending to add to block ----------------------------");
        Blockchain.printAllGlocalUTXOs(Blockchain.gPendAddUTXOs);
        System.out.println("[o][Transaction]----------------------------UTXOs pending to remove in ledger----------------------------");
        Blockchain.printAllOutputUTXOs(Blockchain.gPendRemoveUTXOs);
        System.out.println("[o][Transaction]----------------------------UTXOs supposed in ledger----------------------------");
        Blockchain.printAllOutputUTXOs(Blockchain.gPendUTXOs);
    }

    public boolean processTransaction(HashMap<String, UTXO> pendingUTXOs, UTXO tmpUTXO){

        if (pendingUTXOs.isEmpty()){
            System.out.println("[!][Transaction]no available UTXO can be used" +
                    " with address: "+this.fromAddress);
            return false;
        }

        if (Blockchain.getAmountFromUTXO(pendingUTXOs)<Blockchain.minimumTransaction){
            System.out.println("[!][Transaction]transaction inputs too small: "+Blockchain.getAmountFromUTXO(pendingUTXOs));
            return false;
        }

        for (int i = 0; i<tmpUTXO.txIns.size(); i++){
            if(tmpUTXO.txIns.get(i).verifySignature()){
                System.out.println("[o][Transaction]signature successfully verified ");
                System.out.println("[o][Transaction]TXID "+tmpUTXO.txIns.get(i).txOudId);
            }else
            {
                System.out.println("[!][Transaction]FAIL to verify signature");
                System.out.println("[!][Transaction]TXID "+tmpUTXO.txIns.get(i).txOudId);
                return false;
            }
        }

        if (Blockchain.getUTXOsBalance(Blockchain.gPendUTXOs, this.fromAddress) < this.amount)
        {
            System.out.println("[!][Transaction]Not enough amount to proceed transaction");
            return false;
        }

        return true;
    }


    public String calculateHash(String txnOutputString){

        return HashUtils.getHashForStr(txnOutputString);
    }

    public String calculateHash(){

        return HashUtils.getHashForStr(this.txnOutputString);
    }

    public String pkToString(PublicKey pk){
        return DatatypeConverter.printHexBinary(pk.getEncoded());
    }


}





