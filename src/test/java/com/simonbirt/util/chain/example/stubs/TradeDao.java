package com.simonbirt.util.chain.example.stubs;

public interface TradeDao {

	Trade saveTrade(Trade input);

	Trade getTradeById(Long id);

}
