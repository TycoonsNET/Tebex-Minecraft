package io.tebex.minestom.gui;

import io.tebex.minestom.MinestomTebex;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.CategoryPackage;
import io.tebex.sdk.obj.ICategory;
import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

public class BuyGui extends Generic {

    static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    static InventoryType fromRows(int rows) {
        return switch (rows) {
            case 1 -> InventoryType.CHEST_1_ROW;
            case 2 -> InventoryType.CHEST_2_ROW;
            case 3 -> InventoryType.CHEST_3_ROW;
            case 4 -> InventoryType.CHEST_4_ROW;
            case 5 -> InventoryType.CHEST_5_ROW;
            case 6 -> InventoryType.CHEST_6_ROW;
            default -> throw new IllegalArgumentException("Invalid rows: " + rows);
        };
    }

    private final MinestomTebex platform;

    public BuyGuiConfig config;

    public BuyGui(
            MinestomTebex platform,
            List<Category> sortedCategories,
            BuyGuiConfig config,
            CategoryGuiConfig categoryGuiConfig
    ) {
        super(fromRows(config.rows()), config.title());
        this.platform = platform;
        this.config = config;
        sortedCategories.sort(Comparator.comparingInt(Category::getOrder));

        sortedCategories.forEach(category -> {
            var a = config.categoryItemFunction().apply(category);
            setItemStack(a.first(), a.second());
            addClickHandler(a.first(), event -> {
                event.getPlayer().openInventory(new CategoryGui(
                        platform,
                        categoryGuiConfig,
                        category,
                        this
                ));
            });
        });
    }

    public BuyGuiConfig getConfig() {
        return config;
    }

    public static String handlePlaceholders(MinestomTebex platform, Object obj, String str) {
        if(obj instanceof ICategory category) {

            str = str.replace("%category%", category.getName());
        } else if(obj instanceof CategoryPackage categoryPackage) {


            str = str
                    .replace("%package_name%", categoryPackage.getName())
                    .replace("%package_price%", decimalFormat.format(categoryPackage.getPrice()))
                    .replace("%package_currency_name%", platform.getStoreInformation().getStore().getCurrency().getIso4217())
                    .replace("%package_currency%", platform.getStoreInformation().getStore().getCurrency().getSymbol());

            if(categoryPackage.hasSale()) {
                str = str
                        .replace("%package_discount%", decimalFormat.format(categoryPackage.getSale().getDiscount()))
                        .replace("%package_sale_price%", decimalFormat.format(categoryPackage.getPrice() - categoryPackage.getSale().getDiscount()));
            }
        }

        return str;
    }

}
