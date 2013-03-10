package com.simonbirt.util.chain;

public interface Transform<INPUT, OUTPUT> {
	public OUTPUT transform(INPUT input);
}
