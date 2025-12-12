package com.epam.infrastructure.security.port.in;

public interface PasswordManagementUseCase {

	void changePassword(String username, String oldPassword, String newPassword);

}
