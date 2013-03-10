package com.simonbirt.util.chain.example;

import com.simonbirt.util.chain.Chain;
import com.simonbirt.util.chain.IdentityStep;
import com.simonbirt.util.chain.Transform;
import com.simonbirt.util.chain.example.stubs.Permission;
import com.simonbirt.util.chain.example.stubs.PermissionService;
import com.simonbirt.util.chain.example.stubs.SessionProvider;
import com.simonbirt.util.chain.example.stubs.User;
import com.simonbirt.util.chain.example.stubs.ValidationResult;
import com.simonbirt.util.chain.example.stubs.Validator;

public class Steps {
	
	public static <T, R> IdentityStep<T, R> validate(final Validator<T> validator, final Transform<ValidationResult, R> transform) {
		return new IdentityStep<T, R>() {
			@Override
			public R process(T input, Chain<T, R> controller) {
				ValidationResult v = validator.validate(input);
				if (!v.isValid()) {
					return transform.transform(v);
				}
				return controller.process(input);
			}
		};
	}
	
	public static <T,R> IdentityStep<T, R> checkPermissions(final SessionProvider sessions, final PermissionService permissionService, final Transform<Permission, R> transform, final Permission... permissions) {
		return new IdentityStep<T, R>() {
			@Override
			public R process(T input, Chain<T, R> controller) {
				User user = sessions.getSession().getUser();
				for (Permission permission : permissions) {
					if (!permissionService.isAllowed(user, permission)) {
						return transform.transform(permission);
					}
				}
				return controller.process(input);
			}
		};
	}

}
