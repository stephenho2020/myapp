package com.BlockchainProject;
import java.util.ArrayList;
import java.util.List;

public class Block {


    public int index;
    public long timestamp;
    public String merkleRoot;
    public String previousHash;
    public int difficulty;
    public int nonce;
    public String hash;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();
    public ArrayList<UTXO> UTXOs = new ArrayList<>();


    public Block(int index, long timestamp, String merkleRoot, String previousHash, int difficulty, int nonce) {
        this.index = index;
        this.timestamp = timestamp;
        this.merkleRoot = merkleRoot;
        this.previousHash = previousHash;
        this.difficulty = difficulty;
        this.nonce = nonce;
        this.hash = calculateHash();

    }


    public String calculateHash(){
        return HashUtils.getHashForStr(this.index + this.timestamp+this.merkleRoot +this.previousHash + this.difficulty + this.nonce);
    }


}
