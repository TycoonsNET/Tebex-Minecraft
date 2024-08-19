package io.tebex.minestom.command;

import io.tebex.minestom.MinestomTebex;
import io.tebex.minestom.command.subcommands.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TebexCommand extends Command {

    public static final Component TEBEX_PREFIX = Component.text("[Tebex]", NamedTextColor.DARK_GRAY);

    private static final Component WELCOME_MSG = TEBEX_PREFIX
            .append(Component.text(" Welcome to Tebex!"));
    private static final Component VERSION_MSG = TEBEX_PREFIX
            .append(Component.text(" This server is running version ", NamedTextColor.GRAY))
            .append(Component.text("v" + MinecraftServer.VERSION_NAME));

    public TebexCommand(MinestomTebex platform, @NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setDefaultExecutor((sender, context) -> {
            sender.sendMessage(WELCOME_MSG);
            sender.sendMessage(VERSION_MSG);
        });

        addSubCommands(
                new BanSubCommand(platform),
                new CheckoutSubCommand(platform),
                new ForceCheckSubCommand(platform),
                new GoalsSubCommand(platform),
                new InfoSubCommand(platform),
                new LookupSubCommand(platform),
                new ReportSubCommand(platform),
                new SendLinkSubCommand(platform)
        );

    }

    private void addSubCommands(Command... commands) {
        for (Command command : commands) {
            addSubcommand(command);
        }
    }
}
