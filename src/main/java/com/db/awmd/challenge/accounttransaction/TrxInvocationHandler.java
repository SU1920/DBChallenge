package com.db.awmd.challenge.accounttransaction;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.math.BigDecimal;

public class TrxInvocationHandler<E> implements InvocationHandler {
	
	private final AccountsRepository accountsRepository;
	
	@Getter
	ThreadLocal<TrxContext<Account, Account>> localContext = new ThreadLocal<>();;
	
	TrxInvocationHandler(AccountsRepository accountsRepository){
		this.accountsRepository = accountsRepository;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// do something "dynamic"
		String methodName = method.getName();
		if(methodName.startsWith("get")) {
			Account account = accountsRepository.getAccount((String)args[0]);
			BigDecimal balanceCopy = BigDecimal.ZERO;
			Account proxyAccount = new Account(account.getAccountId(), balanceCopy.add(account.getBalance()));
			
			TrxContext<Account, Account> context = localContext.get();
			if(context != null) {
				context.getSavePoints().put(proxyAccount, account);
				return proxyAccount;
			}else {
				return account;
			}
		}
		
		return null;
	}
}