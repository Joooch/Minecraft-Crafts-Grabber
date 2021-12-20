package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;

import java.util.List;

public class EnderIOGrabber extends BaseGrabber {
    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        try {
            Object manager = Class.forName("crazypants.enderio.machine.alloy.AlloyRecipeManager").getDeclaredMethod("getInstance").invoke(null);//.getInstance().getRecipes()
            List<Object> recipes = (List<Object>) manager.getClass().getMethod("getRecipes").invoke(manager);

            for (Object recipe : recipes) {
                JsonObject recipeJson = new JsonObject();

                ItemStack output = (ItemStack) recipe.getClass().getDeclaredMethod("getOutput").invoke(recipe);
                recipeJson.add("item", itemToJSON(output));

                Object[] inputs = (Object[]) recipe.getClass().getDeclaredMethod("getInputs").invoke(recipe);
                JsonArray craftArray = new JsonArray();

                for (Object input : inputs) {
                    Class<?> inputClass = input.getClass();
                    ItemStack inputStack;

                    if (Class.forName("crazypants.enderio.machine.recipe.OreDictionaryRecipeInput").isInstance(input)) {
                        inputStack = (ItemStack) inputClass.getMethod("getInput").invoke(input);
                    } else {
                        inputStack = (ItemStack) inputClass.getDeclaredMethod("getInput").invoke(input);
                    }

                    craftArray.add(itemToJSON(inputStack));
                }

                recipeJson.add("craft", craftArray);
                recipeJson.addProperty("energy", (int) recipe.getClass().getMethod("getEnergyRequired").invoke(recipe));

                response.add(recipeJson);
            }

        } catch (Exception e) {
        }


        return response;
    }
}
