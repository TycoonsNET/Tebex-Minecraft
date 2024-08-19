package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import static io.tebex.minestom.command.BuyCommand.NO_SETUP;
import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class BanSubCommand extends Command {

    public BanSubCommand(MinestomTebex platform) {
        super("ban");

        var playerArg = ArgumentType.String("player");
        playerArg.setSuggestionCallback((sender, context, builder) -> {
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                builder.addEntry(new SuggestionEntry(player.getUsername()));
            }
        });
        var reasonArg = ArgumentType.String("reason").setDefaultValue("No reason specified.");
        var ipArg = ArgumentType.String("ip").setDefaultValue("");

        addConditionalSyntax((sender, str) -> {
            return sender.hasPermission("tebex.ban");
        }, (sender, context) -> {

            if (!platform.isSetup()) {
                sender.sendMessage(NO_SETUP);
                return;
            }
            try {
                boolean success = platform.getSDK().createBan(context.get(playerArg), context.get(ipArg), context.get(reasonArg)).get();
                if (success) {
                    sender.sendMessage(
                            TEBEX_PREFIX.append(Component.text(" Player banned successfully.", NamedTextColor.GREEN)));
                } else {
                    sender.sendMessage(TEBEX_PREFIX.append(Component.text(" Failed to ban player.", NamedTextColor.RED)));
                }
            }
            catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                sender.sendMessage(TEBEX_PREFIX.append(Component.text(" Error while banning player: " + e.getMessage(), NamedTextColor.RED)));
            }

        }, playerArg, reasonArg, ipArg);
    }

}
