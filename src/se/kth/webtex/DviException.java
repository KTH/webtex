package se.kth.webtex;

import javax.servlet.ServletException;

public class DviException extends ServletException {
	private static final long serialVersionUID = 1L;

	public DviException(String string) {
		super(string);
	}

	public DviException() {
		super();
	}
}
