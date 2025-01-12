package org.example;

class Forecast {
    private final String date;
    private final String weather;
    private final double temperature;

    public Forecast(String date, String weather, double temperature) {
        this.date = date;
        this.weather = weather;
        this.temperature = temperature;
    }

    public String getDate() {
        return date;
    }

    public String getWeather() {
        return weather;
    }

    public double getTemperature() {
        return temperature;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Forecast forecast = (Forecast) obj;
        return this.date.equals(forecast.date);
    }
}
