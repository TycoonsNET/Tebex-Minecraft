package io.tebex.minestom.command;

import io.tebex.minestom.MinestomTebex;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuyCommand extends Command {

    public static final Component NO_SETUP = Component.text("Tebex is not setup yet!", NamedTextColor.RED);
    public static final Component NO_CONSOLE = Component.text("Only players can execute this command!", NamedTextColor.RED);

    public BuyCommand(@NotNull MinestomTebex tebex,  @NotNull String name, @Nullable String... aliases) {
        super(name, aliases);
        setDefaultExecutor((sender, context) -> {
            if (!tebex.isSetup()) {
                sender.sendMessage(NO_SETUP);
                return;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(NO_CONSOLE);
                return;
            }
            var ui = tebex.getBuyGui().get();
            if (ui == null) {
                sender.sendMessage(Component.text("Failed to get listing. Please contact an administrator.", NamedTextColor.RED));
                return;
            }
            player.openInventory(ui);
        });
    }
}
