package shop.s5g.auth.exception;

public class PaycoGetMemberInfoFailedException extends RuntimeException {

    public PaycoGetMemberInfoFailedException(String message) {
        super(message);
    }

    public PaycoGetMemberInfoFailedException() {
        super("payco get member info failed");
    }
}
