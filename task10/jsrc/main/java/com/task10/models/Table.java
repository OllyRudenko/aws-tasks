package com.task10.models;

import java.util.Objects;

public class Table {
    private int id;
    private int number;
    private int places;
    private boolean isVip;
    private Integer minOrder;

    public Table() {
    }

    public Table(int id, int number, int places, boolean isVip, Integer minOrder) {
        this.id = id;
        this.number = number;
        this.places = places;
        this.isVip = isVip;
        this.minOrder = minOrder;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getPlaces() {
        return places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public Integer getMinOrder() {
        return minOrder;
    }

    public void setMinOrder(Integer minOrder) {
        this.minOrder = minOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table)) return false;
        Table table = (Table) o;
        return getId() == table.getId() && getNumber() == table.getNumber() && getPlaces() == table.getPlaces() && isVip() == table.isVip() && Objects.equals(getMinOrder(), table.getMinOrder());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getNumber(), getPlaces(), isVip(), getMinOrder());
    }

    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", number=" + number +
                ", places=" + places +
                ", isVip=" + isVip +
                ", minOrder=" + minOrder +
                '}';
    }
}
