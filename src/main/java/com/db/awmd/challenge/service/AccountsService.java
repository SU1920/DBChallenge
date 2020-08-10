package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AmountTransferException;
import com.db.awmd.challenge.repository.AccountsRepository;
import com.db.awmd.challenge.accounttransaction.AccountTransactionManager;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  
  private AccountTransactionManager transactionManager;
  
  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
    this.transactionManager = new AccountTransactionManager(accountsRepository);
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }

	public List<Account> getAccounts() {
		return this.accountsRepository.getAccounts();
	}

  public void amountTransfer(final String fromAccount,	
		  final String toAccount, final BigDecimal transferAmount) throws AmountTransferException {
	  
	  transactionManager.doInTransaction(() ->{
		  
		  this.debit(fromAccount, transferAmount);
		  this.credit(toAccount, transferAmount);
	  });
	  
	  transactionManager.commit();
	  
  }
  
	private Account debit(String accountId, BigDecimal amount) throws AmountTransferException{
  		final Account account = transactionManager.getRepoProxy().getAccount(accountId);
		if(account == null) {
			throw new AmountTransferException("Account does not exist");
		}
		if(account.getBalance().compareTo(amount) == -1) {
			throw new AmountTransferException("Insufficient balance in account");
		}
		BigDecimal bal = account.getBalance().subtract(amount);
		account.setBalance(bal);
		return account;
	}
	
	private Account credit(String accountId, BigDecimal amount) throws AmountTransferException{
		final Account account = transactionManager.getRepoProxy().getAccount(accountId);
		if(account == null) {
			throw new AmountTransferException("Account does not exist");
		}
		BigDecimal bal = account.getBalance().add(amount);
		account.setBalance(bal);
		return account;
	}
}
