package com.BlockchainProject;
import javax.xml.bind.DatatypeConverter;
import java.security.PublicKey;

import static com.BlockchainProject.ECDSAUtils.verifyECDSA;


public class TxIn {
    //for requirement
    public String txOudId = "";
    public int txOutIndex = 0;
    public String signature = "";
    //for discrepancy
    public PublicKey address = null;
    public float amount = 0;

    public TxIn (String txOudId, int txOutIndex){
        this.txOudId = txOudId;
        this.txOutIndex = txOutIndex;
    }

    public TxIn (String txOudId, int txOutIndex, PublicKey address, float amount, String signature){
        this.txOudId = txOudId;
        this.txOutIndex = txOutIndex;
        this.address = address;
        this.amount = amount;
        this.signature = signature;
    }

    public boolean verifySignature(){
        boolean verifySignature = false;
        verifySignature = verifyECDSA(this.address,this.signature,this.txOudId);

        if (verifySignature)
            System.out.println("[o][TxIn]Signature verification: PASSED");
        else
            System.out.println("[!][TxIn]Signature verification: FAILED");

        return verifySignature;
    }


}
