package uk.ac.ed.inf;

/**
 * An enum that defines order outcomes
 */
public enum OrderOutcome {
    Delivered,
    ValidButNotDelivered,
    InvalidCardNumber,
    InvalidExpiryDate,
    InvalidCvv,
    InvalidTotal,
    InvalidPizzaNotDefined,
    InvalidPizzaCount,
    InvalidPizzaCombinationMultipleSuppliers,
    Invalid
}
