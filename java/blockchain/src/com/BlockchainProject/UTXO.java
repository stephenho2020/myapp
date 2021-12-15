package com.BlockchainProject;

import javax.xml.bind.DatatypeConverter;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UTXO {
    public String id = "";
    public ArrayList<TxIn> txIns = new ArrayList<TxIn>();
    public ArrayList<TxOut> txOuts = new ArrayList<TxOut>();

    public UTXO (){}

    public UTXO (String id, ArrayList<TxIn> txIns, ArrayList<TxOut> txOuts)
    {
        this.id = id;
        this.txIns = txIns;
        this.txOuts = txOuts;
    }

    // move TxOut to current UTXO class
    public boolean moveTxOut2UTXO(ArrayList<TxOut> txOuts, String fromaddress){
        boolean hasTxOut = false;
        for (int i = 0; i< txOuts.size(); i++){
            TxOut txOut = txOuts.get(i);
            if(pkToString(txOut.address).equals(fromaddress)){
                hasTxOut = true;
                this.txOuts.add(txOut);
            }
        }
        return hasTxOut;
    }

    public void moveTxIn2UTXO (TxIn txIn){
        txIns.add(txIn);
    }

    public void moveUTXOfromUTXO (UTXO tmpUTXO){
        this.id = tmpUTXO.id;
        for (int i=0; i<tmpUTXO.txIns.size();i++){
            this.moveTxIn2UTXO(tmpUTXO.txIns.get(i));
        }
        for (int i=0; i<tmpUTXO.txOuts.size();i++){
            this.addNewTxOut(tmpUTXO.txOuts.get(i));
        }
    }

    public void addNewTxOut(TxOut txOut){
        txOut.txOutIndex = this.txOuts.size();
        txOuts.add(txOut.txOutIndex, txOut);
//        sequence ++;
    }

    public String getTransactionID (){
        String txInContent = "";
        String txOutContent = "";
        for (int i = 0; i<txIns.size(); i++){
            String txOudId = txIns.get(i).txOudId;
            int txOutIndex = txIns.get(i).txOutIndex;
            txInContent = txInContent + txOudId + String.valueOf(txOutIndex);
        }
        System.out.println("[o][UTXO]txInContent: "+txInContent);

        for (int i = 0; i<txOuts.size(); i++){
            float amount = txOuts.get(i).amount;
            PublicKey address = txOuts.get(i).address;
            txOutContent = txOutContent + pkToString(address) + Float.toString(amount) ;
        }
        System.out.println("[o][UTXO]txOutContent: "+txOutContent);
        System.out.println("");
        return calculateHash(txInContent + txOutContent);
    }

    public String pkToString(PublicKey pk){
        return DatatypeConverter.printHexBinary(pk.getEncoded());
    }

    public void syncTransactionID(){
        for (int i = 0; i<txOuts.size(); i++){
            txOuts.get(i).txOutId = this.id;
        }
    }

    public void syncSignature(String signature){
        for (int i = 0; i<txIns.size(); i++){
            txIns.get(i).signature = signature;
        }
    }
    public void syncAddress(PublicKey address){
        for (int i = 0; i<txOuts.size(); i++){
            txOuts.get(i).address = address;
        }
    }

    public String calculateHash(String txnOutputString){

        return HashUtils.getHashForStr(txnOutputString);
    }

}
