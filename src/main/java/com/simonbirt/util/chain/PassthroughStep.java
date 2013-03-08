package com.simonbirt.util.chain;

public abstract class PassthroughStep<T, R> implements Step<T,T,R>{

	public R process(T input, Chain<T, R> remainingChain) {		
		FlowController<R> passthrough = new FlowController<R>();
		process(input, passthrough);
		if (passthrough.response == null){
			return remainingChain.process(input);
		} else {
			return passthrough.response;
		}
			
	}
	
	public abstract void process(T input, FlowController<R> passthrough);

	public class FlowController<RESPONSE>{
		private RESPONSE response;
		
		public void terminate(RESPONSE response){
			this.response = response;
		}
	}
	
}
