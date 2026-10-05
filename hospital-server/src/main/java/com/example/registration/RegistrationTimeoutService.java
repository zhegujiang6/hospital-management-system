package com.example.registration;

public interface RegistrationTimeoutService {

    void closeExpiredOrder(Long registrationOrderId);
}