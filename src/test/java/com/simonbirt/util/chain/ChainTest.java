package com.simonbirt.util.chain;
import static com.simonbirt.util.chain.Chain.chainOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ChainTest {

	@Test
	public void canProcessOneStepChain() {
		Input input = new Input("Test");
		Response output = chainOf(returnResponse(Input.class)).build()
				.process(input);
		assertThat(output.getString(), is("InputTest"));
	}
	
	@Test
	public void canProcessTwoStepChainWithInitialPassthrough() {
		Input input = new Input("Test");
		Response output = chainOf(Input.class, Response.class).append(passThrough()).append(returnResponse()).build().process(input);
		assertThat(output.getString(), is("InputTest"));
	}

	@Test
	public void canProcessTwoStepChain() {
		Input input = new Input("Test");
		Response output = chainOf(continueProcessing())
				.append(returnResponse(Input.class)).build().process(input);
		assertThat(output.getString(), is("InputTest"));
	}

	@Test(expected = UnterminatedChainException.class)
	public void throwsOnUnterminatedChain() {
		Input input = new Input("Test");
		chainOf(continueProcessing()).build().process(input);
	}

	@Test
	public void canProcessConvertingChain() {
		Input input = new Input("Test");
		Response output = chainOf(continueProcessing()).append(convert())
				.append(returnResponse(Converted.class)).build().process(input);
		assertThat(output.getString(), is("ConvertedTest"));
	}

	private Step<Input, Input, Response> continueProcessing() {
		return new Step<Input, Input, Response>() {
			public Response process(Input input,
					Chain<Input, Response> controller) {
				return controller.process(input);
			}
		};
	}

	private Step<Input, Converted, Response> convert() {
		return new Step<Input, Converted, Response>() {
			public Response process(Input input,
					Chain<Converted, Response> controller) {
				return controller.process(new Converted(input));
			}
		};
	}

	private <T> Step<T, T, Response> returnResponse(Class<T> inputType) {
		return new Step<T, T, Response>() {
			public Response process(T input, Chain<T, Response> controller) {
				return new Response(input.toString());
			}
		};
	}
	
	private IdentityStep<Input, Response> returnResponse() {
		return new IdentityStep<Input, Response>() {
			@Override
			public Response process(Input input, Chain<Input, Response> controller) {
				return new Response(input.toString());
			}
		};
	}
	
	private IdentityStep<Input, Response> passThrough() {
		return new IdentityStep<Input, Response>() {
			@Override
			public Response process(Input input, Chain<Input, Response> controller) {
				return controller.process(input);
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
