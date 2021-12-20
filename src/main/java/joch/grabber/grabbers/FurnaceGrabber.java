package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import java.util.Map;

public class FurnaceGrabber extends BaseGrabber {

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();
        for (Map.Entry<ItemStack, ItemStack> entry : ((Map<ItemStack, ItemStack>) FurnaceRecipes.smelting().getSmeltingList()).entrySet()) {
            if (entry == null || entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            try {
                JsonObject recipe = new JsonObject();
                recipe.add("item", itemToJSON(entry.getValue()));

                JsonArray craftArray = new JsonArray();
                craftArray.add(itemToJSON(entry.getKey()));
                recipe.add("craft", craftArray);

                response.add(recipe);
            } catch (Exception e) {
                JsonObject unknown = new JsonObject();
                unknown.addProperty("error", e.toString());
                unknown.addProperty("info", "smelting");
                response.add(unknown);
            }
        }

        return response;
    }

}
