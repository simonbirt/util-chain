package com.simonbirt.util.chain.example;

public interface TradeDao {

	Trade saveTrade(Trade input);

	Trade getTradeById(Long id);

}
