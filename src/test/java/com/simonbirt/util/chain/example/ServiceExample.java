package com.simonbirt.util.chain.example;

import static com.simonbirt.util.chain.Chain.chainOf;

import com.simonbirt.util.chain.Chain;
import com.simonbirt.util.chain.IdentityStep;
import com.simonbirt.util.chain.Step;
import com.simonbirt.util.chain.example.stubs.Book;
import com.simonbirt.util.chain.example.stubs.BookDao;
import com.simonbirt.util.chain.example.stubs.BookPermissionError;
import com.simonbirt.util.chain.example.stubs.BookResponse;
import com.simonbirt.util.chain.example.stubs.BookValidationError;
import com.simonbirt.util.chain.example.stubs.Permission;
import com.simonbirt.util.chain.example.stubs.PermissionService;
import com.simonbirt.util.chain.example.stubs.SessionProvider;
import com.simonbirt.util.chain.example.stubs.User;
import com.simonbirt.util.chain.example.stubs.ValidationResult;
import com.simonbirt.util.chain.example.stubs.Validator;

public class ServiceExample {

	private Validator<Book> validator;
	private SessionProvider sessions;
	protected BookDao dao;
	private PermissionService permissionService;


	public BookResponse saveBook(Book book) {
		return chainOf(validate())
				.append(this.<Book>checkPermissions(Permission.WRITE))
				.append(saveBook())
				.append(returnResponse())
				.build().process(book);
	}

	public BookResponse getBookById(Long bookId) {
		return chainOf(this.<Long>checkPermissions(Permission.READ))
				.append(lookupBook())
				.append(returnResponse())
				.build().process(bookId);
	}

	private IdentityStep<Book, BookResponse> returnResponse() {
		return new IdentityStep<Book, BookResponse>() {
			@Override
			public BookResponse process(Book input,Chain<Book, BookResponse> controller) {
				return new BookResponse(input);
			}
		};
	}

	private Step<Book,Book,BookResponse> saveBook() {
		return new Step<Book,Book,BookResponse>() {
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

	private IdentityStep<Book, BookResponse> validate() {
		return new IdentityStep<Book, BookResponse>() {
			@Override
			public BookResponse process(Book input,
					Chain<Book, BookResponse> controller) {
				ValidationResult v = validator.validate(input);
				if (!v.isValid()) {
					return new BookResponse(new BookValidationError(v.getMessage()));
				}
				return controller.process(input);
			}
		};
	}

	private <T> Step<T,T,BookResponse> checkPermissions(final Permission... permissions) {
		return new Step<T,T,BookResponse>() {
			@Override
			public BookResponse process(T input, Chain<T, BookResponse> controller) {
				User user = sessions.getSession().getUser();
				for (Permission permission : permissions) {
					if (!permissionService.isAllowed(user, permission)) {
						return new BookResponse(
								new BookPermissionError(permission.getFailureMessage()));
					}
				}
				return controller.process(input);
			}
		};
	}

}
