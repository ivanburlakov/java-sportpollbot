package io.sportpoll.bot.config;

import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.toggle.AbilityToggle;

public class CustomAbilityToggle implements AbilityToggle {
    @Override
    public boolean isOff(Ability ability) {
        // Disable all built-in abilities except the ones we explicitly want
        if (ability.name().equals("start")) {
            return false; // Keep our custom start
        }
        return true; // Disable everything else (report, backup, recover, ban, unban, promote,
        // demote, claim, commands, stats)
    }

    @Override
    public Ability processAbility(Ability ability) {
        // Return ability as-is (no processing needed)
        return ability;
    }
}
