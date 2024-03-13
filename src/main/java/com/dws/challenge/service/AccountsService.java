package com.dws.challenge.service;

import com.dws.challenge.domain.Account;
import com.dws.challenge.domain.PaymentTransfer;
import com.dws.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  
  @Getter
  private final EmailNotificationService emailNotificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
	this.emailNotificationService = new EmailNotificationService();
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  public void initiateTransfer(PaymentTransfer paymentTransferRequest) {

      Boolean isPaymentTransferSuccessful=this.accountsRepository.initiateTransfer(paymentTransferRequest);
      if(isPaymentTransferSuccessful) {
    	  emailNotificationService.notifyAboutTransfer(getAccount(paymentTransferRequest.getAccountFrom()), paymentTransferRequest.getAmount()+" has been debited from your account");
    	  emailNotificationService.notifyAboutTransfer(getAccount(paymentTransferRequest.getAccountTo()), paymentTransferRequest.getAmount()+" has been credited to your account");
      }
  }
}
