package hr.yeti.uhttp.internal;

import hr.yeti.uhttp.Operation;

import java.util.regex.Pattern;

public class Lookup {

    private Pattern signature;
    private Operation operation;
    private int errorCode;

    public Lookup(Pattern signature, Operation operation, int errorCode) {
        this.signature = signature;
        this.operation = operation;
        this.errorCode = errorCode;
    }

    public Pattern getSignature() {
        return signature;
    }

    public Operation getOperation() {
        return operation;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
