package com.example.service;

public interface RegistrationTimeoutService {

    void closeExpiredOrder(Long registrationOrderId);
}