package com.simonbirt.util.chain.example.stubs;

public interface BookDao {

	Book saveBook(Book input);

	Book getBookById(Long id);

}
