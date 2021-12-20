package joch.grabber.grabbers;

import com.google.common.base.Optional;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.Collection;
import java.util.List;

public class AppEngGrabber extends BaseGrabber {
    private static Object API;
    private static String[] pressList = {
            "calcProcessorPress",
            "engProcessorPress",
            "logicProcessorPress",
            "siliconPress"
    };

    // Ignore ProcessorPress
    private static boolean isPress(ItemStack item) {
        try {
            Object definitions = API.getClass().getDeclaredMethod("definitions").invoke(API);
            Object materials = definitions.getClass().getDeclaredMethod("materials").invoke(definitions);

            for (String pressName : pressList) {
                Object press = materials.getClass().getDeclaredMethod(pressName).invoke(materials);
                if ((boolean) press.getClass().getDeclaredMethod("isSameAs", ItemStack.class).invoke(press, item)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Is press error: " + e);
        }

        return false;
    }

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        //AEApi.instance().registries().inscriber()
        try {
            API = Class.forName("appeng.api.AEApi").getDeclaredMethod("instance").invoke(null);
            Object registries = API.getClass().getDeclaredMethod("registries").invoke(API);
            Object inscriber = registries.getClass().getDeclaredMethod("inscriber").invoke(registries);
            Collection<Object> recipes = (Collection<Object>) inscriber.getClass().getDeclaredMethod("getRecipes").invoke(inscriber);

            for (Object recipe : recipes) {
                ItemStack output = (ItemStack) recipe.getClass().getDeclaredMethod("getOutput").invoke(recipe);
                if (isPress(output)) continue;

                ItemStack input = ((List<ItemStack>) recipe.getClass().getDeclaredMethod("getInputs").invoke(recipe)).get(0);

                Optional<ItemStack> top = (Optional<ItemStack>) recipe.getClass().getDeclaredMethod("getTopOptional").invoke(recipe);
                Optional<ItemStack> bottom = (Optional<ItemStack>) recipe.getClass().getDeclaredMethod("getBottomOptional").invoke(recipe);


                JsonObject recipeJSON = new JsonObject();
                recipeJSON.add("item", itemToJSON(output));

                JsonArray craftArray = new JsonArray();
                craftArray.add(itemToJSON(input));
                if (top.isPresent() && !isPress(top.get()))
                    craftArray.add(itemToJSON(top.get()));
                if (bottom.isPresent() && !isPress(bottom.get()))
                    craftArray.add(itemToJSON(bottom.get()));

                recipeJSON.add("craft", craftArray);

                response.add(recipeJSON);
            }
        } catch (Exception e) {
            System.out.println("APPENG error: " + e);
        }

        return response;
    }
}
