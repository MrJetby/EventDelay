package me.jetby.eventDelay.constructor;


import java.util.List;

public record Groups(
        String id,
        int chance,
        List<String> events) {
}
