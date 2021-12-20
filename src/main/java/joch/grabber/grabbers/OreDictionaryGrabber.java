package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class OreDictionaryGrabber extends BaseGrabber {
    //net.minecraftforge.oredict.OreDictionary.getOreNames()
    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        String[] names = OreDictionary.getOreNames();
        for (int i = 0; i < names.length; i++) {
            JsonObject oreJSON = itemToJSON(i);

            for (ItemStack item : OreDictionary.getOres(names[i], false)) {
                if (item == null) continue;
                JsonObject recipe = new JsonObject();
                recipe.add("item", oreJSON);

                JsonArray craft = new JsonArray();
                try {
                    craft.add(itemToJSON(item));
                } catch (Exception e) {
                    System.out.println("item error:" + e);
                    e.printStackTrace();
                }

                recipe.add("craft", craft);

                response.add(recipe);
            }
        }

        return response;
    }
}
