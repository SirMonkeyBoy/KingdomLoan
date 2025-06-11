package com.github.sirmonkeyboy.kingdomloan.Utils;

import java.sql.Timestamp;
import java.util.UUID;

public class LoanHistoryData {

    private final UUID playerUUID;

    private final String NameOfLenderOrBorrower;

    private final double loanAmount;

    private final Timestamp loanStartDate;

    private final Timestamp loanEndDate;

    public LoanHistoryData(UUID playerUUID, String NameOfLenderOrBorrower, double loanAmount, Timestamp loanStartDate, Timestamp loanEndDate) {
        this.playerUUID = playerUUID;
        this.NameOfLenderOrBorrower = NameOfLenderOrBorrower;
        this.loanAmount = loanAmount;
        this.loanStartDate = loanStartDate;
        this.loanEndDate = loanEndDate;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getNameOfLenderOrBorrower() {
        return NameOfLenderOrBorrower;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public Timestamp getLoanStartDate() {
        return loanStartDate;
    }

    public Timestamp getLoanEndDate() {
        return loanEndDate;
    }
}