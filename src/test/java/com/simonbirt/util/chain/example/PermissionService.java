package com.simonbirt.util.chain.example;

public interface PermissionService {

	boolean isAllowed(User user, Permission permission);

}
