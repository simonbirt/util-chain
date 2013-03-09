package com.simonbirt.util.chain;

import java.util.LinkedList;
import java.util.List;

public class Chain<IN, RESPONSE> {
	private final List<Step> links;

	private Chain(List<Step> links) {
		this.links = links;
	}

	private Chain() {
		links = new LinkedList<Step>();
	}

	public RESPONSE process(IN input) {
		if (links.isEmpty()) {
			throw new UnterminatedChainException();
		}
		Step<IN, ?, RESPONSE> first = links.get(0);
		return first.process(input,tail());
	}

	private Chain tail() {
		return new Chain(links.subList(1, links.size()));
	}

	public static <IN, OUT, RESPONSE> ChainBuilder<IN, OUT, RESPONSE> chainFor(Step<IN, OUT, RESPONSE> link) {
		return new ChainBuilder<IN, OUT, RESPONSE>(link);
	}
	
	public static <IN, RESPONSE> ChainBuilder<IN, IN, RESPONSE> chainFor(Class<IN> inType, Class<RESPONSE> rType){
		return new ChainBuilder<IN, IN, RESPONSE>();
	}

	public static class ChainBuilder<IN, OUT, RESPONSE> {
		private final List<Step> links = new LinkedList<Step>();


		private ChainBuilder(){
			
		}
		
		public ChainBuilder(Step<IN, OUT, RESPONSE> link) {
			links.add(link);
		}

		public <NEWOUT> ChainBuilder<IN, NEWOUT, RESPONSE> append(Step<OUT, NEWOUT, RESPONSE> link) {
			links.add(link);
			return (ChainBuilder<IN, NEWOUT, RESPONSE>) this;
		}
		
		public ChainBuilder<IN, OUT, RESPONSE> append(IdentityStep<OUT,RESPONSE> link) {
			links.add(link);
			return (ChainBuilder<IN, OUT, RESPONSE>) this;
		}
		

		public Chain<IN, RESPONSE> build() {
			return new Chain<IN, RESPONSE>(links);
		}
	}
}