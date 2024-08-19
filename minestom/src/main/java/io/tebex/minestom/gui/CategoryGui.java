package io.tebex.minestom.gui;

import io.tebex.minestom.MinestomTebex;
import io.tebex.sdk.obj.Category;
import io.tebex.sdk.obj.CategoryPackage;
import io.tebex.sdk.obj.ICategory;
import io.tebex.sdk.obj.SubCategory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Comparator;

import static io.tebex.minestom.gui.BuyGui.fromRows;


class CategoryGui extends Generic{

    public CategoryGui(
            MinestomTebex platform,
            CategoryGuiConfig config,
            ICategory category,
            BuyGui parent
    ) {
        super(fromRows(config.rows()), config.title());

        category.getPackages().sort(Comparator.comparingInt(CategoryPackage::getOrder));

        if (category instanceof Category cat) {
            if (cat.getSubCategories() != null) {
                cat.getSubCategories().forEach(sub -> {
                    var a = parent.config.categoryItemFunction().apply(sub);
                    setItemStack(a.first(), a.second());
                    addClickHandler(a.first(), event -> {
                        event.getPlayer().openInventory(new CategoryGui(
                                platform,
                                config,
                                sub,
                                parent
                        ));
                    });
                });
                setItemStack(config.backItem().first(), config.backItem().second());
                addClickHandler(config.backItem().first(), event -> {
                    event.getPlayer().openInventory(parent);
                });
            }
        }
        else if (category instanceof SubCategory sub) {
            setItemStack(config.backItem().first(), config.backItem().second());
            addClickHandler(config.backItem().first(), event -> {
                event.getPlayer().openInventory(new CategoryGui(
                        platform,
                        config,
                        sub.getParent(),
                        parent
                ));
            });
        }

        category.getPackages().forEach(pack -> {
            var a = config.categoryPackageFunction().apply(pack);
            setItemStack(a.first(), a.second());
            addClickHandler(a.first(), event -> {
                event.getPlayer().closeInventory();;
                platform.getSDK().createCheckoutUrl(
                        pack.getId(),
                        event.getPlayer().getUsername()
                ).thenAccept(checkout -> {
                    event.getPlayer().sendMessage(
                            Component.text("You can checkout here: ", NamedTextColor.GREEN)
                                    .append(Component.text(checkout.getUrl()).clickEvent(ClickEvent.openUrl(checkout.getUrl())))
                    );
                })
                        .exceptionally(ex -> {
                            event.getPlayer().sendMessage(
                                    Component.text("An error occurred while trying to create a checkout link", NamedTextColor.RED)
                            );
                            return null;
                        });
            });
        });
    }
}
