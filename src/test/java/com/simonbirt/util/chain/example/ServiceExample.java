package com.simonbirt.util.chain.example;

import static com.simonbirt.util.chain.Chain.first;
import static com.simonbirt.util.chain.Chain.forTypes;

import com.simonbirt.util.chain.Chain;
import com.simonbirt.util.chain.PassthroughStep;
import com.simonbirt.util.chain.Step;

public class ServiceExample {

	private Validator<Trade> validator;
	private UISessionSource sessions;
	protected TradeDao dao;
	private PermissionService permissionService;

	public static interface TradeStep extends Step<Trade, Trade, TradeResponse> {
	}

	public TradeResponse saveTrade(Trade trade) {
		return first(validate())
				.then(checkPermissions(Permission.WRITE))
				.then(saveTrade())
				.then(returnResponse())
				.build().process(trade);
	}

	public TradeResponse getTradeById(Long tradeId) {
		return forTypes(Long.class, TradeResponse.class)
				.then(checkPermissions(Permission.READ))
				.then(lookupTrade())
				.then(returnResponse())
				.build().process(tradeId);
	}

	public TradeStep returnResponse() {
		return new TradeStep() {
			public TradeResponse process(Trade input, Chain<Trade, TradeResponse> remainingChain) {
				return new TradeResponse(input);
			}
		};
	}

	public TradeStep saveTrade() {
		return new TradeStep() {
			public TradeResponse process(Trade input, Chain<Trade, TradeResponse> remainingChain) {
				return remainingChain.process(dao.saveTrade(input));
			}
		};
	}

	public Step<Long, Trade, TradeResponse> lookupTrade() {
		return new Step<Long, Trade, TradeResponse>() {
			public TradeResponse process(Long id, Chain<Trade, TradeResponse> remainingChain) {
				return remainingChain.process(dao.getTradeById(id));
			}
		};
	}

	private TradeStep validate() {
		return new TradeStep() {
			public TradeResponse process(Trade input, Chain<Trade, TradeResponse> remainingChain) {
				ValidationResult v = validator.validate(input);
				if (v.isValid()) {
					return remainingChain.process(input);
				} else {
					return new TradeResponse(new TradeValidationError(v.getMessage()));
				}
			}
		};
	}

	private <T> PassthroughStep<T, TradeResponse> checkPermissions(final Permission... permissions) {
		return new PassthroughStep<T, TradeResponse>() {
			@Override
			public void process(T input, FlowController<TradeResponse> passthrough) {
				User user = sessions.getSession().getUser();
				for (Permission permission : permissions) {
					if (!permissionService.isAllowed(user, permission)) {
						passthrough.terminate(new TradeResponse(
								new TradePermissionError(permission.getFailureMessage())));
					}
				}
			}
		};
	}

}
