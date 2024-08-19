package io.tebex.minestom.gui;

import io.tebex.sdk.obj.ICategory;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;


public record BuyGuiConfig(
        int rows,
        Component title,
        CategoryItemFunction categoryItemFunction
) {


    @FunctionalInterface
    public interface CategoryItemFunction {

        Pair<Integer, ItemStack> apply(ICategory category);

    }


}



