package shop.s5g.auth.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(String message) {
        super(message);
    }
    public MemberNotFoundException() {
        super("member not found");
    }
}
