package com.github.sirmonkeyboy.loan.Utils;

import java.util.UUID;

public class LoanData {

    private final UUID requesterUUID;

    private final double loanAmount;

    private final double payBackAmount;

    public LoanData(UUID requesterUUUID, double loanAmount, double payBackAmount) {
        this.requesterUUID = requesterUUUID;
        this.loanAmount = loanAmount;
        this.payBackAmount = payBackAmount;
    }

    public UUID getRequesterUUID() {
        return requesterUUID;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public double getPayBackAmount() {
        return payBackAmount;
    }
}
