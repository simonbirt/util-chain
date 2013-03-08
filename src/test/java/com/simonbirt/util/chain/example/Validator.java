package com.simonbirt.util.chain.example;

public interface Validator<T> {
	public ValidationResult validate(T input);
}
