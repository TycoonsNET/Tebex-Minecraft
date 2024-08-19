package io.tebex.minestom.gui;

import io.tebex.sdk.obj.CategoryPackage;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.minestom.server.item.ItemStack;


public record CategoryGuiConfig(
        int rows,
        Component title,
        Pair<Integer, ItemStack> backItem,
        CategoryPackageFunction categoryPackageFunction
) {

    public interface CategoryPackageFunction {
        Pair<Integer, ItemStack> apply(CategoryPackage categoryPackage);
    }

}
