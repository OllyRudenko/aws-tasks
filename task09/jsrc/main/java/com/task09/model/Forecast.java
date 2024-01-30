package com.task09.model;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Forecast {
    private Double elevation;
    private Double generationtime_ms;
    private Map<String, List> hourly; // "temperature_2m": [number], "time": [str]
    private Map<String, String> hourly_units; // "temperature_2m": str, "time": str
    private Double latitude;
    private Double longitude;
    private String timezone;
    private String timezone_abbreviation;
    private Long utc_offset_seconds;

    public Forecast() {
    }

    public Double getElevation() {
        return elevation;
    }

    public void setElevation(Double elevation) {
        this.elevation = elevation;
    }

    public Double getGenerationtime_ms() {
        return generationtime_ms;
    }

    public void setGenerationtime_ms(Double generationtime_ms) {
        this.generationtime_ms = generationtime_ms;
    }

    public Map<String, List> getHourly() {
        return hourly;
    }

    public void setHourly(Map<String, List> hourly) {
        this.hourly = hourly;
    }

    public Map<String, String> getHourly_units() {
        return hourly_units;
    }

    public void setHourly_units(Map<String, String> hourly_units) {
        this.hourly_units = hourly_units;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone_abbreviation() {
        return timezone_abbreviation;
    }

    public void setTimezone_abbreviation(String timezone_abbreviation) {
        this.timezone_abbreviation = timezone_abbreviation;
    }

    public Long getUtc_offset_seconds() {
        return utc_offset_seconds;
    }

    public void setUtc_offset_seconds(Long utc_offset_seconds) {
        this.utc_offset_seconds = utc_offset_seconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Forecast)) return false;
        Forecast forecast = (Forecast) o;
        return Objects.equals(getElevation(), forecast.getElevation()) && Objects.equals(getGenerationtime_ms(), forecast.getGenerationtime_ms()) && Objects.equals(getHourly(), forecast.getHourly()) && Objects.equals(getHourly_units(), forecast.getHourly_units()) && Objects.equals(getLatitude(), forecast.getLatitude()) && Objects.equals(getLongitude(), forecast.getLongitude()) && Objects.equals(getTimezone(), forecast.getTimezone()) && Objects.equals(getTimezone_abbreviation(), forecast.getTimezone_abbreviation()) && Objects.equals(getUtc_offset_seconds(), forecast.getUtc_offset_seconds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getElevation(), getGenerationtime_ms(), getHourly(), getHourly_units(), getLatitude(), getLongitude(), getTimezone(), getTimezone_abbreviation(), getUtc_offset_seconds());
    }

    @Override
    public String toString() {
        return "Forecast{" +
                "elevation=" + elevation +
                ", generationtime_ms=" + generationtime_ms +
                ", hourly=" + hourly +
                ", hourly_units=" + hourly_units +
                ", latitude=" + latitude +
                ", longutude=" + longitude +
                ", timezone='" + timezone + '\'' +
                ", timezone_abbreviation='" + timezone_abbreviation + '\'' +
                ", utc_offset_seconds=" + utc_offset_seconds +
                '}';
    }
}
