package com.awstasks.task10.models;

import java.util.Objects;

public class Reservation {
    private int tableNumber;
    private String clientName;
    private String phoneNumber;
    private String date;
    private String slotTimeStart;
    private String slotTimeEnd;

    public Reservation() {
    }

    public Reservation(int tableNumber, String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
        this.tableNumber = tableNumber;
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
        this.date = date;
        this.slotTimeStart = slotTimeStart;
        this.slotTimeEnd = slotTimeEnd;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSlotTimeStart() {
        return slotTimeStart;
    }

    public void setSlotTimeStart(String slotTimeStart) {
        this.slotTimeStart = slotTimeStart;
    }

    public String getSlotTimeEnd() {
        return slotTimeEnd;
    }

    public void setSlotTimeEnd(String slotTimeEnd) {
        this.slotTimeEnd = slotTimeEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return getTableNumber() == that.getTableNumber() && Objects.equals(getClientName(), that.getClientName()) && Objects.equals(getPhoneNumber(), that.getPhoneNumber()) && Objects.equals(getDate(), that.getDate()) && Objects.equals(getSlotTimeStart(), that.getSlotTimeStart()) && Objects.equals(getSlotTimeEnd(), that.getSlotTimeEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTableNumber(), getClientName(), getPhoneNumber(), getDate(), getSlotTimeStart(), getSlotTimeEnd());
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "tableNumber=" + tableNumber +
                ", clientName='" + clientName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", date='" + date + '\'' +
                ", slotTimeStart='" + slotTimeStart + '\'' +
                ", slotTimeEnd='" + slotTimeEnd + '\'' +
                '}';
    }
}
