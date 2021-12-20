package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class BotaniaGrabber extends BaseGrabber {
    // Botania API
    private static Class<?> API;

    private static JsonObject handleSimpleClass_MultiplyInputs(Object recipe) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        ItemStack output = (ItemStack) recipe.getClass().getMethod("getOutput").invoke(recipe);

        Object[] inputs = ((List<Object>) recipe.getClass().getMethod("getInputs").invoke(recipe)).toArray();
        return createRecipeObjectWithMultipleInputs(inputs, output);
    }

    private static JsonObject handleSimpleClass_OneInput(Object recipe) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        ItemStack output = (ItemStack) recipe.getClass().getMethod("getOutput").invoke(recipe);

        Object input = (Object) recipe.getClass().getMethod("getInput").invoke(recipe);
        return createSimpleRecipeObject(input, output);
    }

    private static JsonArray HandlePetalRecipes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        for (Object recipe : (List<?>) API.getDeclaredField("petalRecipes").get(null)) {
            response.add(handleSimpleClass_MultiplyInputs(recipe));
        }

        return response;
    }

    private static JsonArray HandlePureDaisyRecipes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        for (Object recipe : (List<?>) API.getDeclaredField("pureDaisyRecipes").get(null)) {
            Block outputBlock = (Block) recipe.getClass().getDeclaredMethod("getOutput").invoke(recipe);
            int outputMeta = (int) recipe.getClass().getDeclaredMethod("getOutputMeta").invoke(recipe);

            Object input = (Object) recipe.getClass().getDeclaredMethod("getInput").invoke(recipe);
            ItemStack output = new ItemStack(outputBlock, 1, outputMeta);

            if (input instanceof Block) {
                input = new ItemStack((Block) input);
            } // else oredict-string

            response.add(createSimpleRecipeObject(input, output));
        }

        return response;
    }

    private static JsonArray HandleManaInfusionRecipes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        for (Object recipe : (List<?>) API.getDeclaredField("manaInfusionRecipes").get(null)) {
            JsonObject recipeJSON = handleSimpleClass_OneInput(recipe);
            recipeJSON.addProperty("mana", (int) recipe.getClass().getMethod("getManaToConsume").invoke(recipe));
            response.add(recipeJSON);
        }

        return response;
    }

    private static JsonArray HandleRuneAltarRecipes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        for (Object recipe : (List<?>) API.getDeclaredField("runeAltarRecipes").get(null)) {
            JsonObject recipeJSON = handleSimpleClass_MultiplyInputs(recipe);
            recipeJSON.addProperty("mana", (int) recipe.getClass().getMethod("getManaUsage").invoke(recipe));
            response.add(recipeJSON);
        }

        return response;
    }

    private static JsonArray HandleElvenTradeRecipes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        for (Object recipe : (List<?>) API.getDeclaredField("elvenTradeRecipes").get(null)) {
            response.add(handleSimpleClass_MultiplyInputs(recipe));
        }

        return response;
    }

    private static JsonArray HandleBrewRecipes() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        ItemStack output_brewVial = new ItemStack((Item) Class.forName("vazkii.botania.common.item.ModItems").getDeclaredField("brewVial").get(null));

        for (Object recipe : (List<?>) API.getDeclaredField("brewRecipes").get(null)) {

            Object[] inputs = ((List<Object>) recipe.getClass().getMethod("getInputs").invoke(recipe)).toArray();
            JsonObject recipeJSON = createRecipeObjectWithMultipleInputs(inputs, output_brewVial);
            recipeJSON.addProperty( "mana", (int) recipe.getClass().getDeclaredMethod("getManaUsage").invoke(recipe) );
            response.add( recipeJSON );

            /*
            Object brew = recipe.getClass().getDeclaredMethod("getBrew").invoke(recipe);
            Logger.WriteLine("Brew:");
            Logger.WriteLine("key: " + brew.getClass().getMethod("getKey").invoke(brew));
            Logger.WriteLine("cost: " + brew.getClass().getMethod("getManaCost").invoke(brew));
            Logger.WriteLine("name: " + brew.getClass().getDeclaredMethod("getUnlocalizedName").invoke(brew));
            Logger.WriteLine("color: " + brew.getClass().getMethod("getColor", ItemStack.class).invoke(brew,null));
            */
        }

        return response;
    }

    private static JsonArray HandleMiniFlowerRecipes() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        JsonArray response = new JsonArray();

        for (Object recipe : (List<?>) API.getDeclaredField("miniFlowerRecipes").get(null)) {
            JsonObject recipeJSON = handleSimpleClass_OneInput(recipe);

            recipeJSON.addProperty("mana", (int) recipe.getClass().getMethod("getManaToConsume").invoke(recipe));
            response.add(recipeJSON);
        }

        return response;
    }

    public static JsonArray Grab() {
        JsonArray botaniaRecipes = new JsonArray();

        try {
            API = Class.forName("vazkii.botania.api.BotaniaAPI");

            botaniaRecipes.addAll(HandlePetalRecipes());
            botaniaRecipes.addAll(HandlePureDaisyRecipes());
            botaniaRecipes.addAll(HandleManaInfusionRecipes());
            botaniaRecipes.addAll(HandleRuneAltarRecipes());
            botaniaRecipes.addAll(HandleElvenTradeRecipes());
            botaniaRecipes.addAll(HandleBrewRecipes());
            botaniaRecipes.addAll(HandleMiniFlowerRecipes());
        } catch (Exception e) {
            Logger.WriteLine("Botania error: " + e);
        }

        return botaniaRecipes;
    }
}
