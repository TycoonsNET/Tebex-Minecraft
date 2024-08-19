package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import io.tebex.sdk.obj.CommunityGoal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class GoalsSubCommand extends Command {
    public GoalsSubCommand(MinestomTebex platform) {
        super("goals");

        addConditionalSyntax((sender, str) -> {
            return sender.hasPermission("tebex.goals");
        }, (sender, context) -> {
            platform.getSDK().getCommunityGoals().thenAccept(list -> {

                sender.sendMessage(TEBEX_PREFIX.append(Component.text(" Community Goals: ", NamedTextColor.GRAY)));
                list.forEach(goal -> {
                    if (goal.getStatus() != CommunityGoal.Status.DISABLED) {
                        sender.sendMessage(TEBEX_PREFIX.append(Component.text(String.format(" - %s (%.2f/%.2f) [%s]", goal.getName(), goal.getCurrent(), goal.getTarget(), goal.getStatus()), NamedTextColor.GRAY)));
                    }
                });
            }).exceptionally(throwable -> {
                sender.sendMessage(TEBEX_PREFIX.append(Component.text(" Unexpected response: " + throwable.getMessage(), NamedTextColor.RED)));
                return null;
            });
        });
    }
}
