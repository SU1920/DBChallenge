package com.db.awmd.challenge.accounttransaction;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

public class TrxContext<K, V> {
	@Getter
	private Map<K, V> savePoints = new HashMap<>();	
}
