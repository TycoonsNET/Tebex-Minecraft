package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import io.tebex.sdk.obj.PlayerLookupInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.tebex.minestom.command.BuyCommand.NO_SETUP;
import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class LookupSubCommand extends Command {

    public LookupSubCommand(MinestomTebex platform) {
        super("lookup");
        var usernameArg = ArgumentType.String("username")
                .setSuggestionCallback((sender, context, builder) -> {
                    for (var p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                        builder.addEntry(new SuggestionEntry(p.getUsername()));
                    }
                });

        addConditionalSyntax((sender, s) -> sender.hasPermission("tebex.lookup"), (sender, context) -> {
            if (!platform.isSetup()) {
                sender.sendMessage(NO_SETUP);
                return;
            }
            String username = context.get(usernameArg);
            platform.getSDK().getPlayerLookupInfo(username).exceptionally(throwable -> {
                sender.sendMessage(TEBEX_PREFIX.append(Component.text(throwable.getMessage(), NamedTextColor.GRAY)));
                return null;
            }).thenAccept(info -> {
                if (info == null) {
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text("No information found for that player.", NamedTextColor.GRAY)));
                } else {
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text("Username: " + info.getLookupPlayer().getUsername(), NamedTextColor.GRAY)));
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text("Id: " + info.getLookupPlayer().getId(), NamedTextColor.GRAY)));
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text("Chargeback Rate: " + info.chargebackRate, NamedTextColor.GRAY)));
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text("Bans Total: " + info.banCount, NamedTextColor.GRAY)));
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text("Payments: " + info.payments.size(), NamedTextColor.GRAY)));
                }
            });
            }, usernameArg);
    }

}
