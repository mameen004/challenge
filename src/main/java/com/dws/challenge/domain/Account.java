package com.dws.challenge.domain;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Account implements Comparable<Account> {

	@NotNull
	@NotEmpty
	private final String accountId;

	@NotNull
	@Min(value = 0, message = "Initial balance must be positive.")
	private BigDecimal balance;

	@JsonIgnore
	private final Object lock;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
		this.lock = new Object();
	}

	@JsonCreator
	public Account(@JsonProperty("accountId") String accountId, @JsonProperty("balance") BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
		this.lock = new Object();
	}

	@Override
	public int compareTo(Account account) {
		return (Long.valueOf(this.accountId) > Long.valueOf(account.accountId)) ? 1
				: (Long.valueOf(this.accountId) < Long.valueOf(account.accountId)) ? -1 : 0;
	}

}
