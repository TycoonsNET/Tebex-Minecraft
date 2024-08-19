package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

import static io.tebex.minestom.command.BuyCommand.NO_SETUP;
import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class InfoSubCommand extends Command {

    public InfoSubCommand(MinestomTebex platform) {
        super("info");


        addConditionalSyntax((sender, s) -> sender.hasPermission("tebex.info"), (sender, context) -> {
            if (!platform.isSetup()) {
                sender.sendMessage(NO_SETUP);
                return;
            }
            sender.sendMessage(
                    TEBEX_PREFIX.append(Component.text("Information for this server:", NamedTextColor.GRAY))
                            .append(Component.newline())
                            .append(TEBEX_PREFIX)
                            .append(Component.text(platform.getStoreInformation().getServer().getName() + " for webstore " + platform.getStoreInformation().getStore().getName(), NamedTextColor.GRAY))
                            .append(Component.newline())
                            .append(TEBEX_PREFIX)
                            .append(Component.text("Server prices are in " + platform.getStoreInformation().getStore().getCurrency().getIso4217(), NamedTextColor.GRAY))
                            .append(Component.newline())
                            .append(TEBEX_PREFIX)
                            .append(Component.text("Webstore domain " + platform.getStoreInformation().getStore().getDomain(), NamedTextColor.GRAY))

            );
        });
    }

}
