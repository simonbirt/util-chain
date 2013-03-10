package com.simonbirt.util.chain.example;

import static com.simonbirt.util.chain.Chain.chainOf;

import com.simonbirt.util.chain.Chain;
import com.simonbirt.util.chain.IdentityStep;
import com.simonbirt.util.chain.Step;
import com.simonbirt.util.chain.Transform;
import com.simonbirt.util.chain.example.stubs.Book;
import com.simonbirt.util.chain.example.stubs.BookDao;
import com.simonbirt.util.chain.example.stubs.BookPermissionError;
import com.simonbirt.util.chain.example.stubs.BookResponse;
import com.simonbirt.util.chain.example.stubs.BookValidationError;
import com.simonbirt.util.chain.example.stubs.Permission;
import com.simonbirt.util.chain.example.stubs.PermissionService;
import com.simonbirt.util.chain.example.stubs.SessionProvider;
import com.simonbirt.util.chain.example.stubs.ValidationResult;
import com.simonbirt.util.chain.example.stubs.Validator;

public class ServiceExample {

	private Validator<Book> validator;
	private SessionProvider sessions;
	private BookDao dao;
	private PermissionService permissionService;
		
	public BookResponse saveBook(Book book) {
		return chainOf(validate())
				.append(this.<Book>checkPermissions(Permission.WRITE))
				.append(saveBook())
				.append(returnResponse())
				.build().process(book);
	}


	public BookResponse getBookById(Long bookId) {
		return chainOf(this.<Long> checkPermissions(Permission.READ))
				.append(lookupBook())
				.append(returnResponse())
				.build().process(bookId);
	}
	
	private final IdentityStep<Book, BookResponse> validate(){
		return Steps.validate(validator, new Transform<ValidationResult, BookResponse>() {
			@Override
			public BookResponse transform(ValidationResult input) {
				return new BookResponse(new BookValidationError(input.getMessage()));
			}
		});
	}

	private <T> IdentityStep<T, BookResponse> checkPermissions(Permission... permissions) {
		return Steps.<T,BookResponse>checkPermissions(sessions,permissionService,new Transform<Permission, BookResponse>() {
			@Override
			public BookResponse transform(Permission input) {
				return new BookResponse(new BookPermissionError(input.getFailureMessage()));
			}
		}, permissions);
	}

	private IdentityStep<Book, BookResponse> returnResponse() {
		return new IdentityStep<Book, BookResponse>() {
			@Override
			public BookResponse process(Book input, Chain<Book, BookResponse> controller) {
				return new BookResponse(input);
			}
		};
	}

	private Step<Book, Book, BookResponse> saveBook() {
		return new Step<Book, Book, BookResponse>() {
			@Override
			public BookResponse process(Book input, Chain<Book, BookResponse> controller) {
				return controller.process(dao.saveBook(input));
			}
		};
	}

	private Step<Long, Book, BookResponse> lookupBook() {
		return new Step<Long, Book, BookResponse>() {
			@Override
			public BookResponse process(Long id, Chain<Book, BookResponse> controller) {
				return controller.process(dao.getBookById(id));
			}
		};
	}


}
