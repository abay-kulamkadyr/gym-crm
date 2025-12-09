package com.epam.application.service;

import com.epam.application.request.types.CreateProfileRequest;
import com.epam.application.request.types.UpdateProfileRequest;

import java.util.Optional;

public interface UserService<T, C extends CreateProfileRequest, U extends UpdateProfileRequest> {

	T createProfile(C request);

	T updateProfile(U request);

	void updatePassword(String username, String newPassword);

	void toggleActiveStatus(String username);

	void deleteProfile(String username);

	T getProfileByUsername(String username);

}