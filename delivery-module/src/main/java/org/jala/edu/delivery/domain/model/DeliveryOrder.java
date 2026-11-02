package org.jala.edu.delivery.domain.model;

public class DeliveryOrder {
  private String id;
  private String courierId;
  private boolean assigned;

  public DeliveryOrder(String id, String courierId) {
    this.id = id;
    this.courierId = courierId;
    this.assigned = false;
  }

  public boolean isAssigned() {
    return assigned;
  }

  public void setAssigned(boolean assigned) {
    this.assigned = assigned;
  }

  public void assignToCourier(String courierId) {
    this.courierId = courierId;
    this.assigned = true;
  }
}