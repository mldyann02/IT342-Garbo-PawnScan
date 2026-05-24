package edu.cit.garbo.pawnscan.features.verification.publicapi;

public interface PublicStolenItemClient {

    PublicStolenItemMatch searchBySerial(String serial);
}
