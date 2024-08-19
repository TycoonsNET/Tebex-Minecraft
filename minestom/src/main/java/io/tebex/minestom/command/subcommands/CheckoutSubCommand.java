package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import io.tebex.sdk.obj.CheckoutUrl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.tebex.minestom.command.BuyCommand.NO_CONSOLE;
import static io.tebex.minestom.command.BuyCommand.NO_SETUP;
import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class CheckoutSubCommand extends Command {
    public CheckoutSubCommand(MinestomTebex platform) {
        super("checkout");

        var packageArg = ArgumentType.Integer("package");
        addConditionalSyntax((sender, s) -> sender.hasPermission("tebex.checkout"), (sender, context) ->{
            if (!platform.isSetup()) {
                sender.sendMessage(NO_SETUP);
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage(NO_CONSOLE);
                return;
            }

            try {
                CheckoutUrl checkout = platform.getSDK().createCheckoutUrl(context.get(packageArg), player.getUsername()).get();
                sender.sendMessage(
                        TEBEX_PREFIX.append(
                                Component.text(" Checkout started! Click here to complete the payment: " , NamedTextColor.GREEN)
                                        .append(Component.text(checkout.getUrl(), NamedTextColor.YELLOW ).clickEvent(ClickEvent.openUrl(checkout.getUrl())))
                        )
                );
            }
            catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                sender.sendMessage(TEBEX_PREFIX.append(Component.text(" Failed to get checkout link for package, check package ID: " + e.getMessage(), NamedTextColor.RED)));
            }
        }, packageArg);

    }
}
