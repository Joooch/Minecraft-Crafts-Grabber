package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;

public class IC2Grabber extends BaseGrabber {
    // All inputs into one output
    private static void allInputsCombinations(List<ItemStack> inputs, List<ItemStack> outputs, JsonArray array, int amount) {
        for (ItemStack output : outputs) {
            for (ItemStack input : inputs) {
                JsonObject recipe = new JsonObject();
                recipe.add("item", itemToJSON(output));

                JsonArray inputsArray = new JsonArray();
                JsonObject inputObject = itemToJSON(input);
                inputObject.addProperty("qty", amount);
                inputsArray.add(inputObject);
                recipe.add("craft", inputsArray);

                array.add(recipe);
            }
        }
    }

    private static JsonArray handle_IMachineRecipeManager(Object manager) {
        JsonArray response = new JsonArray();

        try {
            Map<Object, Object> recipes = (Map<Object, Object>) manager.getClass().getMethod("getRecipes").invoke(manager);

            for (Map.Entry<Object, Object> entry : recipes.entrySet()) {
                try {
                    JsonArray separateCrafts = new JsonArray();

                    int amount = (int) entry.getKey().getClass().getDeclaredMethod("getAmount").invoke(entry.getKey());

                    List<ItemStack> inputs = (List<ItemStack>) entry.getKey().getClass().getDeclaredMethod("getInputs").invoke(entry.getKey());
                    List<ItemStack> outputs = (List<ItemStack>) entry.getValue().getClass().getDeclaredField("items").get(entry.getValue());
                    allInputsCombinations(inputs, outputs, separateCrafts, amount);

                    response.addAll(separateCrafts);
                } catch (Exception ee) {
                    Logger.WriteLine("Error: handle_IMachineRecipeManager two:\n" + ee);
                }
            }
        } catch (Exception e) {
            Logger.WriteLine("Error: handle_IMachineRecipeManager:\n" + e);
        }

        return response;
    }

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        // Grab macerator
        try {
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("compressor").get(null)));
            } catch (Exception ee) {
            }
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("metalformerExtruding").get(null)));
            } catch (Exception ee) {
            }
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("metalformerCutting").get(null)));
            } catch (Exception ee) {
            }
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("metalformerRolling").get(null)));
            } catch (Exception ee) {
            }
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("extractor").get(null)));
            } catch (Exception ee) {
            }
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("centrifuge").get(null)));
            } catch (Exception ee) {
            }
            /*try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("blastfurance").get(null)));
            } catch (Exception ee) {
            }*/
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("oreWashing").get(null)));
            } catch (Exception ee) {
            }
            try {
                response.addAll(handle_IMachineRecipeManager(Class.forName("ic2.api.recipe.Recipes").getDeclaredField("macerator").get(null)));
            } catch (Exception ee) {
            }

            // response.addAll(handle_IMachineRecipeManager((Object) Class.forName("ic2.api.recipe.Recipes").getDeclaredField("blockcutter").get(null)));
            //response.addAll(handle_IMachineRecipeManager((Object) Class.forName("ic2.api.recipe.Recipes").getDeclaredField("recycler").get(null))); // ??
            //response.addAll(handle_IMachineRecipeManager((Object) Class.forName("ic2.api.recipe.Recipes").getDeclaredField("cannerBottle").get(null)));
            //response.addAll(handle_IMachineRecipeManager((Object) Class.forName("ic2.api.recipe.Recipes").getDeclaredField("cannerEnrich").get(null)));
        } catch (Exception e) {
            System.out.println("Error: Grab:\n" + e.toString());
        }

        return response;
    }
}