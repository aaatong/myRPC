package org.example.rpc.protocol;

public class RPCResponse {
    private String roundID;
    private Object result;
    private ResponseCode code;

    public enum ResponseCode {
        SUCCESS,
        FAIL,
        METHOD_NOT_FOUND,
        CLASS_NOT_FOUND
    }

    public String getRoundID() {
        return roundID;
    }

    public void setRoundID(String roundID) {
        this.roundID = roundID;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }
}
