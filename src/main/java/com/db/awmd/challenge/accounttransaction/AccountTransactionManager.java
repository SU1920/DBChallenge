package com.db.awmd.challenge.accounttransaction;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.lang.reflect.Proxy;
import java.util.Map;

public class AccountTransactionManager {

	private final AccountsRepository accountsRepository;
	
	private TrxInvocationHandler<Account> handler;
	
	@Getter
	private boolean autoCommit = false;
	
	@Getter
	private AccountsRepository repoProxy;
	
	public AccountTransactionManager(AccountsRepository repository){
		this.accountsRepository = repository;
		
		handler = new TrxInvocationHandler<Account>(accountsRepository);
		repoProxy = (AccountsRepository)Proxy.newProxyInstance(AccountsRepository.class.getClassLoader()
				, new Class[] { AccountsRepository.class }, handler);
		
	}
	
	public void doInTransaction(TrxCallback callback) {
		TrxContext<Account, Account> context = new TrxContext<>();
		ThreadLocal<TrxContext<Account, Account>> localContext = handler.getLocalContext();
		localContext.set(context);
		try {
			callback.process();
			if(autoCommit) {
				commit();
			}
		}catch(Exception e) {
			rollBack();
			throw e;
		}finally {
			
		}
		
	}
	
	
	public void commit() {
		TrxContext<Account, Account> localContext = handler.getLocalContext().get();
		Map<Account, Account> savePoints = localContext.getSavePoints();
		// swap save points value to repository 
		savePoints.entrySet().forEach(entry -> {
			Account key = entry.getKey();
			Account value = entry.getValue();
			value.setBalance(key.getBalance());
		});
	}
	
	public void rollBack() {
		// Destroy Save points within same transactional context
		TrxContext<Account, Account> localContext = handler.getLocalContext().get();
		localContext.getSavePoints().clear();
	}
}
