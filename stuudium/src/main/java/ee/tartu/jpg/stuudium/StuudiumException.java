package ee.tartu.jpg.stuudium;

class StuudiumException extends Exception {

	private static final long serialVersionUID = 5441742252494160464L;

	public StuudiumException(String message) {
		super(message);
	}

	public StuudiumException(String message, Throwable t) {
		super(message, t);
	}
}