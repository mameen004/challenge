package com.dws.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.stereotype.Repository;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.PaymentTransfer;
import com.dws.challenge.exception.DuplicateAccountIdException;
import com.dws.challenge.exception.PaymentTransferException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

	@Override
	public Boolean initiateTransfer(PaymentTransfer paymentTransferRequest) throws IllegalArgumentException {

		final Account firstAccount = accounts.get(paymentTransferRequest.getAccountFrom());
		final Account secondAccount = accounts.get(paymentTransferRequest.getAccountTo());
		final BigDecimal amount = paymentTransferRequest.getAmount();

		Callable<Boolean> transfer = () -> {
			return depositAmount(firstAccount, secondAccount, amount);
		};
		ExecutorService executor = Executors.newFixedThreadPool(3);
		Future<Boolean> future = executor.submit(transfer);

		try {
			return future.get();
		} catch (Exception exception) {
			throw new PaymentTransferException(exception.getMessage());
		}

	}

	private boolean depositAmount(Account firstAccount, Account secondAccount, BigDecimal depositAmount)
			throws IllegalArgumentException {

		Account former;
		Account latter;

		if (firstAccount.compareTo(secondAccount) < 0) {
			former = firstAccount;
			latter = secondAccount;
		} else {
			former = secondAccount;
			latter = firstAccount;
		}

		synchronized (former) {
			synchronized (latter) {
				if (depositAmount.compareTo(firstAccount.getBalance()) == 1) {
					throw new IllegalArgumentException("we do not support overdrafts!");
				}
				secondAccount.setBalance(secondAccount.getBalance().add(depositAmount));
				firstAccount.setBalance(firstAccount.getBalance().subtract(depositAmount));
				return true;
			}
		}

	}

}
