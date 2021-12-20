package joch.grabber.grabbers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import joch.grabber.utils.Logger;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThaumCraftGrabber extends BaseGrabber {
    private static JsonObject aspectToJSON(Object aspect, int qty) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> _class = aspect.getClass();
        JsonObject response = new JsonObject();

        response.addProperty("id", (String) _class.getDeclaredMethod("getTag").invoke(aspect));
        response.addProperty("dmg", 0);
        response.addProperty("translated_name", (String) _class.getDeclaredMethod("getLocalizedDescription").invoke(aspect));
        response.addProperty("display_name", (String) _class.getDeclaredMethod("getName").invoke(aspect));
        if (qty != 1) {
            response.addProperty("qty", qty);
        }
        return response;
    }

    // Convert AspectList class into JSON
    private static List<JsonObject> aspectsToList(Object aspectList) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<JsonObject> response = new ArrayList<>();
        for (Object aspect : (Object[]) aspectList.getClass().getDeclaredMethod("getAspects").invoke(aspectList)) {
            if (aspect == null) continue;
            response.add(aspectToJSON(aspect, (int) aspectList.getClass().getDeclaredMethod("getAmount", aspect.getClass()).invoke(aspectList, aspect)));
        }
        return response;
    }

    private static JsonObject handleArcaneRecipe(Class<?> _class, Object recipe) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Object output = _class.getDeclaredMethod("getRecipeOutput").invoke(recipe);
        if (output == null) return null;

        JsonObject recipeJSON = new JsonObject();
        recipeJSON.add("item", itemToJSON(output));

        JsonArray craft = new JsonArray();
        Object inputs = _class.getDeclaredMethod("getInput").invoke(recipe);

        if (List.class.isInstance(inputs)) {
            for (Object input : (List) inputs) {
                if (input == null) continue;

                craft.add(itemToJSON(input));
            }
        } else {
            for (Object input : (Object[]) inputs) {
                if (input == null) continue;

                if (ItemStack.class.isInstance(input)) {
                    craft.add(itemToJSON(input));
                } else {
                    ArrayList<ItemStack> lst = (ArrayList<ItemStack>) input;
                    craft.add(itemToJSON(getGeneralOreID(lst)));
                }

                craft.add(itemToJSON(input));
            }
        }

        for (JsonObject aspect : aspectsToList(_class.getDeclaredMethod("getAspects").invoke(recipe))) {
            if (aspect == null) continue;

            aspect.addProperty("isArcane", true);
            craft.add(aspect);
        }

        recipeJSON.add("craft", craft);
        return recipeJSON;
    }

    public static JsonArray Grab() {
        JsonArray response = new JsonArray();
        Class<?> API;
        try {
            API = Class.forName("thaumcraft.api.ThaumcraftApi");


            // Aspect crafts::

            // List, AspectList
            ConcurrentHashMap<List, Object> objectTags = (ConcurrentHashMap<List, Object>) API.getDeclaredField("objectTags").get(null);

            for (Map.Entry<List, Object> recipe : objectTags.entrySet()) {
                Object aspectList = recipe.getValue();

                if (recipe.getKey() == null || recipe.getKey().get(0) == null || aspectList == null) {
                    continue;
                }
                Item item = (Item) recipe.getKey().get(0);
                int meta = (int) recipe.getKey().get(1);

                JsonArray craft = new JsonArray();
                craft.add(itemToJSON(new ItemStack(item, 1, meta)));

                for (JsonObject aspect : aspectsToList(aspectList)) {
                    if (aspect == null) continue;

                    JsonObject recipeJSON = new JsonObject();
                    recipeJSON.add("item", aspect);
                    recipeJSON.add("craft", craft);

                    response.add(recipeJSON);
                }
            }

            // Crafting Recipes:
            List<Object> recipes = (List<Object>) API.getDeclaredMethod("getCraftingRecipes").invoke(null);

            // class definition:
            Class<?> crucibleRecipe = Class.forName("thaumcraft.api.crafting.CrucibleRecipe");
            Class<?> infusionRecipe = Class.forName("thaumcraft.api.crafting.InfusionRecipe");
            Class<?> shapedArcaneRecipe = Class.forName("thaumcraft.api.crafting.ShapedArcaneRecipe");
            Class<?> shapelessArcaneRecipe = Class.forName("thaumcraft.api.crafting.ShapelessArcaneRecipe");

            for (Object recipe : recipes) {
                if (infusionRecipe.isInstance(recipe)) {
                    Object output = infusionRecipe.getDeclaredMethod("getRecipeOutput").invoke(recipe);
                    if (output == null) {
                        continue;
                    }

                    JsonObject recipeJSON = new JsonObject();
                    recipeJSON.add("item", itemToJSON(output));
                    //recipeJSON.addProperty("research", (String) infusionRecipe.getDeclaredMethod("getResearch").invoke(recipe));
                    recipeJSON.addProperty("instability", (int) infusionRecipe.getDeclaredMethod("getInstability").invoke(recipe));

                    JsonArray craft = new JsonArray();
                    for (Object input : (Object[]) infusionRecipe.getDeclaredMethod("getComponents").invoke(recipe)) {
                        if (input == null) continue;
                        craft.add(itemToJSON(input));
                    }

                    for (JsonObject aspect : aspectsToList(infusionRecipe.getDeclaredMethod("getAspects").invoke(recipe))) {
                        if (aspect == null) continue;
                        craft.add(aspect);
                    }

                    recipeJSON.add("craft", craft);
                    response.add(recipeJSON);
                } else if (shapedArcaneRecipe.isInstance(recipe)) {
                    response.add(handleArcaneRecipe(shapedArcaneRecipe, recipe));
                } else if (shapelessArcaneRecipe.isInstance(recipe)) {
                    response.add(handleArcaneRecipe(shapelessArcaneRecipe, recipe));
                } else if (crucibleRecipe.isInstance(recipe)) {
                    ItemStack output = (ItemStack) crucibleRecipe.getDeclaredMethod("getRecipeOutput").invoke(recipe);
                    Object catalyst = crucibleRecipe.getDeclaredField("catalyst").get(recipe);
                    if (catalyst == null || output == null) continue;

                    JsonObject recipeJSON = new JsonObject();
                    recipeJSON.add("item", itemToJSON(output));

                    JsonArray craft = new JsonArray();
                    craft.add(itemToJSON(catalyst));

                    for (JsonObject aspect : aspectsToList(crucibleRecipe.getDeclaredField("aspects").get(recipe))) {
                        if (aspect == null) continue;
                        craft.add(aspect);
                    }
                    recipeJSON.add("craft", craft);

                    response.add(recipeJSON);
                }
            }


        } catch (Exception e) {
            Logger.WriteLine("Thaumcraft Error:: " + e);
        }


        return response;
    }
}
