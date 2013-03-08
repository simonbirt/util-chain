package com.simonbirt.util.chain;

public interface Step<IN, OUT, RESPONSE>{
	public RESPONSE process(IN input, Chain<OUT, RESPONSE> remainingChain);
}