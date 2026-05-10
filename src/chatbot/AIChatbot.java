package chatbot;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Hybrid AI Chatbot:
 * 1) Checks rule-based responses first (works offline)
 * 2) Falls back to Gemini API for unknown queries
 * 3) Final fallback shows emergency contacts if API fails
 *
 * HOW TO ADD YOUR API KEY:
 * Replace "YOUR_GEMINI_API_KEY_HERE" below with your actual key from:
 * https://makersuite.google.com/app/apikey
 */
public class AIChatbot {

    // ══════════════════════════════════════════════════════
    //  ★ STEP 1: Replace this with your Gemini API key ★
    //  Get free key at: https://aistudio.google.com/app/apikey
    // ══════════════════════════════════════════════════════
    public static String GEMINI_API_KEY = "........";

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private static final String SYSTEM_PROMPT =
            "You are RescueNet AI, an emergency disaster management assistant for Pakistan. " +
            "Provide concise, actionable safety guidance for floods, earthquakes, fires, landslides, and other disasters. " +
            "Always recommend calling 1122 (Rescue) for emergencies in Pakistan. " +
            "Keep answers focused, use bullet points, and be direct. " +
            "If asked about shelters or resources, say users should check the relevant module in RescueNet AI system. " +
            "Maximum 5-6 bullet points per response.";

    /** Returns true if a real API key has been configured */
    public static boolean isApiConfigured() {
        return GEMINI_API_KEY != null &&
               !GEMINI_API_KEY.equals("YOUR_GEMINI_API_KEY_HERE") &&
               GEMINI_API_KEY.length() > 10;
    }

    /** Main entry: tries rule-based first, then API */
    public static String getResponse(String userMessage) {
        String rule = getRuleBasedResponse(userMessage);
        if (rule != null) return rule;

        if (isApiConfigured()) {
            return getGeminiResponse(userMessage);
        }
        return getOfflineFallback(userMessage);
    }

    // ── Rule-based offline responses ──────────────────────
    public static String getRuleBasedResponse(String input) {
        input = input.toLowerCase().trim();

        if (contains(input, "flood", "flooding", "paani", "water level")) return
            "🌊 FLOOD SAFETY GUIDE:\n" +
            "• Move to higher ground immediately\n" +
            "• Avoid walking or driving in floodwater\n" +
            "• Turn off electricity at main breaker\n" +
            "• Do not drink tap water during floods\n" +
            "• Keep emergency kit: food, water, medicines, ID\n" +
            "• Call Rescue 1122 if trapped\n" +
            "• Contact NDMA: 051-9205436";

        if (contains(input, "earthquake", "tremor", "zalzala", "shaking")) return
            "🏚️ EARTHQUAKE SAFETY:\n" +
            "• DROP, COVER, HOLD ON immediately\n" +
            "• Stay away from windows and exterior walls\n" +
            "• If outside: move away from buildings, trees, power lines\n" +
            "• Do NOT use elevators after earthquake\n" +
            "• Check for gas leaks after shaking stops\n" +
            "• Expect aftershocks — stay alert\n" +
            "• NDMA Helpline: 051-9205436 | Rescue: 1122";

        if (contains(input, "fire", "aag", "burning", "smoke")) return
            "🔥 FIRE EMERGENCY:\n" +
            "• Activate nearest fire alarm\n" +
            "• Crawl low under smoke to exit\n" +
            "• Close doors to slow fire spread\n" +
            "• Use stairs ONLY — never elevators\n" +
            "• Feel doors before opening (back of hand)\n" +
            "• If clothes catch fire: STOP, DROP, ROLL\n" +
            "• Fire Brigade: 16 | Rescue: 1122";

        if (contains(input, "landslide", "mudslide", "debris")) return
            "⛰️ LANDSLIDE SAFETY:\n" +
            "• Evacuate immediately if you hear rumbling\n" +
            "• Move to higher ground away from slope\n" +
            "• Avoid river valleys during heavy rain\n" +
            "• Watch for unusual creek sounds or tilting trees\n" +
            "• Do not return until authorities declare safe\n" +
            "• Call 1122 for evacuation assistance";

        if (contains(input, "storm", "cyclone", "hurricane", "toofan")) return
            "🌪️ STORM SAFETY:\n" +
            "• Stay indoors away from windows\n" +
            "• Secure loose outdoor objects\n" +
            "• Keep emergency supplies ready\n" +
            "• Avoid flooded roads\n" +
            "• Monitor Pakistan Met Dept: www.pmd.gov.pk\n" +
            "• PMD Helpline: 051-9250373";

        if (contains(input, "first aid", "bleeding", "injury", "wound", "hurt")) return
            "🩺 FIRST AID BASICS:\n" +
            "• Bleeding: Apply firm pressure with clean cloth\n" +
            "• Elevate injured limb above heart level\n" +
            "• Burns: Cool with running water for 10 min\n" +
            "• Fracture: Immobilize, do not straighten\n" +
            "• Choking: 5 back blows + 5 abdominal thrusts\n" +
            "• Unconscious + breathing: Recovery position\n" +
            "• Ambulance: 115 | Rescue: 1122";

        if (contains(input, "shelter", "camp", "temporary housing", "panah")) return
            "🏠 FINDING SHELTER:\n" +
            "• Check the Shelter Management module in this system\n" +
            "• Contact Rescue 1122 for nearest relief camp\n" +
            "• Bring ID, medicines, warm clothing\n" +
            "• Register at shelter for relief assistance\n" +
            "• NADRA Emergency: for ID replacement\n" +
            "• NDMA: 051-9205436";

        if (contains(input, "contact", "number", "helpline", "call", "emergency")) return
            "📞 PAKISTAN EMERGENCY CONTACTS:\n" +
            "• 🚨 Rescue (Punjab): 1122\n" +
            "• 👮 Police: 15\n" +
            "• 🚑 Ambulance (Edhi): 115\n" +
            "• 🔥 Fire Brigade: 16\n" +
            "• 🏛️ NDMA: 051-9205436\n" +
            "• 🌧️ Pakistan Met Dept: 051-9250373\n" +
            "• 🏥 Pakistan Red Crescent: 051-9261579";

        if (contains(input, "missing person", "missing", "lost")) return
            "🔍 MISSING PERSON:\n" +
            "• Report to nearest police station immediately\n" +
            "• Contact Rescue 1122 during active disasters\n" +
            "• Provide recent photo, last known location\n" +
            "• Check all nearby shelters and hospitals\n" +
            "• NADRA can help trace people via CNIC\n" +
            "• Police: 15 | Child helpline: 1121";

        if (contains(input, "evacuation", "evacuate", "escape", "route")) return
            "🚶 EVACUATION GUIDE:\n" +
            "• Follow official evacuation orders immediately\n" +
            "• Take Go-Bag: water, food, meds, ID, cash\n" +
            "• Inform family/neighbors of your route\n" +
            "• Use designated routes only\n" +
            "• Help elderly and disabled neighbors\n" +
            "• Do NOT return until cleared by authorities\n" +
            "• Rescue: 1122";

        if (contains(input, "kit", "emergency kit", "go bag", "supplies")) return
            "🎒 EMERGENCY KIT CHECKLIST:\n" +
            "• Water: 3 liters per person per day (3 days)\n" +
            "• Food: Non-perishable items (3 days)\n" +
            "• First aid kit + prescription medicines\n" +
            "• Flashlight + extra batteries\n" +
            "• Whistle to signal for help\n" +
            "• Phone charger + power bank\n" +
            "• Copies of important documents (CNIC, passport)\n" +
            "• Blanket, warm clothes, rain gear\n" +
            "• Cash (ATMs may be unavailable)";

        if (contains(input, "hello", "hi", "salam", "assalam", "help", "what can you do")) return
            "🆘 Welcome to RescueNet AI Assistant!\n\n" +
            "I can help you with:\n" +
            "• 🌊 Flood safety guidance\n" +
            "• 🏚️ Earthquake safety\n" +
            "• 🔥 Fire emergency procedures\n" +
            "• 🩺 First aid basics\n" +
            "• 🏠 Finding shelters\n" +
            "• 📞 Emergency contacts\n" +
            "• 🎒 Emergency kit preparation\n" +
            "• 🚶 Evacuation procedures\n\n" +
            "Just type your question or click a quick button above!";

        return null; // No rule matched — try API
    }

    private static boolean contains(String input, String... keywords) {
        for (String kw : keywords) {
            if (input.contains(kw)) return true;
        }
        return false;
    }

    // ── Gemini API call ───────────────────────────────────
    public static String getGeminiResponse(String userMessage) {
        try {
            String fullPrompt = SYSTEM_PROMPT + "\n\nUser Question: " + userMessage;
            String body = buildJsonBody(fullPrompt);

            URL url = new URL(API_URL + GEMINI_API_KEY);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);

            try (OutputStream os = con.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                return getOfflineFallback(userMessage);
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }

            return parseGeminiResponse(sb.toString());

        } catch (Exception e) {
            return getOfflineFallback(userMessage);
        }
    }

    private static String buildJsonBody(String prompt) {
        String escaped = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "");
        return "{\"contents\":[{\"parts\":[{\"text\":\"" + escaped + "\"}]}]," +
               "\"generationConfig\":{\"temperature\":0.7,\"maxOutputTokens\":500}}";
    }

    private static String parseGeminiResponse(String json) {
        try {
            int textStart = json.indexOf("\"text\":\"") + 8;
            if (textStart < 8) return getOfflineFallback("");

            StringBuilder result = new StringBuilder();
            int i = textStart;
            while (i < json.length()) {
                if (json.charAt(i) == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    if (next == 'n') { result.append('\n'); i += 2; continue; }
                    if (next == '"') { result.append('"');  i += 2; continue; }
                    if (next == '\\') { result.append('\\'); i += 2; continue; }
                    if (next == 't') { result.append('\t'); i += 2; continue; }
                }
                if (json.charAt(i) == '"') break; // end of text field
                result.append(json.charAt(i));
                i++;
            }
            String text = result.toString().trim();
            return text.isEmpty() ? getOfflineFallback("") : text;
        } catch (Exception e) {
            return getOfflineFallback("");
        }
    }

    private static String getOfflineFallback(String query) {
        return "⚠️ AI API not configured or offline.\n\n" +
               "For emergency guidance, please ask about:\n" +
               "flood, earthquake, fire, shelter, first aid, or emergency contacts.\n\n" +
               "📞 Immediate Help:\n" +
               "• Rescue: 1122\n" +
               "• Police: 15\n" +
               "• Ambulance: 115\n" +
               "• NDMA: 051-9205436\n\n" ;
    }
}
