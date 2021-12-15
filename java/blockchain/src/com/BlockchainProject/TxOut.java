package com.BlockchainProject;

import java.security.PublicKey;

public class TxOut {
    //for requirement
    public PublicKey address = null;
    public float amount = 0;
    public int txOutIndex = 0;
    public String txOutId = "";

    public TxOut (PublicKey address, float amount){
        this.address = address;
        this.amount = amount;
    }
}
