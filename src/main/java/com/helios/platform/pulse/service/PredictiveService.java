package com.helios.platform.pulse.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.helios.platform.pulse.dto.PredictiveResponse;
import com.helios.platform.pulse.repositories.ILoteRepo;
import com.helios.platform.pulse.repositories.ITransactionV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PredictiveService {

    private final ITransactionV2 transactionRepo;
    private final ILoteRepo loteRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${openweathermap.api.key}")
    private String weatherApiKey;

    private PredictiveResponse cachedResponse;
    private long lastCacheTime = 0;
    // 4 hours in milliseconds
    private static final long CACHE_DURATION_MS = 4L * 60 * 60 * 1000;

    public PredictiveService(ITransactionV2 transactionRepo, ILoteRepo loteRepo) {
        this.transactionRepo = transactionRepo;
        this.loteRepo = loteRepo;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public PredictiveResponse generateForecast() {
        long currentTime = System.currentTimeMillis();

        // Return cached response if it's within 4 hours
        // DESACTIVADO TEMPORALMENTE PARA PRUEBAS
        /*
        if (cachedResponse != null && (currentTime - lastCacheTime) < CACHE_DURATION_MS) {
            System.out.println("Returning predictive response from cache");
            return cachedResponse;
        }
        */

        System.out.println("Cache expired or empty. Generating new predictive forecast...");
        PredictiveResponse response = new PredictiveResponse();

        // 1. Historical & Recent Data
        int historicalExpected = calculateHistoricalAverage();
        int lastWeekendSales = getWeekendSales(1);
        int twoWeekendsAgoSales = getWeekendSales(2);

        // 2. Weather
        WeatherInfo weather = fetchWeather();
        response.setWeatherCondition(weather.condition);
        response.setRainProb(weather.rainProb);

        // 3. Holidays
        String holidayInfo = determineHoliday(LocalDate.now());
        response.setHolidayInfo(holidayInfo);

        // 4. Crisis Monitor (Google News RSS)
        String newsHeadlines = fetchCrisisNews();

        // 5. Calculate Expected
        double multiplier = 1.0;
        if (!holidayInfo.isEmpty()) multiplier += 0.6; // +60%
        if (weather.isRain && weather.rainProb > 50) multiplier -= 0.3; // -30%

        // Mezclamos un poco la tendencia reciente en la afluencia final
        int recentAverage = (lastWeekendSales + twoWeekendsAgoSales) / 2;
        if (recentAverage == 0) recentAverage = historicalExpected; // fallback si no hay data reciente

        int finalExpected = (int) (recentAverage * multiplier);
        if (finalExpected < 0) finalExpected = 0;
        response.setExpectedAttendance(finalExpected);

        // 6. Current Stock
        Integer stock = loteRepo.sumRemaining();
        if (stock == null) stock = 0;
        response.setCurrentStock(stock);
        response.setDeficit((finalExpected + (finalExpected * 0.15)) > stock);

        // 7. Gemini AI
        String insight = fetchGeminiInsight(weather, holidayInfo, newsHeadlines, stock, finalExpected, lastWeekendSales, twoWeekendsAgoSales);
        response.setGeminiInsight(insight);

        // Flag crisis
        response.setCrisis(insight.toLowerCase().contains("crisis"));

        // Save to cache
        this.cachedResponse = response;
        this.lastCacheTime = currentTime;

        return response;
    }

    private int getWeekendSales(int weeksAgo) {
        LocalDate friday = LocalDate.now().minusWeeks(weeksAgo).with(TemporalAdjusters.previousOrSame(DayOfWeek.FRIDAY));
        LocalDate sunday = friday.plusDays(2);

        String startDate = friday.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDate = sunday.format(DateTimeFormatter.ISO_LOCAL_DATE);

        Integer sum = transactionRepo.sumCantidadByDeliveryDateBetween(startDate, endDate);
        return sum == null ? 0 : sum;
    }

    private int calculateHistoricalAverage() {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int lastYear = now.getYear() - 1;

        YearMonth ym = YearMonth.of(lastYear, currentMonth);
        String startDate = ym.atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String endDate = ym.atEndOfMonth().format(DateTimeFormatter.ISO_LOCAL_DATE);

        Integer sum = transactionRepo.sumCantidadByDeliveryDateBetween(startDate, endDate);
        if (sum == null || sum == 0) return 1500; // Fallback

        return (int) (sum / 4.0);
    }

    private WeatherInfo fetchWeather() {
        String lat = "10.7981";
        String lon = "-68.3150";
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + weatherApiKey + "&units=metric&lang=es";

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode list = root.path("list");
            if (list.isArray() && list.size() > 0) {
                JsonNode first = list.get(0);
                boolean isRain = first.path("weather").get(0).path("main").asText().toLowerCase().contains("rain") ||
                                 first.path("weather").get(0).path("description").asText().toLowerCase().contains("lluvia");
                int prob = (int) (first.path("pop").asDouble() * 100);
                return new WeatherInfo(first.path("weather").get(0).path("description").asText(), prob, isRain);
            }
        } catch (Exception e) {
            System.err.println("Weather API Error: " + e.getMessage());
        }
        return new WeatherInfo("Desconocido", 0, false);
    }

    private String determineHoliday(LocalDate date) {
        int m = date.getMonthValue();
        if (m == 2) return "Carnavales";
        if (m == 3 || m == 4) return "Semana Santa";
        if (m == 8) return "Vacaciones Escolares";
        if (m == 12) return "Navidad";
        return "";
    }

    private String fetchCrisisNews() {
        String url = "https://news.google.com/rss/search?q=Tucacas+OR+Morrocoy&hl=es-419&gl=VE&ceid=VE:es-419";
        StringBuilder headlines = new StringBuilder();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            String xml = response.getBody();
            if (xml != null) {
                Pattern pattern = Pattern.compile("<title>(.*?)</title>");
                Matcher matcher = pattern.matcher(xml);
                int count = 0;
                while (matcher.find() && count < 10) {
                    String title = matcher.group(1);
                    if (!title.contains("Google Noticias")) {
                        headlines.append("- ").append(title).append("\n");
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("News Fetch Error: " + e.getMessage());
        }
        return headlines.toString();
    }

    private String fetchGeminiInsight(WeatherInfo w, String holiday, String news, int stock, int expected, int lastWknd, int twoWkndsAgo) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + geminiApiKey;

        String prompt = "Eres el motor de análisis inteligente de una PWA de un club en Tucacas, Venezuela.\n" +
                "Redacta un mensaje personalizado para la gerencia, interpretando inteligentemente esta data combinada:\n" +
                "- Ventas hace 2 fines de semana: " + twoWkndsAgo + " brazaletes.\n" +
                "- Ventas fin de semana pasado: " + lastWknd + " brazaletes.\n" +
                "- Clima esperado este finde: " + w.condition + " (" + w.rainProb + "% prob. de lluvia).\n" +
                "- Temporada/Festividad: " + (holiday.isEmpty() ? "Normal" : holiday) + ".\n" +
                "- Inventario actual de brazaletes: " + stock + ".\n" +
                "- Noticias Recientes en Tucacas: \n" + news + "\n\n" +
                "Instrucciones de Redacción (Muy Importante):\n" +
                "1. Usa este formato discursivo como inspiración: 'Los fines de semana anteriores se manejaron X ventas, se espera que este fin de semana pueda incrementarse/decrementarse en función a [clima, eventos, noticias], se acerca [Navidad/Carnavales/nada], y [tenemos / no tenemos] stock suficiente de brazaletes'.\n" +
                "2. Si las noticias revelan una crisis severa (sismo, derrame petrolero), advierte explícitamente el impacto negativo.\n" +
                "3. Si el stock (" + stock + ") es menor a " + expected + ", exige la compra INMEDIATA de brazaletes con tono de urgencia.\n" +
                "4. Siéntete libre de conectar los puntos: si las ventas vienen subiendo, pero lloverá, predice que la tendencia a la alta se romperá. Usa inteligencia de negocios.\n" +
                "5. Redacta un solo párrafo fluido y contundente, máximo 4 a 6 líneas.\n" +
                "6. Ademas indica que pronostico estas evaluando, si es para este fin de semana, si es para hoy, si es para mañana.";

        try {
            ObjectNode rootNode = objectMapper.createObjectNode();
            ArrayNode contents = rootNode.putArray("contents");
            ObjectNode content = contents.addObject();
            ArrayNode parts = content.putArray("parts");
            ObjectNode part = parts.addObject();
            part.put("text", prompt);

            // Añadir herramienta de búsqueda de Google (googleSearch)
            ArrayNode tools = rootNode.putArray("tools");
            ObjectNode tool = tools.addObject();
            tool.putObject("googleSearch");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(rootNode), headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode resNode = objectMapper.readTree(response.getBody());

            JsonNode candidates = resNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                return candidates.get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
        }
        return "No se pudo generar un insight en este momento.";
    }

    private static class WeatherInfo {
        String condition;
        int rainProb;
        boolean isRain;
        WeatherInfo(String c, int r, boolean b) { condition = c; rainProb = r; isRain = b; }
    }
}
