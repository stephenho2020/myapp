package com.BlockchainProject;

import javax.xml.bind.DatatypeConverter;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.BlockchainProject.ECDSAUtils.signECDSA;

public class Blockchain {

    public static ArrayList<Block> chain = new ArrayList<>();
    public static ArrayList<String> pendingTransaction = new ArrayList<>();
    public static ArrayList<Transaction> pendingTXList = new ArrayList<Transaction>();

    public static HashMap<String, Float> ledger = new HashMap<>();
    public static HashMap<String, Float> pendingPayment = new HashMap<>();
    public static HashMap<String, Float> pendingReceipt = new HashMap<>();
    public static int reward = 50;
    private static int difficulty;
    public static HashMap<String, UTXO> gUTXOs = new HashMap<String, UTXO>();
    public static HashMap<String, UTXO> gPendUTXOs = new HashMap<>();
    public static HashMap<String, UTXO> gPendAddUTXOs = new HashMap<>();
    public static HashMap<String, UTXO> gPendRemoveUTXOs = new HashMap<>();
    public static float minimumTransaction = 0.1f;

    public static Block createGenesisBlock(PrivateKey privateKeyMyself, PublicKey publicKeyMyself) {
        Transaction coinbase = new Transaction(publicKeyMyself, reward);
        ledger.put(coinbase.toAddress, coinbase.amount);

        System.out.println("[o][BlockChain]To Address: "+coinbase.toAddress);
        TxOut output = new TxOut(publicKeyMyself, coinbase.amount);
        UTXO coinbaseUTXO = Blockchain.generateUTXOBy1TxOut(output, privateKeyMyself, publicKeyMyself);
        String merkleRoot = coinbaseUTXO.id;

        long Timestamp = System.currentTimeMillis() / 1000;
        String PreviousHash = "This is Genesis Block";
        int currentDifficulty = calculateDifficulty();
        String requiredPrefix = new String(new char[currentDifficulty]).replace('\0', '0');
        int nonce = 0;
        Block proposedBlock;
        while (true) {
            proposedBlock = new Block(0, Timestamp, merkleRoot, PreviousHash, currentDifficulty, nonce);
            if (proposedBlock.hash.startsWith(requiredPrefix))
                break;
            nonce++;
        }
        for (int i = 0; i<pendingTXList.size(); i++){
            proposedBlock.transactions.add(pendingTXList.get(i));
        }

        Blockchain.putUTXO2UTXOs(gPendUTXOs, coinbaseUTXO);
        Blockchain.putUTXO2UTXOs(gPendAddUTXOs, coinbaseUTXO);
        gUTXOs = gPendUTXOs;

        System.out.println("[o][Blockchain]----------------------------UTXOs added to block----------------------------");
        Blockchain.printAllGlocalUTXOs(gPendAddUTXOs);
        System.out.println("[o][Blockchain]----------------------------all UTXOs in ledger----------------------------");
        Blockchain.printAllOutputUTXOs(gUTXOs);
        Blockchain.addUTXOs2Block(gPendAddUTXOs, proposedBlock);
        pendingTXList.clear();
        gPendAddUTXOs.clear();
        return proposedBlock;
    }


    public static Block addNewBlock(PrivateKey privateKeyMyself, PublicKey publicKeyMyself){
        Transaction coinbase = new Transaction(publicKeyMyself, reward);
        pendingTransaction.add(coinbase.hashTxnOutputString);
        System.out.println("[o][BlockChain]To Address: "+coinbase.toAddress);
        TxOut output = new TxOut(publicKeyMyself, coinbase.amount);
        UTXO coinbaseUTXO = Blockchain.generateUTXOBy1TxOut(output, privateKeyMyself, publicKeyMyself);
        Blockchain.putUTXO2UTXOs(gPendUTXOs, coinbaseUTXO);
        Blockchain.putUTXO2UTXOs(gPendAddUTXOs, coinbaseUTXO);

        int newIndex = chain.size();
        long newTimestamp = System.currentTimeMillis() / 1000;
        String newMerkleRoot = calculateMerkleRoot(gPendAddUTXOs);

        String newPreviousHash = chain.get(chain.size()-1).hash;
        int currentDifficulty = calculateDifficulty();
        String requiredPrefix = new String(new char[currentDifficulty]).replace('\0', '0');
        int newNonce = 0;
        Block proposedBlock;
        while (true){
            proposedBlock = new Block(newIndex,newTimestamp,newMerkleRoot,newPreviousHash,currentDifficulty,newNonce);
            if (proposedBlock.hash.startsWith(requiredPrefix))
                break;
            newNonce++;
        }

        if (ledger.containsKey(coinbase.toAddress)){
            float originalValue = ledger.get(coinbase.toAddress);
            ledger.put(coinbase.toAddress, originalValue+coinbase.amount);
        }else ledger.put(coinbase.toAddress, coinbase.amount);

        for (Map.Entry i : pendingPayment.entrySet()){
            String key = (String) i.getKey();
            if (ledger.containsKey(key)){
                float originalValue = ledger.get(key);
                ledger.put((String) i.getKey(), originalValue+(float) i.getValue());
            }else ledger.put((String) i.getKey(),(float) i.getValue());

        }
        for (Map.Entry i : pendingReceipt.entrySet()){
            String key = (String) i.getKey();
            if (ledger.containsKey(key)){
                float originalValue = ledger.get(key);
                ledger.put((String) i.getKey(), originalValue+(float) i.getValue());
            }else ledger.put((String) i.getKey(),(float) i.getValue());

        }
        for (int i = 0; i<pendingTXList.size(); i++){
            proposedBlock.transactions.add(pendingTXList.get(i));
        }

        Blockchain.addUTXOs2Block(gPendAddUTXOs, proposedBlock);
        gUTXOs = gPendUTXOs;

        System.out.println("[o][Blockchain]----------------------------UTXOs added to a new block----------------------------:");
        Blockchain.printAllGlocalUTXOs(gPendAddUTXOs);
        System.out.println("[o][Blockchain]----------------------------UTXOs removed in ledger----------------------------:");
        Blockchain.printAllOutputUTXOs(gPendRemoveUTXOs);
        System.out.println("[o][Blockchain]----------------------------all UTXOs in ledger----------------------------");
        Blockchain.printAllOutputUTXOs(gUTXOs);
        gPendAddUTXOs.clear();
        gPendRemoveUTXOs.clear();
        pendingPayment.clear();
        pendingTransaction.clear();
        pendingReceipt.clear();
        pendingTXList.clear();
        return proposedBlock;

    }

    public static float getAmountFromUTXO(HashMap<String, UTXO> tmpUTXOs){
        float total = 0;
        for (Map.Entry<String, UTXO> item: tmpUTXOs.entrySet()){
            ArrayList<TxIn> txIns = item.getValue().txIns;
            for (int i = 0; i< txIns.size(); i++) {
                TxIn UTXO = txIns.get(i);
                total += UTXO.amount;
            }

        }
        return total;
    }

    public static void addTxInFromUTXOs2UTXO(HashMap<String, UTXO> pendingUTXOs, UTXO tmpUTXO){
        for (Map.Entry<String, UTXO> item: pendingUTXOs.entrySet()){
            ArrayList<TxIn> txIns = item.getValue().txIns;
            for (int i =0; i<txIns.size(); i++){
                tmpUTXO.txIns.add(new TxIn(txIns.get(i).txOudId, txIns.get(i).txOutIndex,
                        txIns.get(i).address, txIns.get(i).amount, txIns.get(i).signature));
            }
        }
    }

    public static void add2PendRemoveUTXOs(HashMap<String, UTXO> pendingUTXOs){
        for (Map.Entry<String, UTXO> item: pendingUTXOs.entrySet())
        {
            ArrayList<TxIn> txIns = item.getValue().txIns;
            for (int i = 0; i< txIns.size(); i++){
                String TXID = txIns.get(i).txOudId;
                int index = txIns.get(i).txOutIndex;

                Blockchain.addTxOut2PendRemoveUTXOs(Blockchain.gPendUTXOs, TXID, index);
                Blockchain.removeTxOutByTXIDIndex(Blockchain.gPendUTXOs, TXID, index);
            }
        }
    }

    public static void putUTXO2UTXOs(HashMap<String, UTXO> UTXOs, UTXO tmpUTXO){
        if (UTXOs.containsKey(tmpUTXO.id)){
            for (int i = 0; i< tmpUTXO.txOuts.size(); i++){
                UTXOs.get(tmpUTXO.id).addNewTxOut(tmpUTXO.txOuts.get(i));
            }
        }else{
            UTXOs.put(tmpUTXO.id, tmpUTXO);
        }
    }

    public static void addUTXOs2Block(HashMap<String, UTXO> tmpUTXO, Block tmpBlock){
        for (Map.Entry<String, UTXO> item : tmpUTXO.entrySet()){
            tmpBlock.UTXOs.add(item.getValue());
        }
    }
    public static UTXO generateUTXOBy1TxOut(TxOut output, PrivateKey privateKey, PublicKey publicKey)
    {
        UTXO tmpUTXO = new UTXO();
        tmpUTXO.addNewTxOut(output);
        tmpUTXO.id = tmpUTXO.getTransactionID();
        tmpUTXO.syncTransactionID();
        String signature =signECDSA(privateKey, tmpUTXO.id);
        tmpUTXO.syncAddress(publicKey);
        return tmpUTXO;
    }

    public static float getUTXOsBalance(HashMap<String, UTXO> tmpUTXOs, String fromaddress){
        float total = 0;
        for (Map.Entry<String, UTXO> item: tmpUTXOs.entrySet()){
            ArrayList<TxOut> txOuts = item.getValue().txOuts;
            for (int i = 0; i< txOuts.size(); i++){
                TxOut UTXO = txOuts.get(i);
                if(pkToString(UTXO.address).equals(fromaddress)){
                    total += UTXO.amount;
                }
            }
        }
        return total;
    }

    public static HashMap<String, UTXO> getTxOutByAddress(HashMap<String, UTXO> UTXOs, String fromaddress){
        HashMap<String, UTXO> tmpUTXOs = new HashMap<String, UTXO>();
        boolean hasTxOut = false;
        for (Map.Entry<String, UTXO> item: UTXOs.entrySet()){
            hasTxOut = false;
            String TXID = item.getKey();
            UTXO tmpUTXO = new UTXO();
            tmpUTXO.id = TXID;
            ArrayList<TxOut> txOuts = item.getValue().txOuts;
            hasTxOut = tmpUTXO.moveTxOut2UTXO(txOuts, fromaddress);
            if (hasTxOut)
                tmpUTXOs.put(TXID, tmpUTXO);

        }
        return tmpUTXOs;
    }

    public static float walletBalance(String pk){
        float balance = 0;
        for (Map.Entry i : ledger.entrySet()){
            if (i.getKey().equals(pk)){
                balance = balance + (float) i.getValue();
            }
        }
        return balance;
    }

    public static void isChainValid(){
        for (int i = 0; i < chain.size()-1; i++){
            Block previousBlock = chain.get(i);
            Block currentBlock = chain.get(i+1);

            boolean validChain = true;
            if (!chain.get(i+1).previousHash.equals(chain.get(i).hash)){
                validChain = false;
            }
            else if (i == 0 && !(previousBlock.calculateHash().equals(previousBlock.hash)))
            {
                validChain = false;
            }
            else if (!currentBlock.calculateHash().equals(currentBlock.hash))
            {
                validChain = false;
            }

            for (int j = 0; j <currentBlock.UTXOs.size(); j++){
                UTXO currentUTXO  = currentBlock.UTXOs.get(j);
                if (!gUTXOs.containsKey(currentUTXO.id)){
                    System.out.println("[!][Blockchain]invalid transaction id checked: ");
                    System.out.println("[!][Blockchain]TXID: "+currentUTXO.id);
                    validChain = false;
                    continue;
                }
                for (int k = 0; k< currentUTXO.txOuts.size();k++){
                    if (!currentUTXO.txOuts.contains(k)){
                        continue;
                    }
                    if (!currentUTXO.txIns.get(k).verifySignature()){
                        System.out.println("[!][Blockchain]invalid transaction checked: ");
                        System.out.println("[!][Blockchain]TXID: "+currentUTXO.txIns.get(k).txOudId);
                        System.out.println("[!][Blockchain]index: "+currentUTXO.txIns.get(k).txOutIndex);
                        validChain = false;
                    }
                }
            }


            System.out.println("Is Chain Valid: "+ validChain);
            System.out.println("Block "+ (i+1)+" hash         : "+ chain.get(i).hash);
            System.out.println("Block "+ (i+2)+" previous hash: "+ chain.get(i+1).previousHash + "\n");

        }

    }

    public static void printAllGlocalUTXOs(HashMap<String, UTXO> tmpUTXOs)
    {
        int count = 1;
        for (Map.Entry<String, UTXO> item: tmpUTXOs.entrySet()){

            String TXID = item.getKey();
            ArrayList<TxIn> inputs = item.getValue().txIns;
            System.out.println("[o][Blockchain]----UTXO"+count+"----");
            for (int i = 0; i< inputs.size();i++){
                System.out.println("[o][BlockChain][UTXOs][*INPUT*] ");
                System.out.println("[o][BlockChain][UTXOs]previous TXID: "+inputs.get(i).txOudId);
                System.out.println("[o][BlockChain][UTXOs]previous txOutIndex: "+inputs.get(i).txOutIndex);
                System.out.println("[o][BlockChain][UTXOs]signature: "+inputs.get(i).signature);
                System.out.println("[o][BlockChain][UTXOs]from address: "+pkToString(inputs.get(i).address));
                System.out.println("[o][BlockChain][UTXOs]amount sent: "+inputs.get(i).amount);
                System.out.println("");
            }
            ArrayList<TxOut> outputs = item.getValue().txOuts;
            for (int i = 0; i< outputs.size();i++){
                System.out.println("[o][BlockChain][UTXOs][*OUTPUT*] ");
                System.out.println("[o][Blockchain]Block Height: "+chain.size());
                System.out.println("[o][BlockChain][UTXOs]current TXID: "+TXID);
                System.out.println("[o][BlockChain][UTXOs]current txOutIndex: "+outputs.get(i).txOutIndex);
                System.out.println("[o][BlockChain][UTXOs]to address: "+pkToString(outputs.get(i).address));
                System.out.println("[o][BlockChain][UTXOs]amount received: "+outputs.get(i).amount);
                System.out.println("");
            }
            count += 1;
        }
    }

    public static void printAllOutputUTXOs(HashMap<String, UTXO> tmpUTXOs)
    {
        int count = 1;
        for (Map.Entry<String, UTXO> item: tmpUTXOs.entrySet()){

            String TXID = item.getKey();
            System.out.println("[o][Blockchain]----UTXO"+count+"----");
            ArrayList<TxOut> outputs = item.getValue().txOuts;
            for (int i = 0; i< outputs.size();i++){
                System.out.println("[o][BlockChain][UTXOs][*OUTPUT*] ");
                System.out.println("[o][Blockchain]Block Height: "+chain.size());
                System.out.println("[o][BlockChain][UTXOs]TXID: "+TXID);
                System.out.println("[o][BlockChain][UTXOs]txOutIndex: "+outputs.get(i).txOutIndex);
                System.out.println("[o][BlockChain][UTXOs]address: "+pkToString(outputs.get(i).address));
                System.out.println("[o][BlockChain][UTXOs]amount: "+outputs.get(i).amount);
                System.out.println("");
            }
            count += 1;
        }
    }

    public static void removeTxOutByTXIDIndex(HashMap<String, UTXO> tmpUTXOs, String id, int txOutIndex){
        if (tmpUTXOs.containsKey(id)){

            ArrayList<TxOut> tmpTxOuts = tmpUTXOs.get(id).txOuts;
            if (tmpTxOuts.get(txOutIndex).txOutIndex==txOutIndex){
                tmpTxOuts.remove(txOutIndex);
            }
            else
            {
                System.out.println("[o][BlockChain]local parameter {txOutIndex} in Class TxOut" +
                        " which is in ArrayList<TxOut> with index [txOutIndex] " +
                        " is not equal to index [txOutIndex]");
            }
            tmpUTXOs.get(id).txOuts = tmpTxOuts;
        }
        else
        {
            System.out.println("[!][Blockchain]UTXO with TXID: "+id+"cannot be found");
        }

    }

    public static void addTxOut2PendRemoveUTXOs(HashMap<String, UTXO> tmpUTXOs, String id, int txOutIndex){
        if (tmpUTXOs.containsKey(id)){
            ArrayList<TxOut> outputs = tmpUTXOs.get(id).txOuts;
            if (outputs.get(txOutIndex).txOutIndex==txOutIndex){
                if (!Blockchain.gPendRemoveUTXOs.containsKey(id))
                {
                    UTXO tmpUTXO = new UTXO();
                    tmpUTXO.id = id;
                    tmpUTXO.txOuts.add(outputs.get(txOutIndex));
                    Blockchain.gPendRemoveUTXOs.put(tmpUTXO.id, tmpUTXO);
                }
                else
                {
                    Blockchain.gPendRemoveUTXOs.get(id).txOuts.add(txOutIndex, outputs.get(txOutIndex));
                }
            }
            else
            {
                System.out.println("[!][ERROR][BlockChain]local parameter {txOutIndex} in Class TxOut" +
                        " which is in ArrayList<TxOut> with index [txOutIndex] " +
                        " is not equal to index [txOutIndex]");
            }
        }
        else
        {
            System.out.println("[!][ERROR][BlockChain]UTXO with TXID: "+id+"cannot be found");
        }
    }

    private static String calculateMerkleRoot(HashMap<String, UTXO> tmpUTXOs){
        ArrayList<String> pendingTransaction = new ArrayList<>();
        for (Map.Entry<String, UTXO> item: tmpUTXOs.entrySet()){
            pendingTransaction.add(item.getKey());
        }
        MerkleTree merkleTrees = new MerkleTree(pendingTransaction);
        merkleTrees.generateMerkleTreeRoot();
        return merkleTrees.getRoot();
    }

    private static int calculateDifficulty(){
        int blockGenerationInterval = 3;
        int difficultAdjustmentInterval = 3;
        if (chain.size()<=difficultAdjustmentInterval-1){
            difficulty = 4;
        }
        if (chain.size()>difficultAdjustmentInterval-1 && (chain.size())%difficultAdjustmentInterval == 0 ){
            System.out.println("\n    Block generation expected time taken: " + blockGenerationInterval + " second(s)");
            System.out.println("    Actual time taken: "+ (chain.get(chain.size()-1).timestamp - chain.get(chain.size()-difficultAdjustmentInterval).timestamp) + " second(s)");
            if (chain.get(chain.size()-1).timestamp - chain.get(chain.size()-difficultAdjustmentInterval).timestamp<blockGenerationInterval){
                difficulty++;
                System.out.println("    Increase Difficulty by 1\n");
            }
            else{
                if (difficulty>3) {
                    System.out.println("    Decrease Difficulty by 1\n");
                    difficulty--;
                }
                else
                    System.out.println("    Difficulty already equal to floor level 3, i.e. No change\n");
            }
        }
        return difficulty;
    }

    public static String pkToString(PublicKey pk){
        return DatatypeConverter.printHexBinary(pk.getEncoded());
    }

}
