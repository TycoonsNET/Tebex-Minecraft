package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;

import static io.tebex.minestom.command.BuyCommand.NO_SETUP;
import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class ForceCheckSubCommand extends Command {

    private static final Component PERFORMANCE_CHECK = TEBEX_PREFIX.append(Component.text("Performing force check...", NamedTextColor.GREEN));

    public ForceCheckSubCommand(MinestomTebex platform) {
        super("forcecheck");

        addConditionalSyntax((sender, s) -> sender.hasPermission("tebex.forcecheck"), (sender, context) -> {
            if (!platform.isSetup()) {
                sender.sendMessage(NO_SETUP);
                return;
            }
            sender.sendMessage(PERFORMANCE_CHECK);
            platform.performCheck(true);
        });

    }
}
