package com.dws.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PaymentTransfer {

  @NotNull
  @NotEmpty
  private final String accountFrom;
  
  @NotNull
  @NotEmpty
  private final String accountTo;

  @NotNull
  @Min(value = 0, message = "we do not support overdrafts!)")
  private BigDecimal amount;


  @JsonCreator
  public PaymentTransfer(@JsonProperty("accountFrom") String accountFrom,
    @JsonProperty("accountTo") String accountTo,
    @JsonProperty("amount") BigDecimal amount) {
    this.accountFrom = accountFrom;
    this.accountTo = accountTo;
    this.amount = amount;
  }
}
