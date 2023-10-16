package com.samasama.customer;

import com.samasama.clients.fraud.FraudCheckResponse;
import com.samasama.clients.fraud.FraudClient;
import com.samasama.clients.notification.NotificationClient;
import com.samasama.clients.notification.NotificationRequest;
import org.springframework.stereotype.Service;

@Service
public record CustomerService(
    CustomerRepository customerRepository,
    FraudClient fraudClient,
    NotificationClient notificationClient) {

  public void registerCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
    Customer customer =
        Customer.builder()
            .firstName(customerRegistrationRequest.firstName())
            .lastName(customerRegistrationRequest.lastName())
            .email(customerRegistrationRequest.email())
            .build();
    customerRepository.saveAndFlush(customer);
    FraudCheckResponse fraudCheckResponse = fraudClient.isFraudster(customer.getId());
    assert fraudCheckResponse != null;
    if (fraudCheckResponse.isFraudster()) {
      throw new IllegalStateException();
    }
    notificationClient.sendNotification(
        new NotificationRequest(
            customer.getId(),
            customer.getEmail(),
            String.format("Hi %s, welcome to samasama !", customer.getFirstName())));
  }
}
