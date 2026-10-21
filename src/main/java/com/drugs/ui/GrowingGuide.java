package com.drugs.ui;

import com.drugs.DrugsV2;
import com.drugs.cartel.EnforcerGuide;
import com.drugs.growing.DrugGrowing;
import com.drugs.items.DrugItemMetadata;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Player growing index and crop-specific dialogs, shared by recipes and commands. */
public final class GrowingGuide {
    private GrowingGuide() {}

    public static String cropFromIngredient(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        String type = DrugItemMetadata.getItemType(item.getItemMeta());
        if (type == null || !type.startsWith("harvest_")) return null;
        String crop = type.substring("harvest_".length());
        return DrugGrowing.cropIds().contains(crop) ? crop : null;
    }

    public static void open(Player player, String crop, String recipe) {
        DrugsV2.getInstance().showDialog(player, createDialog(crop, recipe));
    }

    public static DialogView.MultiActionDialog createDialog(String crop, String recipe) {
        if (crop == null) {
            var actions = new ArrayList<DialogView.ActionButton>();
            for (String id : DrugGrowing.cropIds()) {
                DrugGrowing.Crop definition = DrugGrowing.crop(id);
                actions.add(Dialogs.button(definition.name(), "/drugs growing " + id, false,
                        DialogView.Icon.SEEDS, "Planting, growing conditions, harvests, and replanting."));
            }
            return Dialogs.menu(Dialogs.base("How to grow ingredients",
                    "Choose a crop for planting and harvesting instructions.\n\n"
                    + "You can also click harvested ingredients in a crafting recipe to open their growing guide.\n\n"
                    + "Recipes need custom harvested ingredients, not ordinary materials or planting stock."), actions);
        }
        List<String> steps = EnforcerGuide.growingSteps(crop);
        StringBuilder message = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) message.append(i + 1).append(". ").append(steps.get(i)).append("\n\n");
        var actions = new ArrayList<DialogView.ActionButton>();
        if (recipe != null) actions.add(Dialogs.button("Back to recipe", "/drugs recipe " + recipe, false, DialogView.Icon.CRAFTING_TABLE));
        actions.add(Dialogs.button("All crops", "/drugs growing", false, DialogView.Icon.SEEDS));
        return Dialogs.menu(Dialogs.base("How to grow: " + crop, message.toString()), actions);
    }
}
