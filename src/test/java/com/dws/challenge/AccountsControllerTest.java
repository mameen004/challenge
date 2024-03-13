package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import com.dws.challenge.service.EmailNotificationService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;
  
  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  @Test
  void initiateTransfer() throws Exception {
	
	// Account Creation 
	// Account From(Balance) = 200.00
	// Account To(Balance) = 150.00
    String accountFromId = System.currentTimeMillis()+"456";
    Account accountFrom = new Account(accountFromId, new BigDecimal("200.00"));
    String accountToId = System.currentTimeMillis()+"789";
    Account accountTo = new Account(accountToId, new BigDecimal("150.00"));
    
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);
        
    // Initiate Payment Transfer
    // Transfer Amount = 100.00
    this.mockMvc.perform(post("/v1/accounts/transfers/payment").contentType(MediaType.APPLICATION_JSON)
        .content("{\"accountFrom\":\""+accountFromId+"\",\"accountTo\":\""+accountToId+"\",\"amount\":100}"))
      .andExpect(status().isCreated());
    
    // Validate Transfer Status
    // Account From(New Balance) = 200.00 - 100.00 = 100.00
    // Account To(New Balance) = 150.00 + 100.00 = 250.00
    this.mockMvc.perform(get("/v1/accounts/" + accountFromId))
    .andExpect(status().isOk())
    .andExpect(
      content().string("{\"accountId\":\"" + accountFromId + "\",\"balance\":100.00}"));
    
    this.mockMvc.perform(get("/v1/accounts/" + accountToId))
    .andExpect(status().isOk())
    .andExpect(
      content().string("{\"accountId\":\"" + accountToId + "\",\"balance\":250.00}"));
  }
  
  @Test
  void initiateTransferNegativeAmount() throws Exception {
	
	// Account Creation 
	// Account From(Balance) = 200.00
	// Account To(Balance) = 150.00
    String accountFromId = System.currentTimeMillis()+"456";
    Account accountFrom = new Account(accountFromId, new BigDecimal("200.00"));
    String accountToId = System.currentTimeMillis()+"789";
    Account accountTo = new Account(accountToId, new BigDecimal("150.00"));
    
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);
    
    // Initiate Payment Transfer
    // Transfer Amount = -100.00
    // Bad Request
    this.mockMvc.perform(post("/v1/accounts/transfers/payment").contentType(MediaType.APPLICATION_JSON)
        .content("{\"accountFrom\":\""+accountFromId+"\",\"accountTo\":\""+accountToId+"\",\"amount\":-100}"))
      .andExpect(status().isBadRequest());
    
  }
  
  @Test
  void initiateTransferOverDraftScenario() throws Exception {
	
	// Account Creation 
	// Account From(Balance) = 200.00
	// Account To(Balance) = 150.00
    String accountFromId = System.currentTimeMillis()+"456";
    Account accountFrom = new Account(accountFromId, new BigDecimal("200.00"));
    String accountToId = System.currentTimeMillis()+"789";
    Account accountTo = new Account(accountToId, new BigDecimal("150.00"));
    
    this.accountsService.createAccount(accountFrom);
    this.accountsService.createAccount(accountTo);
    
    // Initiate Payment Transfer
    // Transfer Amount = 1000.00
    // Over Draft Scenario
    this.mockMvc.perform(post("/v1/accounts/transfers/payment").contentType(MediaType.APPLICATION_JSON)
        .content("{\"accountFrom\":\""+accountFromId+"\",\"accountTo\":\""+accountToId+"\",\"amount\":1000}"))
      .andExpect(status().isBadRequest());
    
  }
}
