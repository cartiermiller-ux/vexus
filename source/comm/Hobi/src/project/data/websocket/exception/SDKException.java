package project.data.websocket.exception;

public class SDKException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static final String INPUT_ERROR = "InputError";

	public static final String RUNTIME_ERROR = "RuntimeError";

	public static final String ENV_ERROR = "EnvironmentError";

	public static final String EXEC_ERROR = "ExecuteError";

	public static final String KEY_MISSING = "KeyMissingError";

	public static final String SYS_ERROR = "SystemError";

	private final String errCode;

	public SDKException(String errCode, String errMsg) {
		super(errMsg);
		this.errCode = errCode;
	}

	public String getErrCode() {
		return errCode;
	}

}
