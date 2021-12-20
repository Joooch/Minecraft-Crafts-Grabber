package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExNihilio extends BaseGrabber {
    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        try {
            HashMap<Object, List<Object>> recipes = (HashMap<Object, List<Object>>) Class.forName("exnihilo.registries.HammerRegistry").getDeclaredMethod("getRewards").invoke(null);
            for (Map.Entry<Object, List<Object>> entry : recipes.entrySet()) {
                //Logger.WriteLine(entry.getKey().toString() + entry.getValue().toString());
                Object itemInfo = entry.getKey();
                if (itemInfo == null) continue;

                List<Object> smashableList = entry.getValue();
                if (smashableList == null) continue;

                List<ItemStack> outputs = new ArrayList<ItemStack>();

                for (Object smash : smashableList) {
                    float chance = (float) smash.getClass().getDeclaredField("chance").get(smash);
                    if (chance < 1) {
                        continue;
                    }

                    Item item = (Item) smash.getClass().getDeclaredField("item").get(smash);
                    int meta = (int) smash.getClass().getDeclaredField("meta").get(smash);

                    ItemStack output = new ItemStack(item, 1, meta);
                    if (output == null) continue;

                    boolean found = false;
                    for (ItemStack rememberedInput : outputs) {
                        if (rememberedInput.isItemEqual(output)) {
                            rememberedInput.stackSize += output.stackSize;
                            found = true;
                            break;
                        }
                    }
                    if( !found ){
                        outputs.add( output );
                    }
                }

                JsonObject inputJSON = itemToJSON(itemInfo.getClass().getDeclaredMethod("getStack").invoke(itemInfo));
                for (ItemStack output : outputs) {
                    JsonObject recipeJSON = new JsonObject();
                    recipeJSON.add("item", itemToJSON(output));

                    JsonArray craft = new JsonArray();
                    craft.add(inputJSON);

                    recipeJSON.add("craft", craft);
                    response.add(recipeJSON);
                }
            }
        } catch (Exception e) {
            Logger.WriteLine("Error ExNihilio: " + e);
        }

        return response;
    }
}
