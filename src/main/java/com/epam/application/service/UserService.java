package com.epam.application.service;

import com.epam.application.Credentials;
import com.epam.application.request.types.CreateProfileRequest;
import com.epam.application.request.types.UpdateProfileRequest;
import java.util.Optional;

public interface UserService<T, C extends CreateProfileRequest, U extends UpdateProfileRequest> {

	T createProfile(C request);

	T updateProfile(U request);

	void updatePassword(Credentials credentials, String newPassword);

	void toggleActiveStatus(Credentials credentials);

	void deleteProfile(Credentials credentials);

	Optional<T> findProfileByUsername(Credentials credentials);

}