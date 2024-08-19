package io.tebex.minestom.command.subcommands;

import io.tebex.minestom.MinestomTebex;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;

import static io.tebex.minestom.command.TebexCommand.TEBEX_PREFIX;

public class SendLinkSubCommand extends Command {

    public SendLinkSubCommand(MinestomTebex platform) {
        super("sendlink");

        var playerArg = ArgumentType.Entity("player").singleEntity(true).onlyPlayers(true);
        var packageIdArg = ArgumentType.Integer("packageId");

        addConditionalSyntax((sender, s) -> sender.hasPermission("tebex.sendlink"), (sender, context) -> {
            var player = context.get(playerArg).findFirstPlayer(sender);
            if (player == null) {
                sender.sendMessage(TEBEX_PREFIX.append(Component.text("Could not find a player with that name on the server.")));
                return;
            }
            int packageId = context.get(packageIdArg);
            platform.getSDK().createCheckoutUrl(packageId, player.getUsername()).exceptionally(throwable -> {
                sender.sendMessage(TEBEX_PREFIX.append(Component.text("Failed to get checkout link for package: " + throwable.getMessage())));
                return null;
            }).thenAccept(checkout -> {
               player.sendMessage(TEBEX_PREFIX.append(Component.text("A checkout link has been created for you. Click here to complete payment: " + checkout.getUrl())));
            });
        }, playerArg, packageIdArg);
    }

}
