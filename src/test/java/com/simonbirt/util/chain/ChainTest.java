package com.simonbirt.util.chain;
import static com.simonbirt.util.chain.Chain.first;
import static com.simonbirt.util.chain.Chain.forTypes;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.simonbirt.util.chain.Chain;
import com.simonbirt.util.chain.PassthroughStep;
import com.simonbirt.util.chain.Step;
import com.simonbirt.util.chain.UnterminatedChainException;

public class ChainTest {

	@Test
	public void canProcessOneStepChain() {
		Input input = new Input("Test");
		Response output = first(returnResponse(Input.class)).build()
				.process(input);
		assertThat(output.getString(), is("InputTest"));
	}
	
	@Test
	public void canProcessTwoStepChainWithInitialPassthrough() {
		Input input = new Input("Test");
		Response output = forTypes(Input.class, Response.class).then(passThrough()).then(returnResponse()).build().process(input);
		assertThat(output.getString(), is("InputTest"));
	}

	@Test
	public void canProcessTwoStepChain() {
		Input input = new Input("Test");
		Response output = first(continueProcessing())
				.then(returnResponse(Input.class)).build().process(input);
		assertThat(output.getString(), is("InputTest"));
	}

	@Test(expected = UnterminatedChainException.class)
	public void throwsOnUnterminatedChain() {
		Input input = new Input("Test");
		first(continueProcessing()).build().process(input);
	}

	@Test
	public void canProcessConvertingChain() {
		Input input = new Input("Test");
		Response output = first(continueProcessing()).then(convert())
				.then(returnResponse(Converted.class)).build().process(input);
		assertThat(output.getString(), is("ConvertedTest"));
	}

	private Step<Input, Input, Response> continueProcessing() {
		return new Step<Input, Input, Response>() {
			public Response process(Input input,
					Chain<Input, Response> remainingChain) {
				return remainingChain.process(input);
			}
		};
	}

	private Step<Input, Converted, Response> convert() {
		return new Step<Input, Converted, Response>() {
			public Response process(Input input,
					Chain<Converted, Response> remainingChain) {
				return remainingChain.process(new Converted(input));
			}
		};
	}

	private <T> Step<T, T, Response> returnResponse(Class<T> inputType) {
		return new Step<T, T, Response>() {
			public Response process(T input, Chain<T, Response> remainingChain) {
				return new Response(input.toString());
			}
		};
	}
	
	private <T> PassthroughStep<T, Response> returnResponse() {
		return new PassthroughStep<T, Response>() {
			@Override
			public void process(T input, FlowController<Response> passthrough) {
				passthrough.terminate(new Response(input.toString()));
			}
		};
	}
	
	private <T> PassthroughStep<T, Response> passThrough() {
		return new PassthroughStep<T, Response>() {
			@Override
			public void process(T input, FlowController<Response> passthrough) {
			}
		};
	}

	private class Input {

		private String string;

		public Input(String string) {
			this.string = string;
		}

		public String getString() {
			return string;
		}

		@Override
		public String toString() {
			return "Input" + string;
		}

	}

	private class Response {
		private String string;

		public Response(String input) {
			this.string = input;
		}

		public String getString() {
			return string;
		}

	}

	private class Converted {
		private String string;

		public Converted(Input input) {
			this.string = input.getString();
		}

		@Override
		public String toString() {
			return "Converted" + string;
		}
	}
}
