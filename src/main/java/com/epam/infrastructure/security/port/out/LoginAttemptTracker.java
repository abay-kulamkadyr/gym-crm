package com.epam.infrastructure.security.port.out;

import com.epam.infrastructure.security.core.LockoutInfo;

public interface LoginAttemptTracker {

    void recordFailedAttempt(String username);

    void clearAttempts(String username);

    LockoutInfo getLockoutInfo(String username);

    boolean isAccountLocked(String username);
}
