package com.gmail.dleemcewen.tandemfieri.Entities;

public class Nonce {
    private String nonce;

    public Nonce(String nonce) {
        setNonce(nonce);
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}