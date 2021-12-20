package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Field;
import java.util.List;

public class AvaritiaGrabber extends BaseGrabber {

    private static void HandleExtremeRecipe(IRecipe recipe, JsonArray output) {
        try {
            if (Class.forName("fox.spiteful.avaritia.crafting.ExtremeShapedOreRecipe").isInstance(recipe)) {
                output.add(createRecipeObjectWithMultipleInputs((Object[]) recipe.getClass().getDeclaredMethod("getInput").invoke(recipe), recipe.getRecipeOutput()));
            }else if (Class.forName("fox.spiteful.avaritia.crafting.ExtremeShapedRecipe").isInstance(recipe)) {
                output.add(createRecipeObjectWithMultipleInputs((Object[]) recipe.getClass().getDeclaredField("recipeItems").get(recipe), recipe.getRecipeOutput()));
            }else if (Class.forName("fox.spiteful.avaritia.crafting.ExtremeShapelessRecipe").isInstance(recipe)) {
                output.add(createRecipeObjectWithMultipleInputs(((List<?>) recipe.getClass().getDeclaredField("recipeItems").get(recipe)).toArray(), recipe.getRecipeOutput()));
            }
        } catch (Exception e) {
            Logger.WriteLine("Error on Recipe Handler: " + e + "\nClass:" + recipe.getClass( ));
        }
    }

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();

        // Compressor Manager
        try {
            // List of CompressorRecipes & CompressorOreRecipes
            List<Object> recipesList = (List<Object>) Class.forName("fox.spiteful.avaritia.crafting.CompressorManager").getDeclaredMethod("getRecipes").invoke(null);

            for (Object recipe : recipesList) {
                Class<?> _class = recipe.getClass();

                Object output = _class.getMethod("getOutput").invoke(recipe);
                Object input;
                int cost = (int) _class.getMethod("getCost").invoke(recipe);

                // Get input object. Check if OreDict or ItemStack
                if (Class.forName("fox.spiteful.avaritia.crafting.CompressOreRecipe").isInstance(recipe)) {
                    Field _field = _class.getDeclaredField("oreID");
                    _field.setAccessible(true);
                    input = _field.get(recipe);
                } else {
                    input = _class.getDeclaredMethod("getIngredient").invoke(recipe);
                }

                JsonObject recipeJSON = createSimpleRecipeObject(input, output);
                recipeJSON.get("craft").getAsJsonArray().get(0).getAsJsonObject().addProperty("qty", cost);
                response.add(recipeJSON);
            }
        } catch (Exception e) {
            Logger.WriteLine("Compressor error: " + e);
        }

        // Extreme Crafting Manager
        try {
            Object ExtremeCraftManager = Class.forName("fox.spiteful.avaritia.crafting.ExtremeCraftingManager").getDeclaredMethod("getInstance").invoke(null);
            List<IRecipe> recipesList = (List<IRecipe>) ExtremeCraftManager.getClass().getDeclaredMethod("getRecipeList").invoke(ExtremeCraftManager);

            for (IRecipe recipe : recipesList) {
                HandleExtremeRecipe(recipe, response);
            }
        } catch (Exception e) {
            Logger.WriteLine("Extreme crafting manager error: " + e);
        }

        return response;
    }

}
