package com.powerpulse.controller;

import java.security.Principal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.powerpulse.model.AppUser;
import com.powerpulse.model.Reading;
import com.powerpulse.repository.ReadingRepository;
import com.powerpulse.repository.UserRepository;

@Controller
public class DashboardController {

    private final UserRepository users;
    private final ReadingRepository readings;

    @Value("${app.rate-per-unit:7}")
    private double ratePerUnit;

    @Value("${app.high-usage-limit:100}")
    private double highUsageLimit;

    public DashboardController(UserRepository users, ReadingRepository readings) {
        this.users = users;
        this.readings = readings;
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        AppUser user = users.findByEmail(principal.getName()).orElseThrow();

        // newest first
        List<Reading> list = readings.findByUserIdOrderByReadingDateDescIdDesc(user.getId());

        double totalUnits = round2(list.stream().mapToDouble(Reading::getUnits).sum());
        double estimatedBill = round2(totalUnits * ratePerUnit);
        double latestUsage = list.isEmpty() ? 0 : list.get(0).getUnits();

        // charts need oldest -> newest
        List<Reading> chronological = new ArrayList<>(list);
        Collections.reverse(chronological);

        List<String> chartDates = new ArrayList<>();
        List<Double> chartUnits = new ArrayList<>();
        Map<String, Double> monthly = new TreeMap<>();

        for (Reading r : chronological) {
            chartDates.add(r.getReadingDate().toString());
            chartUnits.add(r.getUnits());
            monthly.merge(YearMonth.from(r.getReadingDate()).toString(), r.getUnits(), Double::sum);
        }

        List<String> monthlyLabels = new ArrayList<>(monthly.keySet());
        List<Double> monthlyUnits = new ArrayList<>();
        for (Double value : monthly.values()) {
            monthlyUnits.add(round2(value));
        }

        // status is based on the latest month that has data
        double currentMonthUnits = monthlyUnits.isEmpty() ? 0 : monthlyUnits.get(monthlyUnits.size() - 1);

        model.addAttribute("userName", user.getName());
        model.addAttribute("readings", list);
        boolean empty = list.isEmpty();
        model.addAttribute("totalUnits", empty ? (Object) 0 : (Object) totalUnits);
        model.addAttribute("estimatedBill", empty ? (Object) 0 : (Object) estimatedBill);
        model.addAttribute("latestUsage", empty ? (Object) 0 : (Object) latestUsage);
        model.addAttribute("currentMonthUnits", empty ? (Object) 0 : (Object) currentMonthUnits);
        model.addAttribute("highUsage", currentMonthUnits > highUsageLimit);
        model.addAttribute("highLimit", clean(highUsageLimit));
        model.addAttribute("rate", clean(ratePerUnit));
        model.addAttribute("chartDates", chartDates);
        model.addAttribute("chartUnits", chartUnits);
        model.addAttribute("monthlyLabels", monthlyLabels);
        model.addAttribute("monthlyUnits", monthlyUnits);

        return "index";
    }

    /** Shows 7 instead of 7.0 for whole numbers. */
    private static Object clean(double value) {
        if (value == Math.rint(value)) {
            return (long) value;
        }
        return value;
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
