package com.task10.utils;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.models.Reservation;
import com.task10.models.Table;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConverterUtil {

    public static Map<String, Object> convertItem(Map<String, AttributeValue> item) {
        int id = Integer.parseInt(item.get("id").getN());
        int number = Integer.parseInt(item.get("number").getN());
        int places = Integer.parseInt(item.get("places").getN());
        boolean isVip = item.get("isVip").getBOOL();
        Integer minOrder = Integer.parseInt(item.get("minOrder").getN());

        Map<String, Object> table = new HashMap<>();
        table.put("id", id);
        table.put("number", number);
        table.put("place", places);
        table.put("isVip", isVip);
        table.put("minOrder", minOrder);

        return table;
    }

    public static Table convertItemToTable(Map<String, AttributeValue> item) {
        int id = Integer.parseInt(item.get("id").getN());
        int number = Integer.parseInt(item.get("number").getN());
        int places = Integer.parseInt(item.get("places").getN());
        boolean isVip = item.get("isVip").getBOOL();
        Integer minOrder = Integer.parseInt(item.get("minOrder").getN());

        Table table = new Table();
        table.setId(id);
        table.setNumber(number);
        table.setPlaces(places);
        table.setVip(isVip);
        table.setMinOrder(minOrder);

        return table;
    }

    public static Map<String, List<Table>> convertItems(List<Map<String, AttributeValue>> items) {
        List<Table> tableList = new ArrayList<>();
        for (Map<String, AttributeValue> item : items) {
            tableList.add(convertItemToTable(item));
        }
        Map<String, List<Table>> tables = new HashMap<>();
        tables.put("tables", tableList);
        return tables;
    }

    public static Map<String, Object> convertReservationItem(Map<String, AttributeValue> item) {
        int tableNumber = Integer.parseInt(item.get("tableNumber").getN());
        String clientName = item.get("clientName").getS();
        String phoneNumber = item.get("phoneNumber").getS();
        String date = item.get("date").getS();
        String slotTimeStart = item.get("slotTimeStart").getS();
        String slotTimeEnd = item.get("slotTimeEnd").getS();

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("tableNumber", tableNumber);
        reservation.put("clientName", clientName);
        reservation.put("phoneNumber", phoneNumber);
        reservation.put("date", date);
        reservation.put("slotTimeStart", slotTimeStart);
        reservation.put("slotTimeEnd", slotTimeEnd);

        return reservation;
    }

    public static Reservation convertItemToReservation(Map<String, AttributeValue> item) {
        int tableNumber = Integer.parseInt(item.get("tableNumber").getN());
        String clientName = item.get("clientName").getS();
        String phoneNumber = item.get("phoneNumber").getS();
        String date = item.get("date").getS();
        String slotTimeStart = item.get("slotTimeStart").getS();
        String slotTimeEnd = item.get("slotTimeEnd").getS();

        Reservation reservation = new Reservation();
        reservation.setTableNumber(tableNumber);
        reservation.setClientName(clientName);
        reservation.setPhoneNumber(phoneNumber);
        reservation.setDate(date);
        reservation.setSlotTimeStart(slotTimeStart);
        reservation.setSlotTimeEnd(slotTimeEnd);

        return reservation;
    }

    public static Map<String, List<Reservation>> convertReservationItems(List<Map<String, AttributeValue>> items) {
        List<Reservation> reservationList = new ArrayList<>();
        for (Map<String, AttributeValue> item : items) {
            reservationList.add(convertItemToReservation(item));
        }
        Map<String, List<Reservation>> reservations = new HashMap<>();
        reservations.put("reservations", reservationList);
        return reservations;
    }

    public static String convertResponseToJson(Map<String, Object> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertResponseWithListToJson(Map<String, List> response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
