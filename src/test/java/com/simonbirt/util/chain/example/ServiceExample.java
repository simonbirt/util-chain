package com.simonbirt.util.chain.example;

import static com.simonbirt.util.chain.Chain.chainFor;

import com.simonbirt.util.chain.Chain;
import com.simonbirt.util.chain.IdentityStep;
import com.simonbirt.util.chain.Step;
import com.simonbirt.util.chain.example.stubs.Permission;
import com.simonbirt.util.chain.example.stubs.PermissionService;
import com.simonbirt.util.chain.example.stubs.Trade;
import com.simonbirt.util.chain.example.stubs.TradeDao;
import com.simonbirt.util.chain.example.stubs.TradePermissionError;
import com.simonbirt.util.chain.example.stubs.TradeResponse;
import com.simonbirt.util.chain.example.stubs.TradeValidationError;
import com.simonbirt.util.chain.example.stubs.UISessionSource;
import com.simonbirt.util.chain.example.stubs.User;
import com.simonbirt.util.chain.example.stubs.ValidationResult;
import com.simonbirt.util.chain.example.stubs.Validator;

public class ServiceExample {

	private Validator<Trade> validator;
	private UISessionSource sessions;
	protected TradeDao dao;
	private PermissionService permissionService;


	public TradeResponse saveTrade(Trade trade) {
		return chainFor(validate())
				.append(this.<Trade>checkPermissions(Permission.WRITE))
				.append(saveTrade())
				.append(returnResponse())
				.build().process(trade);
	}

	public TradeResponse getTradeById(Long tradeId) {
		return chainFor(Long.class, TradeResponse.class)
				.append(this.<Long>checkPermissions(Permission.READ))
				.append(lookupTrade())
				.append(returnResponse())
				.build().process(tradeId);
	}

	private IdentityStep<Trade, TradeResponse> returnResponse() {
		return new IdentityStep<Trade, TradeResponse>() {
			@Override
			public TradeResponse process(Trade input,Chain<Trade, TradeResponse> controller) {
				return new TradeResponse(input);
			}
		};
	}

	private Step<Trade,Trade,TradeResponse> saveTrade() {
		return new Step<Trade,Trade,TradeResponse>() {
			@Override
			public TradeResponse process(Trade input, Chain<Trade, TradeResponse> controller) {
				return controller.process(dao.saveTrade(input));
			}
		};
	}

	private Step<Long, Trade, TradeResponse> lookupTrade() {
		return new Step<Long, Trade, TradeResponse>() {
			@Override
			public TradeResponse process(Long id, Chain<Trade, TradeResponse> controller) {
				return controller.process(dao.getTradeById(id));
			}
		};
	}

	private IdentityStep<Trade, TradeResponse> validate() {
		return new IdentityStep<Trade, TradeResponse>() {
			@Override
			public TradeResponse process(Trade input,
					Chain<Trade, TradeResponse> controller) {
				ValidationResult v = validator.validate(input);
				if (!v.isValid()) {
					return new TradeResponse(new TradeValidationError(v.getMessage()));
				}
				return controller.process(input);
			}
		};
	}

	private <T> Step<T,T,TradeResponse> checkPermissions(final Permission... permissions) {
		return new Step<T,T,TradeResponse>() {
			@Override
			public TradeResponse process(T input, Chain<T, TradeResponse> controller) {
				User user = sessions.getSession().getUser();
				for (Permission permission : permissions) {
					if (!permissionService.isAllowed(user, permission)) {
						return new TradeResponse(
								new TradePermissionError(permission.getFailureMessage()));
					}
				}
				return controller.process(input);
			}
		};
	}

}
