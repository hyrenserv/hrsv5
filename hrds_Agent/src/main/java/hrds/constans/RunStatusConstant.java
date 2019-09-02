package hrds.constans;

public enum RunStatusConstant implements EnumConstantInterface {
    WAITING(1, "waiting"),RUNNING(2, "running"),COMPLETE(3, "compele"),
    SUCCEED(4, "succeed"),FAILED(0, "failed"),TERMINATED(-1, "terminated"),
    PAUSE(5, "pause");

    private final int code;
    private final String message;

    RunStatusConstant(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
