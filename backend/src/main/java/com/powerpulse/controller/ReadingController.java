package com.powerpulse.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.powerpulse.model.AppUser;
import com.powerpulse.model.Reading;
import com.powerpulse.repository.ReadingRepository;
import com.powerpulse.repository.UserRepository;

@Controller
public class ReadingController {

    private final UserRepository users;
    private final ReadingRepository readings;

    public ReadingController(UserRepository users, ReadingRepository readings) {
        this.users = users;
        this.readings = readings;
    }

    /** Result of validating the date + units typed by the user. */
    private record Parsed(LocalDate date, double units, String error) {
    }

    private Parsed parse(String dateText, String unitsText) {
        LocalDate date;
        try {
            date = LocalDate.parse(dateText == null ? "" : dateText.trim());
        } catch (DateTimeParseException e) {
            return new Parsed(null, 0, "Please enter a valid date.");
        }

        double units;
        try {
            units = Double.parseDouble(unitsText == null ? "" : unitsText.trim());
        } catch (NumberFormatException e) {
            return new Parsed(null, 0, "Units must be a number.");
        }

        if (Double.isNaN(units) || Double.isInfinite(units)) {
            return new Parsed(null, 0, "Units must be a valid number.");
        }
        if (units < 0) {
            return new Parsed(null, 0, "Units cannot be negative.");
        }
        return new Parsed(date, units, null);
    }

    private Long currentUserId(Principal principal) {
        AppUser user = users.findByEmail(principal.getName()).orElseThrow();
        return user.getId();
    }

    @PostMapping("/add-reading")
    public String add(@RequestParam("readingDate") String readingDate,
                      @RequestParam("units") String units,
                      Principal principal,
                      RedirectAttributes redirectAttributes) {

        Parsed parsed = parse(readingDate, units);
        if (parsed.error() != null) {
            redirectAttributes.addFlashAttribute("error", parsed.error());
            return "redirect:/";
        }

        readings.save(new Reading(parsed.date(), parsed.units(), currentUserId(principal)));
        redirectAttributes.addFlashAttribute("message", "Reading added.");
        return "redirect:/";
    }

    @PostMapping("/delete-reading/{id}")
    public String delete(@PathVariable("id") Long id,
                         Principal principal,
                         RedirectAttributes redirectAttributes) {

        Optional<Reading> reading = readings.findByIdAndUserId(id, currentUserId(principal));
        if (reading.isPresent()) {
            readings.delete(reading.get());
            redirectAttributes.addFlashAttribute("message", "Reading deleted.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Reading not found.");
        }
        return "redirect:/";
    }

    @GetMapping("/edit-reading/{id}")
    public String editPage(@PathVariable("id") Long id,
                           Principal principal,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        Optional<Reading> reading = readings.findByIdAndUserId(id, currentUserId(principal));
        if (reading.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Reading not found.");
            return "redirect:/";
        }
        model.addAttribute("reading", reading.get());
        return "edit";
    }

    @PostMapping("/edit-reading/{id}")
    public String edit(@PathVariable("id") Long id,
                       @RequestParam("readingDate") String readingDate,
                       @RequestParam("units") String units,
                       Principal principal,
                       RedirectAttributes redirectAttributes) {

        Optional<Reading> found = readings.findByIdAndUserId(id, currentUserId(principal));
        if (found.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Reading not found.");
            return "redirect:/";
        }

        Parsed parsed = parse(readingDate, units);
        if (parsed.error() != null) {
            redirectAttributes.addFlashAttribute("error", parsed.error());
            return "redirect:/edit-reading/" + id;
        }

        Reading reading = found.get();
        reading.setReadingDate(parsed.date());
        reading.setUnits(parsed.units());
        readings.save(reading);

        redirectAttributes.addFlashAttribute("message", "Reading updated.");
        return "redirect:/";
    }
}
