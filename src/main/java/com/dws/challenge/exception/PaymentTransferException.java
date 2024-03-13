package com.dws.challenge.exception;

public class PaymentTransferException extends RuntimeException {

private static final long serialVersionUID = 1L;

public PaymentTransferException(String message) {
    super(message);
  }
}
