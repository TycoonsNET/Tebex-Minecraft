package io.tebex.minestom;

import io.tebex.minestom.command.BuyCommand;
import io.tebex.minestom.command.TebexCommand;
import io.tebex.minestom.gui.BuyGui;
import io.tebex.minestom.gui.BuyGuiConfig;
import io.tebex.minestom.gui.CategoryGuiConfig;
import io.tebex.minestom.placeholder.MinestomNamePlaceholder;
import io.tebex.sdk.SDK;
import io.tebex.sdk.Tebex;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.ServerEvent;
import io.tebex.sdk.placeholder.PlaceholderManager;
import io.tebex.sdk.placeholder.defaults.UuidPlaceholder;
import io.tebex.sdk.platform.Platform;
import io.tebex.sdk.platform.PlatformTelemetry;
import io.tebex.sdk.platform.PlatformType;
import io.tebex.sdk.platform.config.IPlatformConfig;
import io.tebex.sdk.platform.config.ServerPlatformConfig;
import io.tebex.sdk.request.response.ServerInformation;
import io.tebex.sdk.util.CommandResult;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinestomTebex implements Platform {

    private static final ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(4);
    public static final Logger LOGGER = LoggerFactory.getLogger(MinestomTebex.class);

    private SDK sdk;
    private ServerPlatformConfig config;
    private boolean setup;
    private PlaceholderManager placeholderManager;
    private Map<Object, Integer> queuedPlayers;

    private ServerInformation storeInformation;
    private List<Category> storeCategories;
    private List<ServerEvent> serverEvents;
    private Supplier<Inventory> buyGui;
    private final File dataFolder;

    public MinestomTebex(ServerPlatformConfig config, File dataFolder) {
        this.config = config;
        this.dataFolder = dataFolder;
    }
    public MinestomTebex(ServerPlatformConfig config) {
        this.config = config;
        {
            try {
                dataFolder = Files.createDirectory(Path.of("tebex")).toFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void enable() {
        Tebex.init(this);


        this.sdk = new SDK(this, config.getSecretKey());

        placeholderManager = new PlaceholderManager();
        placeholderManager.register(new UuidPlaceholder(placeholderManager));
        placeholderManager.register(new MinestomNamePlaceholder(placeholderManager));

        queuedPlayers = new ConcurrentHashMap<>();
        storeCategories = new ArrayList<>();
        serverEvents = new ArrayList<>();

        init();
        EXECUTOR.scheduleAtFixedRate(this::refreshListings, 0, 5, TimeUnit.MINUTES);
        EXECUTOR.scheduleAtFixedRate(() -> this.sdk.sendPluginEvents(), 0, 10, TimeUnit.MINUTES);
        EXECUTOR.scheduleAtFixedRate(() -> {
            List<ServerEvent> runEvents = new ArrayList<>(serverEvents.subList(0, Math.min(serverEvents.size(), 750)));
            if (runEvents.isEmpty()) return;
            if (!this.isSetup()) return;

            sdk.sendJoinEvents(runEvents)
                    .thenAccept(aVoid -> {
                        serverEvents.removeAll(runEvents);
                        debug("Successfully sent join events.");
                    })
                    .exceptionally(throwable -> {
                        debug("Failed to send join events: " + throwable.getMessage());
                        return null;
                    });
        }, 0, 1, TimeUnit.MINUTES);
    }

    public ServerInformation getStoreInformation() {
        return storeInformation;
    }

    public List<Category> getStoreCategories() {
        return storeCategories;
    }

    public List<ServerEvent> getServerEvents() {
        return serverEvents;
    }

    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public PlatformType getType() {
        return PlatformType.MINESTOM;
    }

    @Override
    public String getStoreType() {
        return storeInformation == null ? "" : storeInformation.getStore().getGameType();
    }

    @Override
    public SDK getSDK() {
        return sdk;
    }

    public Supplier<Inventory> getBuyGui() {
        return buyGui;
    }


    // to make it simpler
    public MinestomTebex buyGui(Supplier<Inventory> buyGui) {
        this.buyGui = buyGui;
        return this;
    }

    public MinestomTebex defaultBuyGui(BuyGuiConfig buyGuiConfig, CategoryGuiConfig categoryGuiConfig) {
        this.buyGui = () -> {
            if (storeCategories.isEmpty()) {
                return null;
            }
            return new BuyGui(this, storeCategories, buyGuiConfig, categoryGuiConfig);
        };
        return this;
    }

    public MinestomTebex defaultCommands() {
        var cm = MinecraftServer.getCommandManager();
        cm.register(new TebexCommand(this, "tebex"));
        cm.register(new BuyCommand(this, "buy"));
        return this;
    }


    @Override
    public File getDirectory() {
        return null;
    }

    @Override
    public boolean isSetup() {
        return setup;
    }

    @Override
    public void setSetup(boolean setup) {
        setup = true;
    }

    @Override
    public boolean isOnlineMode() {
        return MojangAuth.isEnabled() && !config.isProxyMode();
    }

    @Override
    public void configure() {
        setup = true;
        performCheck();
        sdk.sendTelemetry();
    }

    @Override
    public void halt() {
        setup = false;
    }

    @Override
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }

    @Override
    public Map<Object, Integer> getQueuedPlayers() {
        return queuedPlayers;
    }

    @Override
    public CommandResult dispatchCommand(String command) {
        var result = MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().getConsoleSender(), command);
        if (result.getType() == net.minestom.server.command.builder.CommandResult.Type.SUCCESS) {
            return CommandResult.from(true);
        }
        else {
            return CommandResult.from(false).withMessage("Invalid input: " + result.getInput());
        }
    }

    @Override
    public void executeAsync(Runnable runnable) {
        EXECUTOR.execute(runnable);
    }

    @Override
    public void executeAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        EXECUTOR.schedule(runnable, time, unit);
    };

    @Override
    public void executeBlocking(Runnable runnable) {
        MinecraftServer.getSchedulerManager().execute(runnable);
    }

    @Override
    public void executeBlockingLater(Runnable runnable, long time, TimeUnit unit) {
        MinecraftServer.getSchedulerManager().buildTask(runnable).delay(time, unit.toChronoUnit()).schedule();
    }

    @Override
    public boolean isPlayerOnline(Object player) {
        return getPlayer(player) != null;
    }

    @Override
    public int getFreeSlots(Object playerId) {
        var player = getPlayer(playerId);
        if (player == null) return -1;
        ItemStack[] inv = player.getInventory().getItemStacks();
        inv = Arrays.copyOfRange(inv, 0, 36);
        return (int) Arrays.stream(inv).filter(item -> item == null || item.isAir()).count();
    }

    @Override
    public String getVersion() {
        return MinecraftServer.VERSION_NAME;
    }

    @Override
    public void log(Level level, String message) {
        if (level == Level.INFO) {
            LOGGER.info(message);
        } else if (level == Level.WARNING) {
            LOGGER.warn(message);
        } else if (level == Level.SEVERE) {
            LOGGER.error(message);
        } else {
            LOGGER.debug(message);
        }
    }

    @Override
    public void setStoreInfo(ServerInformation info) {
        storeInformation = info;
    }

    public Player getPlayer(Object player) {
        if (player == null) return null;
        if (isOnlineMode() && !isGeyser() && player instanceof UUID uuid) {
            return MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid);
        }
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUsername((String) player);
    }

    @Override
    public void setStoreCategories(List<Category> categories) {
        storeCategories = categories;
    }

    @Override
    public IPlatformConfig getPlatformConfig() {
        return config;
    }

    @Override
    public PlatformTelemetry getTelemetry() {
        String serverVersion = MinecraftServer.VERSION_NAME;

        Pattern pattern = Pattern.compile("MC: (\\d+\\.\\d+\\.\\d+)");
        Matcher matcher = pattern.matcher(serverVersion);
        if (matcher.find()) {
            serverVersion = matcher.group(1);
        }

        return new PlatformTelemetry(
                getVersion(),
                MinecraftServer.getBrandName(),
                serverVersion,
                System.getProperty("java.version"),
                System.getProperty("os.arch"),
                MojangAuth.isEnabled()
        );
    }

    @Override
    public String getServerIp() {
        var p = MinecraftServer.process().server();
        return p.getAddress() + ":" + p.getPort();
    }
}
