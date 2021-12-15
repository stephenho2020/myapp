package com.BlockchainProject;

import java.net.*;
import java.io.*;
 

public class NodeMachine {
    private String hostname;
    private int port;
    private String userName;
    ReadThread rt;
    WriteThread wt;

    public NodeMachine(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }


    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
 
            System.out.println("Connected.");

            rt = new ReadThread(socket, this);
            wt = new WriteThread(socket, this);

            rt.start();
            wt.start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
 
    }
 
    void setUserName(String userName) {
        this.userName = userName;
    }
 
    String getUserName() {
        return this.userName;
    }
 
 
    public static void main(String[] args) {
        if (args.length < 2) return;

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);
 
        NodeMachine client = new NodeMachine(hostname, port);
        client.execute();
    }

    public void bc(String msg) {
        wt.boardcast(msg);
    }


}