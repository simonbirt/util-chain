package com.simonbirt.util.chain.example.stubs;

public interface Validator<T> {
	public ValidationResult validate(T input);
}
