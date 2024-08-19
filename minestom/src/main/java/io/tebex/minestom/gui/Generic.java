package io.tebex.minestom.gui;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

abstract class Generic extends Inventory {

    static {
        MinecraftServer.getGlobalEventHandler().addListener(InventoryPreClickEvent.class, event -> {
            if (event.getInventory() instanceof Generic gui) {
                event.setCancelled(true);
                gui.processClick(event);
            }
        });
    }

    private final Map<Integer, Consumer<InventoryPreClickEvent>> clickHandlers = new HashMap<>();


    public Generic(@NotNull InventoryType inventoryType, @NotNull Component title) {
        super(inventoryType, title);
    }

    public void addClickHandler(int slot, Consumer<InventoryPreClickEvent> handler) {
        clickHandlers.put(slot, handler);
    }

    public void removeClickHandler(int slot) {
        clickHandlers.remove(slot);
    }

    public void processClick(InventoryPreClickEvent event) {
        if (clickHandlers.containsKey(event.getSlot())) {
            clickHandlers.get(event.getSlot()).accept(event);
        }
    }

}
