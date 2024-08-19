package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;

import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class ReportSubCommand extends Command {

    public ReportSubCommand(MinestomTebex platform) {
        super("report");

        var msgArg = ArgumentType.String("message");

        addConditionalSyntax((sender, s) -> sender.hasPermission("tebex.report"), (sender, context) -> {
            sender.sendMessage(TEBEX_PREFIX.append(Component.text("Sending your report to Tebex...", NamedTextColor.GRAY)));
            platform.error("User reported error in-game: " + context.get(msgArg));
        }, msgArg);
    }

}
