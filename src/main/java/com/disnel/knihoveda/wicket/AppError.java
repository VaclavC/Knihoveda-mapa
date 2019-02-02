package com.disnel.knihoveda.wicket;

public class AppError extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public AppError()
	{
		super();
	}

	public AppError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AppError(String message, Throwable cause)
	{
		super(message, cause);
	}

	public AppError(String message)
	{
		super(message);
	}

	public AppError(Throwable cause)
	{
		super(cause);
	}
	
}
